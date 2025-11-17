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
public class HomeResourceResponse {
    private Long id;
    private String title;
    private String description;
    private String cover;
    private List<String> images;
    private String videoUrl;
    private String audioUrl;
    private String content;
    private List<String> tags;
    private String region;
    private Integer views;
    private Integer favorites;
    private Integer likes;
    private Integer comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 标识资源来源
    private String source; // "CULTURE_RESOURCE" 或 "COMMUNITY_POST"
    private String resourceType; // 对于文化资源，可能是 "ARTICLE", "EXHIBIT", "VIDEO", "AUDIO"
}







