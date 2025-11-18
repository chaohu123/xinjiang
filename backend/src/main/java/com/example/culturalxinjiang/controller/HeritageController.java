package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.request.HeritageItemRequest;
import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.HeritageItemResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.service.HeritageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/heritage")
@RequiredArgsConstructor
public class HeritageController {

    private final HeritageService heritageService;

    @GetMapping
    public ApiResponse<PageResponse<HeritageItemResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String heritageLevel,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<HeritageItemResponse> response = heritageService.search(
                keyword, region, category, heritageLevel, tags, page, size
        );
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<HeritageItemResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(heritageService.getDetail(id));
    }

    @GetMapping("/recommend")
    public ApiResponse<List<HeritageItemResponse>> recommend(
            @RequestParam(defaultValue = "6") Integer limit
    ) {
        return ApiResponse.success(heritageService.getRecommendations(limit));
    }
}


