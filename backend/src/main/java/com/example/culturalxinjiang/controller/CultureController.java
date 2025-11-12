package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.CultureResourceResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.entity.CultureResource;
import com.example.culturalxinjiang.entity.Favorite;
import com.example.culturalxinjiang.service.CultureResourceService;
import com.example.culturalxinjiang.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/culture")
@RequiredArgsConstructor
public class CultureController {

    private final CultureResourceService cultureResourceService;
    private final FavoriteService favoriteService;

    @GetMapping("/search")
    public ApiResponse<PageResponse<CultureResourceResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) CultureResource.CultureType type,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<CultureResourceResponse> response = cultureResourceService.search(
                keyword, type, region, tags, page, size);
        return ApiResponse.success(response);
    }

    @GetMapping("/{type}/{id}")
    public ApiResponse<CultureResourceResponse> getDetail(
            @PathVariable CultureResource.CultureType type,
            @PathVariable Long id
    ) {
        CultureResourceResponse response = cultureResourceService.getDetail(type, id);
        cultureResourceService.incrementViews(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/hot")
    public ApiResponse<List<CultureResourceResponse>> getHotResources(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<CultureResourceResponse> response = cultureResourceService.getHotResources(limit);
        return ApiResponse.success(response);
    }

    @GetMapping("/recommended")
    public ApiResponse<List<CultureResourceResponse>> getRecommendedResources(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<CultureResourceResponse> response = cultureResourceService.getRecommendedResources(limit);
        return ApiResponse.success(response);
    }

    @PostMapping("/{type}/{id}/favorite")
    public ApiResponse<Void> favoriteResource(
            @PathVariable CultureResource.CultureType type,
            @PathVariable Long id
    ) {
        favoriteService.favoriteResource(Favorite.ResourceType.CULTURE, id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{type}/{id}/favorite")
    public ApiResponse<Void> unfavoriteResource(
            @PathVariable CultureResource.CultureType type,
            @PathVariable Long id
    ) {
        favoriteService.unfavoriteResource(Favorite.ResourceType.CULTURE, id);
        return ApiResponse.success(null);
    }
}

