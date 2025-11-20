package com.example.culturalxinjiang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    private Long id;
    private String title;
    private String description;
    private String cover;
    private String theme;
    private Integer duration;
    private Double distance;
    private String startLocation;
    private String endLocation;
    private Integer waypoints;
    private Integer views;
    private Integer favorites;
    private LocalDateTime createdAt;
    private Double estimatedBudget;
}

