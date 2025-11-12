package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.request.AdminEventRequest;
import com.example.culturalxinjiang.dto.response.EventRegistrationResponse;
import com.example.culturalxinjiang.dto.response.EventResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.entity.Event;
import com.example.culturalxinjiang.entity.EventRegistration;
import com.example.culturalxinjiang.repository.EventRegistrationRepository;
import com.example.culturalxinjiang.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    @Transactional(readOnly = true)
    public PageResponse<EventResponse> getEvents(Integer page, Integer size, String keyword, String status) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        String trimmedKeyword = keyword != null ? keyword.trim() : null;
        Event.EventStatus eventStatus = parseStatus(status);

        Page<Event> eventPage;
        if (StringUtils.hasText(trimmedKeyword) && eventStatus != null) {
            eventPage = eventRepository.findByTitleContainingIgnoreCaseAndStatus(trimmedKeyword, eventStatus, pageable);
        } else if (StringUtils.hasText(trimmedKeyword)) {
            eventPage = eventRepository.findByTitleContainingIgnoreCase(trimmedKeyword, pageable);
        } else if (eventStatus != null) {
            eventPage = eventRepository.findByStatus(eventStatus, pageable);
        } else {
            eventPage = eventRepository.findAll(pageable);
        }

        List<EventResponse> responses = eventPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, eventPage.getTotalElements(), page, size);
    }

    @Transactional
    public EventResponse createEvent(AdminEventRequest request) {
        Event event = new Event();
        applyRequestToEvent(request, event, true);
        event.setRegistered(0);
        Event saved = eventRepository.save(event);
        return mapToResponse(saved);
    }

    @Transactional
    public EventResponse updateEvent(Long id, AdminEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("活动不存在"));
        applyRequestToEvent(request, event, false);
        Event saved = eventRepository.save(event);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("活动不存在");
        }
        registrationRepository.deleteByEventId(id);
        eventRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getRegistrations(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("活动不存在"));
        List<EventRegistration> registrations = registrationRepository.findByEventIdOrderByCreatedAtAsc(event.getId());
        return registrations.stream()
                .map(this::mapToRegistrationResponse)
                .collect(Collectors.toList());
    }

    private void applyRequestToEvent(AdminEventRequest request, Event event, boolean creating) {
        if (request.getTitle() != null || creating) {
            event.setTitle(request.getTitle());
        }
        if (request.getDescription() != null || creating) {
            event.setDescription(request.getDescription());
        }
        if (request.getCover() != null || creating) {
            event.setCover(request.getCover());
        }
        if (request.getType() != null || creating) {
            event.setType(parseType(request.getType()));
        }
        if (request.getStartDate() != null || creating) {
            event.setStartDate(parseDate(request.getStartDate(), "开始日期格式不正确"));
        }
        if (request.getEndDate() != null || creating) {
            event.setEndDate(parseDate(request.getEndDate(), "结束日期格式不正确"));
        }
        if (request.getCapacity() != null || creating) {
            event.setCapacity(request.getCapacity());
        }
        if (request.getPrice() != null || creating) {
            event.setPrice(request.getPrice());
        }
        if (request.getStatus() != null || creating) {
            event.setStatus(parseStatusOrThrow(request.getStatus()));
        }
        if (request.getContent() != null || creating) {
            event.setContent(request.getContent());
        }
        if (request.getLocation() != null || creating) {
            event.setLocation(request.getLocation());
        }
        if (request.getOrganizer() != null || creating) {
            event.setOrganizer(request.getOrganizer());
        }
        if (request.getImages() != null || creating) {
            event.setImages(request.getImages() != null ? new ArrayList<>(request.getImages()) : new ArrayList<>());
        }
        if (request.getSchedule() != null || creating) {
            event.setSchedule(request.getSchedule() != null ? new ArrayList<>(request.getSchedule()) : new ArrayList<>());
        }
        if (request.getRequirements() != null || creating) {
            event.setRequirements(request.getRequirements() != null ? new ArrayList<>(request.getRequirements()) : new ArrayList<>());
        }
    }

    private Event.EventType parseType(String type) {
        if (!StringUtils.hasText(type)) {
            throw new RuntimeException("活动类型不能为空");
        }
        try {
            return Event.EventType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("无效的活动类型: " + type);
        }
    }

    private Event.EventStatus parseStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        try {
            return Event.EventStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Event.EventStatus parseStatusOrThrow(String status) {
        Event.EventStatus eventStatus = parseStatus(status);
        if (eventStatus == null) {
            throw new RuntimeException("无效的活动状态: " + status);
        }
        return eventStatus;
    }

    private LocalDate parseDate(String value, String errorMessage) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME,
                DateTimeFormatter.ISO_DATE_TIME
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        // Try to parse first 10 characters (yyyy-MM-dd)
        if (trimmed.length() >= 10) {
            try {
                return LocalDate.parse(trimmed.substring(0, 10), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new RuntimeException(errorMessage + ": " + value);
    }

    private EventResponse mapToResponse(Event event) {
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
                .build();
    }

    private EventRegistrationResponse mapToRegistrationResponse(EventRegistration registration) {
        var user = registration.getUser();
        return EventRegistrationResponse.builder()
                .id(registration.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .registeredAt(registration.getCreatedAt())
                .build();
    }
}



