package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.request.AdminEventRequest;
import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.EventRegistrationResponse;
import com.example.culturalxinjiang.dto.response.EventResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.service.AdminEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final AdminEventService adminEventService;

    @GetMapping
    public ApiResponse<PageResponse<EventResponse>> getEvents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        PageResponse<EventResponse> response = adminEventService.getEvents(page, size, keyword, status);
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<EventResponse> createEvent(@RequestBody AdminEventRequest request) {
        EventResponse response = adminEventService.createEvent(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<EventResponse> updateEvent(
            @PathVariable Long id,
            @RequestBody AdminEventRequest request
    ) {
        EventResponse response = adminEventService.updateEvent(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEvent(@PathVariable Long id) {
        adminEventService.deleteEvent(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/registrations")
    public ApiResponse<List<EventRegistrationResponse>> getRegistrations(@PathVariable Long id) {
        List<EventRegistrationResponse> responses = adminEventService.getRegistrations(id);
        return ApiResponse.success(responses);
    }
}



