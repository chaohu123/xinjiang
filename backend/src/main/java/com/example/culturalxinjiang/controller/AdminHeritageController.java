package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.request.HeritageItemRequest;
import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.HeritageItemResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.service.HeritageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/heritage")
@RequiredArgsConstructor
public class AdminHeritageController {

    private final HeritageService heritageService;

    @GetMapping
    public ApiResponse<PageResponse<HeritageItemResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String heritageLevel,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<HeritageItemResponse> response = heritageService.search(
                keyword, region, category, heritageLevel, null, page, size
        );
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<HeritageItemResponse> create(@RequestBody HeritageItemRequest request) {
        return ApiResponse.success(heritageService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<HeritageItemResponse> update(
            @PathVariable Long id,
            @RequestBody HeritageItemRequest request
    ) {
        return ApiResponse.success(heritageService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        heritageService.delete(id);
        return ApiResponse.success(null);
    }
}






