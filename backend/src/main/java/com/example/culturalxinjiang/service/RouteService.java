package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.request.GenerateRouteRequest;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.dto.response.RouteDetailResponse;
import com.example.culturalxinjiang.dto.response.RouteResponse;
import com.example.culturalxinjiang.entity.CultureResource;
import com.example.culturalxinjiang.entity.Route;
import com.example.culturalxinjiang.repository.CultureResourceRepository;
import com.example.culturalxinjiang.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final CultureResourceRepository cultureResourceRepository;

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

    public RouteDetailResponse getRouteDetail(Long id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("路线不存在"));
        return mapToDetailResponse(route);
    }

    @Transactional
    public RouteDetailResponse generateRoute(GenerateRouteRequest request) {
        // Simple route generation logic
        // In a real implementation, this would use algorithms to find best routes
        // based on start/end locations and interests

        Route route = Route.builder()
                .title("从" + request.getStartLocation() + "到" + request.getEndLocation() + "的路线")
                .description("根据您的兴趣生成的个性化路线")
                .theme(String.join(",", request.getInterests()))
                .duration(request.getDuration())
                .distance(calculateDistance(request.getStartLocation(), request.getEndLocation()))
                .startLocation(request.getStartLocation())
                .endLocation(request.getEndLocation())
                .waypoints(0)
                .views(0)
                .favorites(0)
                .build();

        // Find resources based on interests
        List<CultureResource> resources = cultureResourceRepository.findAll()
                .stream()
                .filter(r -> request.getInterests().stream().anyMatch(interest -> 
                    r.getTags().contains(interest) || r.getRegion().contains(interest)))
                .limit(10)
                .collect(Collectors.toList());

        // Create itinerary items
        for (int i = 0; i < request.getDuration() && i < resources.size(); i++) {
            Route.ItineraryItem item = new Route.ItineraryItem();
            item.setRoute(route);
            item.setDay(i + 1);
            item.setTitle("第" + (i + 1) + "天");
            item.setDescription("参观" + resources.get(i).getTitle());
            route.getItinerary().add(item);
        }

        route.setWaypoints(route.getItinerary().size());
        route = routeRepository.save(route);

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

        response.setItinerary(route.getItinerary().stream()
                .map(item -> {
                    RouteDetailResponse.ItineraryItem dtoItem = new RouteDetailResponse.ItineraryItem();
                    dtoItem.setDay(item.getDay());
                    dtoItem.setTitle(item.getTitle());
                    dtoItem.setDescription(item.getDescription());
                    dtoItem.setLocations(item.getLocations().stream()
                            .map(loc -> new RouteDetailResponse.ItineraryItem.Location(
                                    loc.getName(), loc.getLat(), loc.getLng(), loc.getDescription()))
                            .collect(Collectors.toList()));
                    dtoItem.setAccommodation(item.getAccommodation());
                    dtoItem.setMeals(item.getMeals());
                    return dtoItem;
                })
                .collect(Collectors.toList()));

        response.setResources(route.getResources().stream()
                .map(res -> {
                    RouteDetailResponse.RouteResource dtoRes = new RouteDetailResponse.RouteResource();
                    dtoRes.setId(res.getCultureResource().getId());
                    dtoRes.setType(res.getCultureResource().getType().name());
                    dtoRes.setTitle(res.getCultureResource().getTitle());
                    dtoRes.setCover(res.getCultureResource().getCover());
                    dtoRes.setOrder(res.getOrder());
                    return dtoRes;
                })
                .collect(Collectors.toList()));

        response.setTips(route.getTips());
        return response;
    }
}

