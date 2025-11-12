package com.example.culturalxinjiang.dto.response;

import com.example.culturalxinjiang.entity.CultureResource;
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
public class CultureResourceResponse {
    private Long id;
    private CultureResource.CultureType type;
    private String title;
    private String description;
    private String cover;
    private List<String> images;
    private String videoUrl;
    private String audioUrl;
    private String content;
    private List<String> tags;
    private String region;
    private Location location;
    private Integer views;
    private Integer favorites;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private Double lat;
        private Double lng;
        private String address;
    }
}






