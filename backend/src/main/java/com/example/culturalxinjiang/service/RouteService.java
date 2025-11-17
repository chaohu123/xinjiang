package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.request.GenerateRouteRequest;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public RouteDetailResponse generateRoute(GenerateRouteRequest request) {
        log.info("开始生成路线：起点={}, 终点={}, 天数={}, 人数={}, 预算={}",
            request.getStartLocation(), request.getEndLocation(),
            request.getDuration(), request.getPeopleCount(), request.getBudget());

        // 调用AI服务生成路线
        AIRouteResponse aiResponse = aiService.generateRoute(request);

        // 构建路线主题（基于兴趣标签）
        String theme = (request.getInterests() != null && !request.getInterests().isEmpty())
            ? String.join(",", request.getInterests())
            : "general";

        // 获取当前登录用户
        User user = getCurrentUser();

        // 创建Route实体
        Route route = Route.builder()
                .title(aiResponse.getTitle())
                .description(aiResponse.getDescription())
                .theme(theme)
                .duration(request.getDuration())
                .distance(calculateDistance(request.getStartLocation(), request.getEndLocation()))
                .startLocation(request.getStartLocation())
                .endLocation(request.getEndLocation())
                .waypoints(0)
                .views(0)
                .favorites(0)
                .user(user) // 关联当前用户
                .build();

        // 转换AI返回的行程安排为Route.ItineraryItem
        List<Route.ItineraryItem> itineraryItems = new ArrayList<>();
        if (aiResponse.getItinerary() != null) {
            for (AIRouteResponse.ItineraryItem aiItem : aiResponse.getItinerary()) {
                Route.ItineraryItem item = new Route.ItineraryItem();
                item.setRoute(route);
                item.setDay(aiItem.getDay());
                item.setTitle(aiItem.getTitle());
                item.setDescription(aiItem.getDescription());

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

        return mapToDetailResponse(route);
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
                .cover(route.getCover())
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
        response.setCover(route.getCover());
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
                    dtoItem.setDescription(item.getDescription());

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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("用户未登录");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
}

