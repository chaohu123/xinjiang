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
public class RouteAnalyticsResponse {
    private Summary summary;
    private List<ThemeStat> themeStats;
    private List<PreferenceStat> durationPreferences;
    private List<PreferenceStat> budgetPreferences;
    private List<HeatmapPoint> heatmapPoints;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private long totalRoutes;
        private double avgDuration;
        private double avgDistance;
        private double geoCoverageRate;
        private double budgetCoverageRate;
        private LocalDateTime generatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThemeStat {
        private String theme;
        private String label;
        private long count;
        private double percent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferenceStat {
        private String label;
        private long count;
        private double percent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeatmapPoint {
        private Long routeId;
        private String routeTitle;
        private Double lat;
        private Double lng;
        private double intensity;
        private Integer views;
        private Integer favorites;
        private String theme;
        private Integer duration;
        private String startLocation;
        private String endLocation;
    }
}





