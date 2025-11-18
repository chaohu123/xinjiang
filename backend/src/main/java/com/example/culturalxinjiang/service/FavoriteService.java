package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.response.CultureResourceResponse;
import com.example.culturalxinjiang.dto.response.FavoriteItemResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.entity.CultureResource;
import com.example.culturalxinjiang.entity.Favorite;
import com.example.culturalxinjiang.entity.Route;
import com.example.culturalxinjiang.entity.User;
import com.example.culturalxinjiang.repository.CommunityPostRepository;
import com.example.culturalxinjiang.repository.CultureResourceRepository;
import com.example.culturalxinjiang.repository.FavoriteRepository;
import com.example.culturalxinjiang.repository.RouteRepository;
import com.example.culturalxinjiang.repository.UserRepository;
import com.example.culturalxinjiang.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final CultureResourceRepository cultureResourceRepository;
    private final RouteRepository routeRepository;
    private final CommunityPostRepository communityPostRepository;
    @Value("${app.routes.default-cover:/digital-images/route.jpg}")
    private String defaultRouteCover;

    @Transactional
    public void favoriteResource(Favorite.ResourceType resourceType, Long resourceId) {
        User user = getCurrentUser();

        if (favoriteRepository.existsByUserIdAndResourceTypeAndResourceId(user.getId(), resourceType, resourceId)) {
            throw new RuntimeException("已经收藏过");
        }

        // Verify resource exists
        if (resourceType == Favorite.ResourceType.CULTURE) {
            cultureResourceRepository.findById(resourceId)
                    .orElseThrow(() -> new RuntimeException("资源不存在"));
            CultureResource resource = cultureResourceRepository.findById(resourceId).get();
            resource.setFavorites(resource.getFavorites() + 1);
            cultureResourceRepository.save(resource);
        } else if (resourceType == Favorite.ResourceType.ROUTE) {
            routeRepository.findById(resourceId)
                    .orElseThrow(() -> new RuntimeException("路线不存在"));
            Route route = routeRepository.findById(resourceId).get();
            route.setFavorites(route.getFavorites() + 1);
            routeRepository.save(route);
        } else if (resourceType == Favorite.ResourceType.POST) {
            communityPostRepository.findById(resourceId)
                    .orElseThrow(() -> new RuntimeException("帖子不存在"));
            // Posts don't have a favorites count field, so we just save the favorite
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .build();
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void unfavoriteResource(Favorite.ResourceType resourceType, Long resourceId) {
        User user = getCurrentUser();

        Favorite favorite = favoriteRepository.findByUserIdAndResourceTypeAndResourceId(
                user.getId(), resourceType, resourceId)
                .orElseThrow(() -> new RuntimeException("未收藏"));

        favoriteRepository.delete(favorite);

        // Update resource favorite count
        if (resourceType == Favorite.ResourceType.CULTURE) {
            CultureResource resource = cultureResourceRepository.findById(resourceId).get();
            resource.setFavorites(Math.max(0, resource.getFavorites() - 1));
            cultureResourceRepository.save(resource);
        } else if (resourceType == Favorite.ResourceType.ROUTE) {
            Route route = routeRepository.findById(resourceId).get();
            route.setFavorites(Math.max(0, route.getFavorites() - 1));
            routeRepository.save(route);
        } else if (resourceType == Favorite.ResourceType.POST) {
            // Posts don't have a favorites count field, so we just delete the favorite
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<FavoriteItemResponse> getFavorites(Integer page, Integer size) {
        User user = getCurrentUser();
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Favorite.ResourceType> resourceTypes = Arrays.asList(Favorite.ResourceType.CULTURE, Favorite.ResourceType.ROUTE);
        Page<Favorite> favoritePage = favoriteRepository.findByUserIdAndResourceTypeIn(user.getId(), resourceTypes, pageable);

        List<FavoriteItemResponse> responses = favoritePage.getContent().stream()
                .map(this::mapFavoriteItem)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return PageResponse.of(responses, favoritePage.getTotalElements(), page, size);
    }

    private FavoriteItemResponse mapFavoriteItem(Favorite favorite) {
        if (favorite.getResourceType() == Favorite.ResourceType.CULTURE) {
            return cultureResourceRepository.findById(favorite.getResourceId())
                    .map(this::mapCultureFavorite)
                    .orElse(null);
        }

        if (favorite.getResourceType() == Favorite.ResourceType.ROUTE) {
            return routeRepository.findById(favorite.getResourceId())
                    .map(this::mapRouteFavorite)
                    .orElse(null);
        }

        return null;
    }

    private FavoriteItemResponse mapCultureFavorite(CultureResource resource) {
        CultureResourceResponse response = mapToResponse(resource);
        return FavoriteItemResponse.builder()
                .id(response.getId())
                .resourceType(Favorite.ResourceType.CULTURE)
                .title(response.getTitle())
                .description(response.getDescription())
                .cover(response.getCover())
                .region(response.getRegion())
                .type(response.getType() != null ? response.getType().name().toLowerCase() : null)
                .favorites(response.getFavorites())
                .build();
    }

    private FavoriteItemResponse mapRouteFavorite(Route route) {
        return FavoriteItemResponse.builder()
                .id(route.getId())
                .resourceType(Favorite.ResourceType.ROUTE)
                .title(route.getTitle())
                .description(route.getDescription())
                .cover(resolveRouteCover(route.getCover()))
                .startLocation(route.getStartLocation())
                .endLocation(route.getEndLocation())
                .duration(route.getDuration())
                .distance(route.getDistance())
                .favorites(route.getFavorites())
                .theme(route.getTheme())
                .build();
    }

    private String resolveRouteCover(String cover) {
        if (cover == null || cover.isBlank()) {
            return defaultRouteCover;
        }
        return cover;
    }

    private User getCurrentUser() {
        String username = SecurityUtils.getRequiredUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private CultureResourceResponse mapToResponse(CultureResource resource) {
        // 显式访问懒加载集合，确保在事务内加载
        // 这会在 Session 开启时触发懒加载，避免序列化时 Session 已关闭的问题
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
}


