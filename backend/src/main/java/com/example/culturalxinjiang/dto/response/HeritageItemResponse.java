package com.example.culturalxinjiang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeritageItemResponse {
    private Long id;
    private String title;
    private String region;
    private String category;
    private String cover;
    private List<String> images;
    private String description;
    private String content;
    private String videoUrl;
    private String heritageLevel;
    private List<String> tags;
    private Boolean featured;
    private Integer views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String resourceType; // 固定为 HERITAGE，便于前端识别
}











