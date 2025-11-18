package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.request.ChangePasswordRequest;
import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.FavoriteItemResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.dto.response.UserInfoResponse;
import com.example.culturalxinjiang.service.FavoriteService;
import com.example.culturalxinjiang.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FavoriteService favoriteService;

    @GetMapping("/info")
    public ApiResponse<UserInfoResponse> getUserInfo() {
        UserInfoResponse response = userService.getUserInfo();
        return ApiResponse.success(response);
    }

    @PutMapping("/info")
    public ApiResponse<UserInfoResponse> updateUserInfo(@RequestBody UserInfoResponse request) {
        UserInfoResponse response = userService.updateUserInfo(request);
        return ApiResponse.success(response);
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ApiResponse.success(null);
    }

    @GetMapping("/favorites")
    public ApiResponse<PageResponse<FavoriteItemResponse>> getFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<FavoriteItemResponse> response = favoriteService.getFavorites(page, size);
        return ApiResponse.success(response);
    }
}

