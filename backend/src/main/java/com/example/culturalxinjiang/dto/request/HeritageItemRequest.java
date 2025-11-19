package com.example.culturalxinjiang.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class HeritageItemRequest {
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
}











