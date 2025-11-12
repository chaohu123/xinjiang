package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.request.CreatePostRequest;
import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.CultureResourceResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.dto.response.UserInfoResponse;
import com.example.culturalxinjiang.dto.response.CommunityPostResponse;
import com.example.culturalxinjiang.entity.CultureResource;
import com.example.culturalxinjiang.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ==================== 用户管理 ====================
    
    @GetMapping("/users")
    public ApiResponse<PageResponse<UserInfoResponse>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword
    ) {
        PageResponse<UserInfoResponse> response = adminService.getUsers(page, size, keyword);
        return ApiResponse.success(response);
    }

    @PutMapping("/users/{id}")
    public ApiResponse<UserInfoResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserInfoResponse request
    ) {
        UserInfoResponse response = adminService.updateUser(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<Void> toggleUserStatus(
            @PathVariable Long id,
            @RequestBody ToggleStatusRequest request
    ) {
        adminService.toggleUserStatus(id, request.enabled);
        return ApiResponse.success(null);
    }

    // ==================== 文化资源管理 ====================

    @GetMapping("/culture")
    public ApiResponse<PageResponse<CultureResourceResponse>> getCultureResources(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type
    ) {
        CultureResource.CultureType cultureType = null;
        if (type != null && !type.trim().isEmpty()) {
            try {
                // 支持小写和大写输入，转换为枚举
                cultureType = CultureResource.CultureType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 如果类型无效，忽略该参数
            }
        }
        PageResponse<CultureResourceResponse> response = adminService.getCultureResources(page, size, keyword, cultureType);
        return ApiResponse.success(response);
    }

    @PostMapping("/culture")
    public ApiResponse<CultureResourceResponse> createCultureResource(
            @RequestBody CultureResourceRequest request
    ) {
        // 如果 type 是字符串，转换为枚举
        if (request.getTypeString() != null && request.getType() == null) {
            try {
                request.setType(CultureResource.CultureType.valueOf(request.getTypeString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的资源类型: " + request.getTypeString());
            }
        }
        CultureResourceResponse response = adminService.createCultureResource(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/culture/{id}")
    public ApiResponse<CultureResourceResponse> updateCultureResource(
            @PathVariable Long id,
            @RequestBody CultureResourceRequest request
    ) {
        // 如果 type 是字符串，转换为枚举
        if (request.getTypeString() != null && request.getType() == null) {
            try {
                request.setType(CultureResource.CultureType.valueOf(request.getTypeString().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("无效的资源类型: " + request.getTypeString());
            }
        }
        CultureResourceResponse response = adminService.updateCultureResource(id, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/culture/{id}")
    public ApiResponse<Void> deleteCultureResource(@PathVariable Long id) {
        adminService.deleteCultureResource(id);
        return ApiResponse.success(null);
    }

    // ==================== 社区投稿管理 ====================

    @GetMapping("/posts")
    public ApiResponse<PageResponse<CommunityPostResponse>> getPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        PageResponse<CommunityPostResponse> response = adminService.getPosts(page, size, keyword, status);
        return ApiResponse.success(response);
    }

    @PutMapping("/posts/{id}/approve")
    public ApiResponse<Void> approvePost(@PathVariable Long id) {
        adminService.approvePost(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/posts/{id}/reject")
    public ApiResponse<Void> rejectPost(
            @PathVariable Long id,
            @RequestBody(required = false) RejectPostRequest request
    ) {
        adminService.rejectPost(id, request != null ? request.reason : null);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/posts/{id}")
    public ApiResponse<Void> deletePost(@PathVariable Long id) {
        adminService.deletePost(id);
        return ApiResponse.success(null);
    }

    // ==================== 内部请求类 ====================

    @lombok.Data
    public static class ToggleStatusRequest {
        private Boolean enabled;
    }

    @lombok.Data
    public static class CultureResourceRequest {
        private CultureResource.CultureType type;
        private String typeString; // 支持字符串类型的 type（前端可能发送小写）
        private String title;
        private String description;
        private String cover;
        private java.util.List<String> images;
        private String videoUrl;
        private String audioUrl;
        private String content;
        private java.util.List<String> tags;
        private String region;
        private CultureResourceResponse.Location location;
    }

    @lombok.Data
    public static class RejectPostRequest {
        private String reason;
    }
}

