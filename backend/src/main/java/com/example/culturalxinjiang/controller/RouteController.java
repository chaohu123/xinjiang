package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.request.GenerateRouteRequest;
import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.RouteDetailResponse;
import com.example.culturalxinjiang.dto.response.RouteResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

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

    @PostMapping("/generate")
    public ApiResponse<RouteDetailResponse> generateRoute(@Valid @RequestBody GenerateRouteRequest request) {
        RouteDetailResponse response = routeService.generateRoute(request);
        return ApiResponse.success(response);
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






