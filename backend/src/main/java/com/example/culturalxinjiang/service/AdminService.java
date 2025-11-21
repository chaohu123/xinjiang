package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.response.CultureResourceResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.dto.response.RouteAnalyticsResponse;
import com.example.culturalxinjiang.dto.response.UserInfoResponse;
import com.example.culturalxinjiang.dto.response.CommunityPostResponse;
import com.example.culturalxinjiang.dto.response.EventResponse;
import com.example.culturalxinjiang.entity.CultureResource;
import com.example.culturalxinjiang.entity.CommunityPost;
import com.example.culturalxinjiang.entity.Event;
import com.example.culturalxinjiang.entity.Route;
import com.example.culturalxinjiang.entity.User;
import com.example.culturalxinjiang.repository.CultureResourceRepository;
import com.example.culturalxinjiang.repository.CommunityPostRepository;
import com.example.culturalxinjiang.repository.EventRepository;
import com.example.culturalxinjiang.repository.RouteRepository;
import com.example.culturalxinjiang.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final CultureResourceRepository cultureResourceRepository;
    private final CommunityPostRepository communityPostRepository;
    private final EventRepository eventRepository;
    private final RouteRepository routeRepository;

    private static final Map<String, String> THEME_LABELS = Map.of(
            "silkRoad", "丝绸之路",
            "nature", "自然风光",
            "culture", "文化体验",
            "food", "美食探索"
    );

    private static final Map<String, double[]> CITY_COORDINATES = Map.ofEntries(
            Map.entry("乌鲁木齐", new double[]{43.793026, 87.627704}),
            Map.entry("喀什", new double[]{39.4704, 75.9898}),
            Map.entry("吐鲁番", new double[]{42.9476, 89.1788}),
            Map.entry("库尔勒", new double[]{41.7269, 86.1746}),
            Map.entry("阿克苏", new double[]{41.1675, 80.2684}),
            Map.entry("阿勒泰", new double[]{47.8484, 88.1389}),
            Map.entry("伊犁", new double[]{43.9169, 81.3242}),
            Map.entry("那拉提", new double[]{43.8224, 82.7043}),
            Map.entry("巴音布鲁克", new double[]{42.9035, 84.1465}),
            Map.entry("库车", new double[]{41.7167, 82.9833}),
            Map.entry("喀纳斯", new double[]{48.9983, 86.9958}),
            Map.entry("和田", new double[]{37.1142, 79.9225}),
            Map.entry("哈密", new double[]{42.8586, 93.5319}),
            Map.entry("塔什库尔干", new double[]{37.7726, 75.2284}),
            Map.entry("天山天池", new double[]{43.9073, 88.1234})
    );

    // ==================== 用户管理 ====================

    @Transactional(readOnly = true)
    public PageResponse<UserInfoResponse> getUsers(Integer page, Integer size, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<User> userPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            userPage = userRepository.findByUsernameContainingOrEmailContaining(
                    keyword.trim(), keyword.trim(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<UserInfoResponse> responses = userPage.getContent().stream()
                .map(this::mapToUserInfoResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, userPage.getTotalElements(), page, size);
    }

    @Transactional
    public UserInfoResponse updateUser(Long id, UserInfoResponse request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getEmail() != null) {
            // 检查邮箱是否被其他用户使用
            userRepository.findByEmail(request.getEmail())
                    .ifPresent(u -> {
                        if (!u.getId().equals(id)) {
                            throw new RuntimeException("邮箱已被使用");
                        }
                    });
            user.setEmail(request.getEmail());
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        user = userRepository.save(user);
        return mapToUserInfoResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        userRepository.delete(user);
    }

    @Transactional
    public void toggleUserStatus(Long id, Boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    // ==================== 文化资源管理 ====================

    @Transactional(readOnly = true)
    public PageResponse<CultureResourceResponse> getCultureResources(
            Integer page, Integer size, String keyword, CultureResource.CultureType type) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CultureResource> resourcePage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            resourcePage = cultureResourceRepository.search(
                    keyword.trim(), type, null, pageable);
        } else if (type != null) {
            resourcePage = cultureResourceRepository.findByType(type, pageable);
        } else {
            resourcePage = cultureResourceRepository.findAll(pageable);
        }

        List<CultureResourceResponse> responses = resourcePage.getContent().stream()
                .map(this::mapToCultureResourceResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, resourcePage.getTotalElements(), page, size);
    }

    @Transactional
    public CultureResourceResponse createCultureResource(
            com.example.culturalxinjiang.controller.AdminController.CultureResourceRequest request) {
        CultureResource resource = CultureResource.builder()
                .type(request.getType())
                .title(request.getTitle())
                .description(request.getDescription())
                .cover(request.getCover())
                .images(request.getImages() != null ? new ArrayList<>(request.getImages()) : new ArrayList<>())
                .videoUrl(request.getVideoUrl())
                .audioUrl(request.getAudioUrl())
                .content(request.getContent())
                .tags(request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>())
                .region(request.getRegion())
                .views(0)
                .favorites(0)
                .build();

        if (request.getLocation() != null) {
            CultureResource.Location location = new CultureResource.Location(
                    request.getLocation().getLat(),
                    request.getLocation().getLng(),
                    request.getLocation().getAddress()
            );
            resource.setLocation(location);
        }

        resource = cultureResourceRepository.save(resource);
        return mapToCultureResourceResponse(resource);
    }

    @Transactional
    public CultureResourceResponse updateCultureResource(
            Long id, com.example.culturalxinjiang.controller.AdminController.CultureResourceRequest request) {
        CultureResource resource = cultureResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在"));

        if (request.getType() != null) {
            resource.setType(request.getType());
        }
        if (request.getTitle() != null) {
            resource.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            resource.setDescription(request.getDescription());
        }
        if (request.getCover() != null) {
            resource.setCover(request.getCover());
        }
        if (request.getImages() != null) {
            resource.setImages(new ArrayList<>(request.getImages()));
        }
        if (request.getVideoUrl() != null) {
            resource.setVideoUrl(request.getVideoUrl());
        }
        if (request.getAudioUrl() != null) {
            resource.setAudioUrl(request.getAudioUrl());
        }
        if (request.getContent() != null) {
            resource.setContent(request.getContent());
        }
        if (request.getTags() != null) {
            resource.setTags(new ArrayList<>(request.getTags()));
        }
        if (request.getRegion() != null) {
            resource.setRegion(request.getRegion());
        }
        if (request.getLocation() != null) {
            CultureResource.Location location = new CultureResource.Location(
                    request.getLocation().getLat(),
                    request.getLocation().getLng(),
                    request.getLocation().getAddress()
            );
            resource.setLocation(location);
        }

        resource = cultureResourceRepository.save(resource);
        return mapToCultureResourceResponse(resource);
    }

    @Transactional
    public void deleteCultureResource(Long id) {
        CultureResource resource = cultureResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在"));
        cultureResourceRepository.delete(resource);
    }

    // ==================== 社区投稿管理 ====================

    @Transactional(readOnly = true)
    public PageResponse<CommunityPostResponse> getPosts(
            Integer page, Integer size, String keyword, String status) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CommunityPost> postPage;

        String trimmedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String trimmedStatus = (status != null && !status.trim().isEmpty()) ? status.trim() : null;

        // 根据参数情况选择不同的查询方法
        // 管理员审核页面应该显示所有状态的投稿，所以使用专门的查询方法
        if (trimmedStatus != null && trimmedKeyword != null) {
            // 同时有状态和关键字，使用组合查询
            postPage = communityPostRepository.findByStatusAndKeyword(trimmedStatus, trimmedKeyword, pageable);
        } else if (trimmedStatus != null) {
            // 只有状态
            postPage = communityPostRepository.findByStatus(trimmedStatus, pageable);
        } else if (trimmedKeyword != null) {
            // 只有关键字，查询所有状态的投稿
            postPage = communityPostRepository.findByKeywordForAdmin(trimmedKeyword, pageable);
        } else {
            // 都没有，查询所有状态的投稿
            postPage = communityPostRepository.findAllForAdmin(pageable);
        }

        List<CommunityPostResponse> responses = postPage.getContent().stream()
                .map(this::mapToCommunityPostResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, postPage.getTotalElements(), page, size);
    }

    @Transactional
    public void approvePost(Long id) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        post.setStatus("approved");
        communityPostRepository.save(post);
    }

    @Transactional
    public void rejectPost(Long id, String reason) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        post.setStatus("rejected");
        post.setRejectReason(reason);
        communityPostRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        CommunityPost post = communityPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("帖子不存在"));
        communityPostRepository.delete(post);
    }

    // ==================== 映射方法 ====================

    private UserInfoResponse mapToUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .bio(user.getBio())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private CultureResourceResponse mapToCultureResourceResponse(CultureResource resource) {
        List<String> images = resource.getImages() != null ? new ArrayList<>(resource.getImages()) : new ArrayList<>();
        List<String> tags = resource.getTags() != null ? new ArrayList<>(resource.getTags()) : new ArrayList<>();

        CultureResourceResponse.Location location = null;
        if (resource.getLocation() != null) {
            location = new CultureResourceResponse.Location(
                    resource.getLocation().getLat(),
                    resource.getLocation().getLng(),
                    resource.getLocation().getAddress()
            );
        }

        return CultureResourceResponse.builder()
                .id(resource.getId())
                .type(resource.getType())
                .resourceType("CULTURE")
                .title(resource.getTitle())
                .description(resource.getDescription())
                .cover(resource.getCover())
                .images(images)
                .videoUrl(resource.getVideoUrl())
                .audioUrl(resource.getAudioUrl())
                .content(resource.getContent())
                .tags(tags)
                .region(resource.getRegion())
                .location(location)
                .views(resource.getViews())
                .favorites(resource.getFavorites())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .category(resource.getType() != null ? resource.getType().name() : null)
                .build();
    }

    private CommunityPostResponse mapToCommunityPostResponse(CommunityPost post) {
        var author = post.getAuthor();
        CommunityPostResponse.AuthorInfo authorInfo = new CommunityPostResponse.AuthorInfo(
                author.getId(),
                author.getUsername(),
                author.getAvatar()
        );

        List<String> images = post.getImages() != null ? new ArrayList<>(post.getImages()) : new ArrayList<>();
        List<String> tags = post.getTags() != null ? new ArrayList<>(post.getTags()) : new ArrayList<>();

        return CommunityPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .images(images)
                .author(authorInfo)
                .tags(tags)
                .likes(post.getLikes())
                .comments(post.getComments())
                .views(post.getViews())
                .status(post.getStatus() != null ? post.getStatus() : "pending")
                .rejectReason(post.getRejectReason())
                .isLiked(false) // 管理员查看时不需要点赞状态
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    // ==================== 仪表板数据 ====================

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long userCount = userRepository.count();
        long cultureCount = cultureResourceRepository.count();
        long eventCount = eventRepository.count();
        long postCount = communityPostRepository.count();

        return new DashboardStatsResponse(userCount, cultureCount, eventCount, postCount);
    }

    @Transactional(readOnly = true)
    public List<CommunityPostResponse> getPendingPosts(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit != null ? limit : 10);
        Page<CommunityPost> postPage = communityPostRepository.findByStatus("pending", pageable);

        return postPage.getContent().stream()
                .map(this::mapToCommunityPostResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getOngoingEvents(Integer limit) {
        // 先更新活动状态
        updateEventStatuses();

        List<Event> events = eventRepository.findByStatus(Event.EventStatus.ONGOING);
        int size = limit != null ? Math.min(limit, events.size()) : events.size();

        return events.stream()
                .limit(size)
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RouteAnalyticsResponse getRouteAnalytics() {
        List<Route> routes = routeRepository.findAll();
        long total = routes.size();

        RouteAnalyticsResponse.Summary summary = RouteAnalyticsResponse.Summary.builder()
                .totalRoutes(total)
                .avgDuration(calculateAverageDuration(routes))
                .avgDistance(calculateAverageDistance(routes))
                .geoCoverageRate(total == 0 ? 0 : (double) countRoutesWithGeo(routes) / total)
                .budgetCoverageRate(total == 0 ? 0 : (double) countRoutesWithBudget(routes) / total)
                .generatedAt(LocalDateTime.now())
                .build();

        return RouteAnalyticsResponse.builder()
                .summary(summary)
                .themeStats(buildThemeStats(routes, total))
                .durationPreferences(buildDurationPreferences(routes, total))
                .budgetPreferences(buildBudgetPreferences(routes, total))
                .heatmapPoints(buildHeatmapPoints(routes))
                .build();
    }

    private EventResponse mapToEventResponse(Event event) {
        List<String> images = event.getImages() != null ? new ArrayList<>(event.getImages()) : new ArrayList<>();
        List<String> videos = event.getVideos() != null ? new ArrayList<>(event.getVideos()) : new ArrayList<>();

        EventResponse.EventLocation location = null;
        if (event.getLocation() != null) {
            location = new EventResponse.EventLocation(
                    event.getLocation().getName(),
                    event.getLocation().getAddress(),
                    event.getLocation().getLat(),
                    event.getLocation().getLng()
            );
        }

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .cover(event.getCover())
                .type(event.getType())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(location)
                .capacity(event.getCapacity())
                .registered(event.getRegistered())
                .price(event.getPrice())
                .status(event.getStatus())
                .createdAt(event.getCreatedAt())
                .images(images)
                .videos(videos)
                .isRegistered(false) // 仪表板不需要用户报名状态
                .build();
    }

    /**
     * 更新活动状态
     */
    @Transactional
    public void updateEventStatuses() {
        java.time.LocalDate today = java.time.LocalDate.now();
        List<Event> events = eventRepository.findAll();

        for (Event event : events) {
            Event.EventStatus calculatedStatus = calculateEventStatus(event, today);
            if (event.getStatus() != calculatedStatus) {
                event.setStatus(calculatedStatus);
                eventRepository.save(event);
            }
        }
    }

    private Event.EventStatus calculateEventStatus(Event event, java.time.LocalDate today) {
        java.time.LocalDate startDate = event.getStartDate();
        java.time.LocalDate endDate = event.getEndDate();

        if (endDate.isBefore(today)) {
            return Event.EventStatus.PAST;
        }
        if (startDate.isAfter(today)) {
            return Event.EventStatus.UPCOMING;
        }
        return Event.EventStatus.ONGOING;
    }

    private double calculateAverageDuration(List<Route> routes) {
        return routes.stream()
                .map(Route::getDuration)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    private double calculateAverageDistance(List<Route> routes) {
        return routes.stream()
                .map(Route::getDistance)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private long countRoutesWithGeo(List<Route> routes) {
        return routes.stream()
                .filter(route -> resolveCoordinates(route) != null)
                .count();
    }

    private long countRoutesWithBudget(List<Route> routes) {
        return routes.stream()
                .filter(route -> route.getEstimatedBudget() != null)
                .count();
    }

    private List<RouteAnalyticsResponse.ThemeStat> buildThemeStats(List<Route> routes, long total) {
        Map<String, Long> themeCount = routes.stream()
                .collect(Collectors.groupingBy(route -> {
                    String theme = route.getTheme();
                    return (theme != null && !theme.isBlank()) ? theme : "other";
                }, Collectors.counting()));

        return themeCount.entrySet().stream()
                .map(entry -> RouteAnalyticsResponse.ThemeStat.builder()
                        .theme(entry.getKey())
                        .label(getThemeLabel(entry.getKey()))
                        .count(entry.getValue())
                        .percent(total == 0 ? 0 : (double) entry.getValue() / total)
                        .build())
                .sorted(Comparator.comparingLong(RouteAnalyticsResponse.ThemeStat::getCount).reversed())
                .collect(Collectors.toList());
    }

    private List<RouteAnalyticsResponse.PreferenceStat> buildDurationPreferences(List<Route> routes, long total) {
        long shortTrips = 0;
        long mediumTrips = 0;
        long deepTrips = 0;
        long longTrips = 0;
        long unknown = 0;

        for (Route route : routes) {
            Integer duration = route.getDuration();
            if (duration == null || duration <= 0) {
                unknown++;
            } else if (duration <= 3) {
                shortTrips++;
            } else if (duration <= 6) {
                mediumTrips++;
            } else if (duration <= 9) {
                deepTrips++;
            } else {
                longTrips++;
            }
        }

        List<RouteAnalyticsResponse.PreferenceStat> stats = new ArrayList<>();
        stats.add(buildPreferenceStat("1-3天轻松游", shortTrips, total));
        stats.add(buildPreferenceStat("4-6天经典线", mediumTrips, total));
        stats.add(buildPreferenceStat("7-9天深度游", deepTrips, total));
        stats.add(buildPreferenceStat("10天以上探索线", longTrips, total));
        if (unknown > 0) {
            stats.add(buildPreferenceStat("未设置", unknown, total));
        }
        return stats;
    }

    private List<RouteAnalyticsResponse.PreferenceStat> buildBudgetPreferences(List<Route> routes, long total) {
        long economy = 0;
        long comfort = 0;
        long premium = 0;
        long luxury = 0;
        long unknown = 0;

        for (Route route : routes) {
            Double budget = resolveBudgetForStats(route);
            if (budget == null) {
                unknown++;
                continue;
            }
            if (budget <= 3000) {
                economy++;
            } else if (budget <= 6000) {
                comfort++;
            } else if (budget <= 10000) {
                premium++;
            } else {
                luxury++;
            }
        }

        List<RouteAnalyticsResponse.PreferenceStat> stats = new ArrayList<>();
        stats.add(buildPreferenceStat("≤3000元", economy, total));
        stats.add(buildPreferenceStat("3000-6000元", comfort, total));
        stats.add(buildPreferenceStat("6000-10000元", premium, total));
        stats.add(buildPreferenceStat("≥10000元", luxury, total));
        if (unknown > 0) {
            stats.add(buildPreferenceStat("未设置", unknown, total));
        }
        return stats;
    }

    private List<RouteAnalyticsResponse.HeatmapPoint> buildHeatmapPoints(List<Route> routes) {
        return routes.stream()
                .sorted(Comparator.comparingDouble(this::calculateIntensity).reversed())
                .limit(10)
                .map(route -> {
                    double[] coordinate = resolveCoordinates(route);
                    return RouteAnalyticsResponse.HeatmapPoint.builder()
                            .routeId(route.getId())
                            .routeTitle(route.getTitle())
                            .theme(route.getTheme())
                            .duration(route.getDuration())
                            .startLocation(route.getStartLocation())
                            .endLocation(route.getEndLocation())
                            .views(route.getViews())
                            .favorites(route.getFavorites())
                            .intensity(calculateIntensity(route))
                            .lat(coordinate != null ? coordinate[0] : null)
                            .lng(coordinate != null ? coordinate[1] : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private double[] resolveCoordinates(Route route) {
        if (route.getItinerary() != null) {
            List<Route.ItineraryItem.Location> locations = route.getItinerary().stream()
                    .filter(item -> item.getLocations() != null)
                    .flatMap(item -> item.getLocations().stream())
                    .filter(loc -> loc.getLat() != null && loc.getLng() != null)
                    .collect(Collectors.toList());
            if (!locations.isEmpty()) {
                double avgLat = locations.stream().mapToDouble(Route.ItineraryItem.Location::getLat).average().orElse(0.0);
                double avgLng = locations.stream().mapToDouble(Route.ItineraryItem.Location::getLng).average().orElse(0.0);
                return new double[]{avgLat, avgLng};
            }
        }

        double[] start = findCityCoordinate(route.getStartLocation());
        if (start != null) {
            return start;
        }
        return findCityCoordinate(route.getEndLocation());
    }

    private double[] findCityCoordinate(String location) {
        if (location == null || location.isBlank()) {
            return null;
        }
        return CITY_COORDINATES.entrySet().stream()
                .filter(entry -> location.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private double calculateIntensity(Route route) {
        double views = route.getViews() != null ? route.getViews() : 0;
        double favorites = route.getFavorites() != null ? route.getFavorites() : 0;
        return views * 0.6 + favorites * 1.0;
    }

    private RouteAnalyticsResponse.PreferenceStat buildPreferenceStat(String label, long count, long total) {
        return RouteAnalyticsResponse.PreferenceStat.builder()
                .label(label)
                .count(count)
                .percent(total == 0 ? 0 : (double) count / Math.max(total, 1))
                .build();
    }

    private String getThemeLabel(String theme) {
        if (theme == null || theme.isBlank()) {
            return "其他主题";
        }
        return THEME_LABELS.getOrDefault(theme, theme);
    }

    private Double resolveBudgetForStats(Route route) {
        if (route.getEstimatedBudget() != null && route.getEstimatedBudget() > 0) {
            return route.getEstimatedBudget();
        }
        return fallbackBudget(route);
    }

    private Double fallbackBudget(Route route) {
        Integer duration = route.getDuration();
        Double distance = route.getDistance();
        if (duration == null && distance == null) {
            return null;
        }

        double durationFactor = duration != null ? duration : 5;
        double distanceFactor = distance != null ? distance : 300.0;
        double estimated = durationFactor * 800 + distanceFactor * 2;
        return Math.round(Math.max(estimated, 1500) / 100.0) * 100.0;
    }

    // ==================== 内部响应类 ====================

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class DashboardStatsResponse {
        private long users;
        private long culture;
        private long events;
        private long posts;
    }
}




