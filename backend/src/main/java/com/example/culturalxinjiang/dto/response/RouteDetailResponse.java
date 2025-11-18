package com.example.culturalxinjiang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RouteDetailResponse extends RouteResponse {
    private List<ItineraryItem> itinerary;
    private List<RouteResource> resources;
    private List<String> tips;
    private Boolean favorited;
    private List<TimelineItem> timeline;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItineraryItem {
        private Integer day;
        private String title;
        private String description;
        private List<Location> locations;
        private String accommodation;
        private List<String> meals;
        private String transportation;
        private String timeSchedule;
        private String dailyBudget;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Location {
            private String name;
            private Double lat;
            private Double lng;
            private String description;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteResource {
        private Long id;
        private String type;
        private String title;
        private String cover;
        private Integer order;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineItem {
        private Integer day;
        private String label;
        private String startTime;
        private String endTime;
        private Integer stopCount;
        private Double estimatedDistance;
    }
}

