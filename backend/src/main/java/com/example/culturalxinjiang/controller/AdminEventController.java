package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.request.AdminEventRequest;
import com.example.culturalxinjiang.dto.request.EventRegistrationReviewRequest;
import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.EventRegistrationResponse;
import com.example.culturalxinjiang.dto.response.EventResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.service.AdminEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final AdminEventService adminEventService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

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

    @PutMapping("/{eventId}/registrations/{registrationId}/approve")
    public ApiResponse<Void> approveRegistration(
            @PathVariable Long eventId,
            @PathVariable Long registrationId
    ) {
        adminEventService.approveRegistration(eventId, registrationId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{eventId}/registrations/{registrationId}/reject")
    public ApiResponse<Void> rejectRegistration(
            @PathVariable Long eventId,
            @PathVariable Long registrationId,
            @RequestBody(required = false) EventRegistrationReviewRequest request
    ) {
        String reason = request != null ? request.getReason() : null;
        adminEventService.rejectRegistration(eventId, registrationId, reason);
        return ApiResponse.success(null);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, String>> uploadMedia(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
            return ApiResponse.error("仅支持上传图片或视频文件");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String url = contextPath + "/uploads/" + filename;
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("type", contentType);

            return ApiResponse.success(result);
        } catch (IOException e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }
}



