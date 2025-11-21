package com.example.culturalxinjiang.dto.response;

import com.example.culturalxinjiang.dto.response.EventResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventCalendarResponse {
    private String month;
    private List<DayEvents> days;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayEvents {
        private Integer day;
        private List<EventResponse> events;
    }
}















<<<<<<< HEAD


=======
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
