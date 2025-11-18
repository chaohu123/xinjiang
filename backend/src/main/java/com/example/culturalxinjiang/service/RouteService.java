package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.request.GenerateRouteRequest;
import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.dto.response.RouteDetailResponse;
import com.example.culturalxinjiang.dto.response.RouteResponse;
import com.example.culturalxinjiang.entity.CultureResource;
import com.example.culturalxinjiang.entity.Route;
import com.example.culturalxinjiang.entity.User;
import com.example.culturalxinjiang.repository.CultureResourceRepository;
import com.example.culturalxinjiang.repository.RouteRepository;
import com.example.culturalxinjiang.repository.UserRepository;
import com.example.culturalxinjiang.service.AIService.AIRouteResponse;
import com.example.culturalxinjiang.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final CultureResourceRepository cultureResourceRepository;
    private final UserRepository userRepository;
    private final AIService aiService;

    @Value("${app.routes.default-cover:/digital-images/route.jpg}")
    private String defaultRouteCover;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public PageResponse<RouteResponse> getRoutes(String theme, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Route> routePage;

        if (theme != null && !theme.isEmpty()) {
            routePage = routeRepository.findByTheme(theme, pageable);
        } else {
            routePage = routeRepository.findAll(pageable);
        }

        List<RouteResponse> responses = routePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, routePage.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public PageResponse<RouteResponse> getMyRoutes(Integer page, Integer size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Route> routePage = routeRepository.findByUserId(user.getId(), pageable);

        List<RouteResponse> responses = routePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, routePage.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public RouteDetailResponse getRouteDetail(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("路线不存在"));
        return mapToDetailResponse(route);
    }

    // 用于存储调试信息的内部类
    public static class RouteGenerationResult {
        private RouteDetailResponse route;
        private ApiResponse.DebugInfo debugInfo;

        public RouteGenerationResult(RouteDetailResponse route, ApiResponse.DebugInfo debugInfo) {
            this.route = route;
            this.debugInfo = debugInfo;
        }

        public RouteDetailResponse getRoute() {
            return route;
        }

        public ApiResponse.DebugInfo getDebugInfo() {
            return debugInfo;
        }
    }

    @Transactional
    public RouteGenerationResult generateRouteWithDebug(GenerateRouteRequest request) {
        // 处理目的地：从 destinations 中提取起点和终点
        String startLocation = request.getStartLocation();
        String endLocation = request.getEndLocation();

        if (request.getDestinations() != null && !request.getDestinations().isEmpty()) {
            String destinations = request.getDestinations();
            // 支持逗号、中文逗号或箭头分隔
            String[] cities = destinations.split("[,，→]|->");
            if (cities.length > 0) {
                startLocation = cities[0].trim();
                if (cities.length > 1) {
                    endLocation = cities[cities.length - 1].trim();
                } else {
                    endLocation = startLocation; // 单城市路线
                }
            }
        }

        // 处理预算：优先使用 totalBudget，否则使用 budget
        Double budget = request.getTotalBudget() != null ? request.getTotalBudget() : request.getBudget();
        if (budget == null && request.getDailyBudget() != null && request.getDuration() != null) {
            budget = request.getDailyBudget() * request.getDuration();
        }

        log.info("开始生成路线：目的地={}, 天数={}, 人数={}, 预算={}",
            request.getDestinations(), request.getDuration(), request.getPeopleCount(), budget);

        // 调用AI服务生成路线（记录开始时间）
        long aiCallStartTime = System.currentTimeMillis();
        AIRouteResponse aiResponse;
        String aiProvider = "unknown";
        boolean aiCallSuccess = false;
        String aiError = null;

        try {
            aiResponse = aiService.generateRoute(request);
            aiCallSuccess = true;
            aiProvider = aiService.getProvider(); // 需要添加getter方法
            log.info("DeepSeek API调用成功，Provider: {}", aiProvider);
        } catch (Exception e) {
            aiCallSuccess = false;
            aiError = e.getMessage();
            log.error("DeepSeek API调用失败: {}", aiError, e);
            throw e; // 重新抛出异常
        }

        long aiCallEndTime = System.currentTimeMillis();
        long aiCallDuration = aiCallEndTime - aiCallStartTime;

        // 构建路线主题（基于风格偏好或兴趣标签）
        String theme = "general";
        if (request.getStylePreferences() != null && !request.getStylePreferences().isEmpty()) {
            theme = String.join(",", request.getStylePreferences());
        } else if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            theme = String.join(",", request.getInterests());
        }

        // 获取当前登录用户
        User user = getCurrentUser();

        // 创建Route实体
        Route route = Route.builder()
                .title(aiResponse.getTitle())
                .description(aiResponse.getDescription())
                .theme(theme)
                .duration(request.getDuration())
                .distance(calculateDistance(startLocation, endLocation))
                .startLocation(startLocation)
                .endLocation(endLocation)
                .waypoints(0)
                .views(0)
                .favorites(0)
                .user(user) // 关联当前用户
                .build();

        route.setCover(resolveRouteCover(route.getCover()));

        // 转换AI返回的行程安排为Route.ItineraryItem
        List<Route.ItineraryItem> itineraryItems = new ArrayList<>();
        if (aiResponse.getItinerary() != null) {
            for (AIRouteResponse.ItineraryItem aiItem : aiResponse.getItinerary()) {
                Route.ItineraryItem item = new Route.ItineraryItem();
                item.setRoute(route);
                item.setDay(aiItem.getDay());
                item.setTitle(aiItem.getTitle());

                // 合并所有信息到 description 中（因为数据库只有 description 字段）
                StringBuilder descBuilder = new StringBuilder();
                if (aiItem.getDescription() != null && !aiItem.getDescription().isEmpty()) {
                    descBuilder.append(aiItem.getDescription());
                }
                if (aiItem.getTimeSchedule() != null && !aiItem.getTimeSchedule().isEmpty()) {
                    descBuilder.append("\n\n【时间安排】\n").append(aiItem.getTimeSchedule());
                }
                if (aiItem.getTransportation() != null && !aiItem.getTransportation().isEmpty()) {
                    descBuilder.append("\n\n【交通信息】\n").append(aiItem.getTransportation());
                }
                if (aiItem.getDailyBudget() != null && !aiItem.getDailyBudget().isEmpty()) {
                    descBuilder.append("\n\n【每日预算】\n").append(aiItem.getDailyBudget());
                }
                item.setDescription(descBuilder.toString());

                // 转换地点信息（只保存有坐标的地点，因为数据库要求 lat/lng 不能为 null）
                if (aiItem.getLocations() != null) {
                    List<Route.ItineraryItem.Location> locations = aiItem.getLocations().stream()
                            .filter(aiLoc -> {
                                // 只保留有坐标的地点
                                if (aiLoc.getLat() == null || aiLoc.getLng() == null) {
                                    log.warn("跳过没有坐标的地点: {}", aiLoc.getName());
                                    return false;
                                }
                                return true;
                            })
                            .map(aiLoc -> {
                                Route.ItineraryItem.Location loc = new Route.ItineraryItem.Location();
                                loc.setName(aiLoc.getName());
                                loc.setLat(aiLoc.getLat());
                                loc.setLng(aiLoc.getLng());
                                loc.setDescription(aiLoc.getDescription());
                                return loc;
                            })
                            .collect(Collectors.toList());
                    item.setLocations(locations);
                } else {
                    item.setLocations(new ArrayList<>());
                }

                item.setAccommodation(aiItem.getAccommodation());
                item.setMeals(aiItem.getMeals() != null ? aiItem.getMeals() : new ArrayList<>());

                itineraryItems.add(item);
            }
        }
        route.setItinerary(itineraryItems);

        // 设置站点数量
        int waypointCount = itineraryItems.stream()
                .mapToInt(item -> item.getLocations() != null ? item.getLocations().size() : 0)
                .sum();
        route.setWaypoints(waypointCount);

        // 设置提示
        if (aiResponse.getTips() != null && !aiResponse.getTips().isEmpty()) {
            route.setTips(aiResponse.getTips());
        } else {
            route.setTips(new ArrayList<>());
        }

        // 保存路线到数据库
        route = routeRepository.save(route);

        log.info("路线生成成功，ID={}, 标题={}", route.getId(), route.getTitle());

        RouteDetailResponse response = mapToDetailResponse(route);

        // 构建调试信息
        ApiResponse.DebugInfo debugInfo = ApiResponse.DebugInfo.builder()
                .apiCallStatus(aiCallSuccess ? "成功" : "失败")
                .apiCallSuccess(aiCallSuccess)
                .apiResponseTime(aiCallDuration + "ms")
                .aiProvider(aiProvider)
                .apiError(aiError)
                .build();

        log.info("路线生成完成，DeepSeek API调用状态: {}, 耗时: {}ms",
                aiCallSuccess ? "成功" : "失败", aiCallDuration);

        return new RouteGenerationResult(response, debugInfo);
    }

    // 保持向后兼容的方法（不包含调试信息）
    @Transactional
    public RouteDetailResponse generateRoute(GenerateRouteRequest request) {
        return generateRouteWithDebug(request).getRoute();
    }

    private Double calculateDistance(String start, String end) {
        // Simplified distance calculation
        // In real implementation, use geocoding and distance calculation APIs
        return 100.0; // placeholder
    }

    private RouteResponse mapToResponse(Route route) {
        return RouteResponse.builder()
                .id(route.getId())
                .title(route.getTitle())
                .description(route.getDescription())
                .cover(resolveRouteCover(route.getCover()))
                .theme(route.getTheme())
                .duration(route.getDuration())
                .distance(route.getDistance())
                .startLocation(route.getStartLocation())
                .endLocation(route.getEndLocation())
                .waypoints(route.getWaypoints())
                .views(route.getViews())
                .favorites(route.getFavorites())
                .createdAt(route.getCreatedAt())
                .build();
    }

    private RouteDetailResponse mapToDetailResponse(Route route) {
        RouteDetailResponse response = new RouteDetailResponse();
        response.setId(route.getId());
        response.setTitle(route.getTitle());
        response.setDescription(route.getDescription());
        response.setCover(resolveRouteCover(route.getCover()));
        response.setTheme(route.getTheme());
        response.setDuration(route.getDuration());
        response.setDistance(route.getDistance());
        response.setStartLocation(route.getStartLocation());
        response.setEndLocation(route.getEndLocation());
        response.setWaypoints(route.getWaypoints());
        response.setViews(route.getViews());
        response.setFavorites(route.getFavorites());
        response.setCreatedAt(route.getCreatedAt());

        // 显式访问懒加载集合，确保在事务内加载
        List<Route.ItineraryItem> itinerary = route.getItinerary() != null
                ? new ArrayList<>(route.getItinerary())
                : new ArrayList<>();
        response.setItinerary(itinerary.stream()
                .map(item -> {
                    RouteDetailResponse.ItineraryItem dtoItem = new RouteDetailResponse.ItineraryItem();
                    dtoItem.setDay(item.getDay());
                    dtoItem.setTitle(item.getTitle());

                    // 从 description 中解析出各个部分
                    String fullDescription = item.getDescription();
                    if (fullDescription != null) {
                        // 分离描述、时间安排、交通信息和预算
                        String[] parts = fullDescription.split("\n\n【");
                        String description = parts.length > 0 ? parts[0].trim() : "";
                        String timeSchedule = null;
                        String transportation = null;
                        String dailyBudget = null;

                        for (int i = 1; i < parts.length; i++) {
                            String part = "【" + parts[i];
                            if (part.startsWith("【时间安排】")) {
                                timeSchedule = part.substring("【时间安排】\n".length()).trim();
                            } else if (part.startsWith("【交通信息】")) {
                                transportation = part.substring("【交通信息】\n".length()).trim();
                            } else if (part.startsWith("【每日预算】")) {
                                dailyBudget = part.substring("【每日预算】\n".length()).trim();
                            } else {
                                // 如果还有其他部分，保留在描述中
                                description += "\n\n" + part;
                            }
                        }

                        dtoItem.setDescription(description);
                        dtoItem.setTimeSchedule(timeSchedule);
                        dtoItem.setTransportation(transportation);
                        dtoItem.setDailyBudget(dailyBudget);
                    } else {
                        dtoItem.setDescription("");
                    }

                    // 显式访问懒加载的 locations 集合，确保在事务内加载
                    List<Route.ItineraryItem.Location> locations = item.getLocations() != null
                            ? new ArrayList<>(item.getLocations())
                            : new ArrayList<>();
                    dtoItem.setLocations(locations.stream()
                            .map(loc -> new RouteDetailResponse.ItineraryItem.Location(
                                    loc.getName(), loc.getLat(), loc.getLng(), loc.getDescription()))
                            .collect(Collectors.toList()));

                    dtoItem.setAccommodation(item.getAccommodation());

                    // 显式访问懒加载的 meals 集合，确保在事务内加载
                    List<String> meals = item.getMeals() != null
                            ? new ArrayList<>(item.getMeals())
                            : new ArrayList<>();
                    dtoItem.setMeals(meals);

                    return dtoItem;
                })
                .collect(Collectors.toList()));

        // 显式访问懒加载集合，确保在事务内加载
        List<Route.RouteResource> resources = route.getResources() != null
                ? new ArrayList<>(route.getResources())
                : new ArrayList<>();
        response.setResources(resources.stream()
                .map(res -> {
                    RouteDetailResponse.RouteResource dtoRes = new RouteDetailResponse.RouteResource();
                    // 显式访问懒加载的 cultureResource，确保在事务内加载
                    CultureResource cultureResource = res.getCultureResource();
                    dtoRes.setId(cultureResource.getId());
                    dtoRes.setType(cultureResource.getType().name());
                    dtoRes.setTitle(cultureResource.getTitle());
                    dtoRes.setCover(cultureResource.getCover());
                    dtoRes.setOrder(res.getOrder());
                    return dtoRes;
                })
                .collect(Collectors.toList()));

        // 显式访问懒加载集合
        List<String> tips = route.getTips() != null
                ? new ArrayList<>(route.getTips())
                : new ArrayList<>();
        response.setTips(tips);
        return response;
    }

    private String resolveRouteCover(String cover) {
        String resolved = StringUtils.hasText(cover) ? cover : defaultRouteCover;

        if (!StringUtils.hasText(resolved)) {
            return defaultRouteCover;
        }

        if (isAbsoluteUrl(resolved)) {
            return resolved;
        }

        String normalizedPath = resolved.startsWith("/") ? resolved : "/" + resolved;
        String normalizedContextPath = normalizeContextPath();

        if (StringUtils.hasText(normalizedContextPath)
                && !normalizedPath.startsWith(normalizedContextPath + "/")
                && !normalizedPath.equals(normalizedContextPath)) {
            return normalizedContextPath + normalizedPath;
        }

        return normalizedPath;
    }

    private boolean isAbsoluteUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String lowerCase = value.toLowerCase();
        return lowerCase.startsWith("http://")
                || lowerCase.startsWith("https://")
                || lowerCase.startsWith("data:");
    }

    private String normalizeContextPath() {
        if (!StringUtils.hasText(contextPath) || "/".equals(contextPath)) {
            return "";
        }
        return contextPath.startsWith("/") ? contextPath : "/" + contextPath;
    }

    @Transactional
    public void deleteMyRoute(Long id) {
        User user = getCurrentUser();
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("路线不存在"));

        // 验证路线是否属于当前用户
        if (route.getUser() == null || !route.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("无权删除此路线");
        }

        routeRepository.delete(route);
        log.info("路线已删除，ID={}, 标题={}", route.getId(), route.getTitle());
    }

    private User getCurrentUser() {
        String username = SecurityUtils.getRequiredUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}

