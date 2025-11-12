package com.example.culturalxinjiang.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String cover;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "name", column = @Column(name = "location_name")),
        @AttributeOverride(name = "address", column = @Column(name = "location_address")),
        @AttributeOverride(name = "lat", column = @Column(name = "location_lat")),
        @AttributeOverride(name = "lng", column = @Column(name = "location_lng"))
    })
    private EventLocation location;

    private Integer capacity;

    @Column(nullable = false)
    @Builder.Default
    private Integer registered = 0;

    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EventStatus status = EventStatus.UPCOMING;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    @CollectionTable(name = "event_images", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "name", column = @Column(name = "organizer_name")),
        @AttributeOverride(name = "contact", column = @Column(name = "organizer_contact"))
    })
    private Organizer organizer;

    @ElementCollection
    @CollectionTable(name = "event_schedules", joinColumns = @JoinColumn(name = "event_id"))
    @Builder.Default
    private List<ScheduleItem> schedule = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "event_requirements", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "requirement")
    @Builder.Default
    private List<String> requirements = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventLocation {
        private String name;
        private String address;
        private Double lat;
        private Double lng;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Organizer {
        private String name;
        private String contact;
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScheduleItem {
        private String time;
        private String activity;
    }

    public enum EventType {
        EXHIBITION, PERFORMANCE, WORKSHOP, TOUR
    }

    public enum EventStatus {
        UPCOMING, ONGOING, PAST
    }
}

