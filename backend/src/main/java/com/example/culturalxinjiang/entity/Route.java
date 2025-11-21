package com.example.culturalxinjiang.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String cover;

    @Column(nullable = false)
    private String theme;

    @Column(nullable = false)
    private Integer duration; // 天数

    @Column(nullable = false)
    private Double distance; // 公里

    @Column(name = "estimated_budget")
    private Double estimatedBudget; // 预估总预算（元）

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 路线创建者，可为空

    @Column(nullable = false)
    private String startLocation;

    @Column(nullable = false)
    private String endLocation;

    @Column(nullable = false)
    @Builder.Default
    private Integer waypoints = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer views = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer favorites = 0;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItineraryItem> itinerary = new ArrayList<>();

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RouteResource> resources = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "route_tips", joinColumns = @JoinColumn(name = "route_id"))
    @Column(name = "tip", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> tips = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Entity
    @Table(name = "itinerary_items")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItineraryItem {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "route_id", nullable = false)
        private Route route;

        @Column(nullable = false)
        private Integer day;

        @Column(nullable = false)
        private String title;

        @Column(columnDefinition = "TEXT")
        private String description;

        @ElementCollection
        @CollectionTable(name = "itinerary_locations", joinColumns = @JoinColumn(name = "itinerary_item_id"))
        @Builder.Default
        private List<Location> locations = new ArrayList<>();

        @Column(columnDefinition = "TEXT")
        private String accommodation;

        @ElementCollection
        @CollectionTable(name = "itinerary_meals", joinColumns = @JoinColumn(name = "itinerary_item_id"))
        @Column(name = "meal", columnDefinition = "TEXT")
        @Builder.Default
        private List<String> meals = new ArrayList<>();

        @Embeddable
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Location {
            @Column(length = 255)
            private String name;
            private Double lat;
            private Double lng;
            @Column(columnDefinition = "TEXT")
            private String description;
        }
    }

    @Entity
    @Table(name = "route_resources")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteResource {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "route_id", nullable = false)
        private Route route;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "culture_resource_id", nullable = false)
        private CultureResource cultureResource;

        @Column(nullable = false)
        private Integer order;
    }
}

