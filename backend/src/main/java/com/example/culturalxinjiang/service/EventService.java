package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.response.EventDetailResponse;
import com.example.culturalxinjiang.dto.response.EventResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.entity.Event;
import com.example.culturalxinjiang.entity.EventRegistration;
import com.example.culturalxinjiang.entity.EventRegistration.RegistrationStatus;
import com.example.culturalxinjiang.entity.User;
import com.example.culturalxinjiang.repository.EventRegistrationRepository;
import com.example.culturalxinjiang.repository.EventRepository;
import com.example.culturalxinjiang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    @Transactional
    public PageResponse<EventResponse> getEvents(String month, Event.EventStatus status, Event.EventType type, Integer page, Integer size) {
        // 先自动更新活动状态
        updateEventStatuses();

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Event> eventPage;

        // 如果指定了 status，使用 status 查询
        if (status != null) {
            eventPage = eventRepository.findByStatus(status, pageable);
        } else if (type != null) {
            // 如果指定了 type，使用 type 查询
            LocalDate monthDate = month != null ? LocalDate.parse(month + "-01") : null;
            eventPage = eventRepository.findByTypeAndMonth(type, monthDate, pageable);
        } else {
            // 否则查询所有事件
            eventPage = eventRepository.findAll(pageable);
        }

        List<EventResponse> responses = eventPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, eventPage.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("活动不存在"));

        return mapToDetailResponse(event);
    }

    @Transactional
    public void registerEvent(Long eventId) {
        User user = getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("活动不存在"));

        if (registrationRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw new RuntimeException("已经报名过该活动");
        }

        if (event.getCapacity() != null) {
            long approvedCount = registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.APPROVED);
            if (approvedCount >= event.getCapacity()) {
                throw new RuntimeException("活动已满员");
            }
        }

        EventRegistration registration = EventRegistration.builder()
                .user(user)
                .event(event)
                .status(RegistrationStatus.PENDING)
                .build();
        registrationRepository.save(registration);

        updateRegisteredCount(event);
    }

    @Transactional
    public void cancelEventRegistration(Long eventId) {
        User user = getCurrentUser();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("活动不存在"));

        EventRegistration registration = registrationRepository.findByUserIdAndEventId(user.getId(), eventId)
                .orElseThrow(() -> new RuntimeException("未报名该活动"));

        registrationRepository.delete(registration);

        updateRegisteredCount(event);
    }

    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getMyRegisteredEvents(Integer page, Integer size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, size);

        List<EventRegistration> registrations = registrationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        // 计算分页
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), registrations.size());
        List<EventRegistration> pagedRegistrations = start < registrations.size()
            ? registrations.subList(start, end)
            : new ArrayList<>();

        List<EventResponse> responses = pagedRegistrations.stream()
                .map(registration -> {
                    Event event = registration.getEvent();
                    EventResponse response = mapToResponse(event);
                    // 设置报名状态信息
                    response.setIsRegistered(true);
                    return response;
                })
                .collect(Collectors.toList());

        return PageResponse.of(responses, (long) registrations.size(), page, size);
    }

    private void updateRegisteredCount(Event event) {
        long approvedCount = registrationRepository.countByEventIdAndStatus(event.getId(), RegistrationStatus.APPROVED);
        event.setRegistered(Math.toIntExact(approvedCount));
        eventRepository.save(event);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private EventResponse mapToResponse(Event event) {
        // 显式访问懒加载集合，确保在事务内加载
        List<String> images = event.getImages() != null ? new ArrayList<>(event.getImages()) : new ArrayList<>();
        List<String> videos = event.getVideos() != null ? new ArrayList<>(event.getVideos()) : new ArrayList<>();

        EventResponse.EventLocation location = null;
        if (event.getLocation() != null) {
            location = new EventResponse.EventLocation(
                    event.getLocation().getName(),
                    event.getLocation().getAddress(),
                    event.getLocation().getLat(),
                    event.getLocation().getLng()
            );
        }

        // 检查当前用户是否已报名
        Boolean isRegistered = checkUserRegistration(event.getId());

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .cover(event.getCover())
                .type(event.getType())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(location)
                .capacity(event.getCapacity())
                .registered(event.getRegistered())
                .price(event.getPrice())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .images(images)
                .videos(videos)
                .isRegistered(isRegistered)
                .build();
    }

    private EventDetailResponse mapToDetailResponse(Event event) {
        EventDetailResponse response = new EventDetailResponse();
        // Copy base fields
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setCover(event.getCover());
        response.setType(event.getType());
        response.setStartDate(event.getStartDate());
        response.setEndDate(event.getEndDate());

        if (event.getLocation() != null) {
            response.setLocation(new EventResponse.EventLocation(
                    event.getLocation().getName(),
                    event.getLocation().getAddress(),
                    event.getLocation().getLat(),
                    event.getLocation().getLng()
            ));
        }

        response.setCapacity(event.getCapacity());
        response.setRegistered(event.getRegistered());
        response.setPrice(event.getPrice());
        response.setStatus(event.getStatus());
        response.setCreatedAt(event.getCreatedAt());

        // Set detail fields
        response.setContent(event.getContent());
        // 显式访问懒加载集合，确保在事务内加载
        List<String> images = event.getImages() != null ? new ArrayList<>(event.getImages()) : new ArrayList<>();
        response.setImages(images);
        List<String> videos = event.getVideos() != null ? new ArrayList<>(event.getVideos()) : new ArrayList<>();
        response.setVideos(videos);

        if (event.getOrganizer() != null) {
            response.setOrganizer(new EventDetailResponse.Organizer(
                    event.getOrganizer().getName(),
                    event.getOrganizer().getContact()
            ));
        }

        // 显式访问懒加载集合
        List<Event.ScheduleItem> schedule = event.getSchedule() != null ? new ArrayList<>(event.getSchedule()) : new ArrayList<>();
        response.setSchedule(schedule.stream()
                .map(s -> new EventDetailResponse.ScheduleItem(s.getTime(), s.getActivity()))
                .collect(Collectors.toList()));
        List<String> requirements = event.getRequirements() != null ? new ArrayList<>(event.getRequirements()) : new ArrayList<>();
        response.setRequirements(requirements);

        // 检查当前用户是否已报名
        response.setIsRegistered(checkUserRegistration(event.getId()));

        return response;
    }

    /**
     * 检查当前用户是否已报名该活动
     * @param eventId 活动ID
     * @return 如果用户已登录且已报名返回true，否则返回false或null
     */
    private Boolean checkUserRegistration(Long eventId) {
        try {
            Optional<User> currentUser = getCurrentUserOptional();
            if (currentUser.isPresent()) {
                return registrationRepository.existsByUserIdAndEventId(currentUser.get().getId(), eventId);
            }
        } catch (Exception e) {
            // 用户未登录或其他错误，返回null
        }
        return null;
    }

    /**
     * 获取当前用户（如果已登录）
     * @return 当前用户，如果未登录返回Optional.empty()
     */
    private Optional<User> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())
                || "anonymousUser".equals(authentication.getName())) {
            return Optional.empty();
        }
        try {
            return userRepository.findByUsername(authentication.getName());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 根据日期自动更新活动状态
     * 将过期的活动状态更新为PAST，将已开始的活动状态更新为ONGOING，将未开始的活动状态更新为UPCOMING
     * 只更新需要更新的活动，提高性能
     */
    @Transactional
    public void updateEventStatuses() {
        LocalDate today = LocalDate.now();

        // 只查询可能需要更新的活动（状态不是PAST的活动，因为PAST状态不会再改变）
        List<Event> eventsToCheck = eventRepository.findAll().stream()
                .filter(event -> event.getStatus() != Event.EventStatus.PAST)
                .collect(Collectors.toList());

        for (Event event : eventsToCheck) {
            Event.EventStatus calculatedStatus = calculateEventStatus(event, today);

            // 只有当计算出的状态与当前状态不同时才更新
            if (event.getStatus() != calculatedStatus) {
                event.setStatus(calculatedStatus);
                eventRepository.save(event);
            }
        }
    }

    /**
     * 根据活动日期计算活动状态
     * @param event 活动
     * @param today 今天的日期
     * @return 计算出的活动状态
     */
    private Event.EventStatus calculateEventStatus(Event event, LocalDate today) {
        LocalDate startDate = event.getStartDate();
        LocalDate endDate = event.getEndDate();

        // 如果结束日期在今天之前，活动已结束
        if (endDate.isBefore(today)) {
            return Event.EventStatus.PAST;
        }

        // 如果开始日期在今天之后，活动即将开始
        if (startDate.isAfter(today)) {
            return Event.EventStatus.UPCOMING;
        }

        // 否则活动进行中（今天在开始和结束日期之间）
        return Event.EventStatus.ONGOING;
    }

    /**
     * 获取首页最新动态：当前时间一个月前后的即将开始、进行中或已结束的活动
     * @param page 页码
     * @param size 每页大小
     * @return 活动列表
     */
    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getLatestEvents(Integer page, Integer size) {
        // 先自动更新活动状态
        updateEventStatuses();

        LocalDate today = LocalDate.now();
        LocalDate oneMonthAgo = today.minusMonths(1);
        LocalDate oneMonthLater = today.plusMonths(1);

        // 查询一个月前后且状态为即将开始、进行中或已结束的活动
        List<Event.EventStatus> statuses = List.of(
                Event.EventStatus.UPCOMING,
                Event.EventStatus.ONGOING,
                Event.EventStatus.PAST
        );

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Event> eventPage = eventRepository.findRecentEvents(oneMonthAgo, oneMonthLater, statuses, pageable);

        List<EventResponse> responses = eventPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, eventPage.getTotalElements(), page, size);
    }
}

