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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getEvents(String month, Event.EventStatus status, Event.EventType type, Integer page, Integer size) {
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

        return response;
    }
}

