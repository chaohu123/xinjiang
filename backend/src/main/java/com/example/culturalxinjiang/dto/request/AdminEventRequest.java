package com.example.culturalxinjiang.dto.request;

import com.example.culturalxinjiang.entity.Event;
import lombok.Data;

import java.util.List;

@Data
public class AdminEventRequest {
    private String title;
    private String description;
    private String cover;
    /**
     * 活动类型，支持小写输入，如 "exhibition"
     */
    private String type;
    /**
     * 活动开始日期，支持 "yyyy-MM-dd" 或 ISO 日期时间字符串
     */
    private String startDate;
    /**
     * 活动结束日期，支持 "yyyy-MM-dd" 或 ISO 日期时间字符串
     */
    private String endDate;
    private Integer capacity;
    private Double price;
    /**
     * 活动状态，支持小写输入，如 "upcoming"
     */
    private String status;
    private Event.EventLocation location;
    private List<String> images;
    private Event.Organizer organizer;
    private List<Event.ScheduleItem> schedule;
    private List<String> requirements;
    /**
     * 活动正文内容
     */
    private String content;
}



