package com.example.culturalxinjiang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapPoiResponse {

    private List<MapPoi> pois;
    private List<MapCluster> clusters;
    private Map<String, Long> stats;
    private Long total;
    private Long filtered;
    private boolean clustered;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapPoi {
        private Long id;
        private String originType; // CULTURE / HERITAGE
        private String category;   // scenic / relic / museum / heritage
        private String title;
        private Double lat;
        private Double lng;
        private String region;
        private String cover;
        private List<String> tags;
        private String summary;
        private Integer views;
        private Integer favorites;
        private Long originRefId; // useful for heritage映射
        private String contentType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapCluster {
        private Double lat;
        private Double lng;
        private Long count;
        private Map<String, Long> categories;
    }
}


