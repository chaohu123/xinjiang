package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.CultureResourceResponse;
import com.example.culturalxinjiang.dto.response.HomeResourceResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.entity.CultureResource;
import com.example.culturalxinjiang.entity.Favorite;
import com.example.culturalxinjiang.service.CultureResourceService;
import com.example.culturalxinjiang.service.FavoriteService;
import com.example.culturalxinjiang.service.HomeRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/culture")
@RequiredArgsConstructor
public class CultureController {

    private final CultureResourceService cultureResourceService;
    private final FavoriteService favoriteService;
    private final HomeRecommendationService homeRecommendationService;

    @GetMapping("/search")
    public ApiResponse<PageResponse<CultureResourceResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        CultureResource.CultureType cultureType = null;
        boolean heritageOnly = false;
        if (type != null && !type.isBlank()) {
            if ("heritage".equalsIgnoreCase(type)) {
                heritageOnly = true;
            } else {
                cultureType = CultureResource.CultureType.valueOf(type.toUpperCase());
            }
        }

        PageResponse<CultureResourceResponse> response;
        if (heritageOnly) {
            response = cultureResourceService.wrapHeritageOnly(keyword, region, tags, page, size);
        } else {
            response = cultureResourceService.search(keyword, cultureType, region, tags, page, size);
        }
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
    public ApiResponse<List<HomeResourceResponse>> getHotResources(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<HomeResourceResponse> response = homeRecommendationService.getHotResources(limit);
        return ApiResponse.success(response);
    }

    @GetMapping("/recommended")
    public ApiResponse<List<HomeResourceResponse>> getRecommendedResources(
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        List<HomeResourceResponse> response = homeRecommendationService.getFeaturedResources(limit);
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

