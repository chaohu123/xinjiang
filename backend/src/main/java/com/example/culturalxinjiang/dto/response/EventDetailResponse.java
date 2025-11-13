package com.example.culturalxinjiang.dto.response;

import com.example.culturalxinjiang.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EventDetailResponse extends EventResponse {
    private String content;
    private Organizer organizer;
    private List<ScheduleItem> schedule;
    private List<String> requirements;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Organizer {
        private String name;
        private String contact;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleItem {
        private String time;
        private String activity;
    }
}

