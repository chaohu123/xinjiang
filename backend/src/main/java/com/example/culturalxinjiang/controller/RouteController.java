package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.request.GenerateRouteRequest;
import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.RouteDetailResponse;
import com.example.culturalxinjiang.dto.response.RouteResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.entity.Favorite;
import com.example.culturalxinjiang.service.FavoriteService;
import com.example.culturalxinjiang.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final FavoriteService favoriteService;

    @GetMapping
    public ApiResponse<PageResponse<RouteResponse>> getRoutes(
            @RequestParam(required = false) String theme,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<RouteResponse> response = routeService.getRoutes(theme, page, size);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<RouteDetailResponse> getRouteDetail(@PathVariable Long id) {
        RouteDetailResponse response = routeService.getRouteDetail(id);
        return ApiResponse.success(response);
    }

    @PostMapping("/{id}/favorite")
    public ApiResponse<Void> favoriteRoute(@PathVariable Long id) {
        favoriteService.favoriteResource(Favorite.ResourceType.ROUTE, id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}/favorite")
    public ApiResponse<Void> unfavoriteRoute(@PathVariable Long id) {
        favoriteService.unfavoriteResource(Favorite.ResourceType.ROUTE, id);
        return ApiResponse.success(null);
    }

    @PostMapping("/generate")
    public ApiResponse<RouteDetailResponse> generateRoute(@Valid @RequestBody GenerateRouteRequest request) {
        RouteService.RouteGenerationResult result = routeService.generateRouteWithDebug(request);
        RouteDetailResponse response = result.getRoute();
        ApiResponse.DebugInfo debugInfo = result.getDebugInfo();

        // 构建包含调试信息的响应
        ApiResponse<RouteDetailResponse> apiResponse = ApiResponse.<RouteDetailResponse>builder()
                .code(200)
                .message("成功")
                .data(response)
                .timestamp(System.currentTimeMillis())
                .debugInfo(debugInfo)
                .build();

        return apiResponse;
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<RouteResponse>> getMyRoutes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<RouteResponse> response = routeService.getMyRoutes(page, size);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMyRoute(@PathVariable Long id) {
        routeService.deleteMyRoute(id);
        return ApiResponse.success(null);
    }
}






