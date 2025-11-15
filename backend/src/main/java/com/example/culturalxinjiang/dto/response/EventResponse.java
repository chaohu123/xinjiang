package com.example.culturalxinjiang.dto.response;

import com.example.culturalxinjiang.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String cover;
    private Event.EventType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private EventLocation location;
    private Integer capacity;
    private Integer registered;
    private Double price;
    private Event.EventStatus status;
    private LocalDateTime createdAt;
    private List<String> images;
    private List<String> videos;
    private Boolean isRegistered; // 当前用户是否已报名

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventLocation {
        private String name;
        private String address;
        private Double lat;
        private Double lng;
    }
}

