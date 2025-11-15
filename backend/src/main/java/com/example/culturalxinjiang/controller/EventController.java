package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.EventDetailResponse;
import com.example.culturalxinjiang.dto.response.EventResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.entity.Event;
import com.example.culturalxinjiang.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ApiResponse<PageResponse<EventResponse>> getEvents(
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Event.EventType type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        // 将前端传来的小写 status 转换为枚举
        Event.EventStatus eventStatus = null;
        if (status != null) {
            try {
                eventStatus = Event.EventStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 如果转换失败，保持为 null，表示不过滤
            }
        }
        PageResponse<EventResponse> response = eventService.getEvents(month, eventStatus, type, page, size);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<EventDetailResponse> getEventDetail(@PathVariable Long id) {
        EventDetailResponse response = eventService.getEventDetail(id);
        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/register")
    public ApiResponse<Void> registerEvent(@PathVariable Long id) {
        eventService.registerEvent(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}/register")
    public ApiResponse<Void> cancelEventRegistration(@PathVariable Long id) {
        eventService.cancelEventRegistration(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/my-registrations")
    public ApiResponse<PageResponse<EventResponse>> getMyRegisteredEvents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<EventResponse> response = eventService.getMyRegisteredEvents(page, size);
        return ApiResponse.success(response);
    }

    @GetMapping("/latest")
    public ApiResponse<PageResponse<EventResponse>> getLatestEvents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<EventResponse> response = eventService.getLatestEvents(page, size);
        return ApiResponse.success(response);
    }
}


