package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.HomeResourceResponse;
import com.example.culturalxinjiang.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping
    public ApiResponse<List<HomeResourceResponse>> recommend(
            @RequestParam(defaultValue = "6") Integer limit
    ) {
        return ApiResponse.success(recommendationService.getRecommendations(limit));
    }
}






