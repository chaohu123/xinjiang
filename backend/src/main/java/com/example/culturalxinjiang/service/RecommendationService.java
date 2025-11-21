package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.response.HomeResourceResponse;
import com.example.culturalxinjiang.entity.CultureResource;
import com.example.culturalxinjiang.entity.Favorite;
import com.example.culturalxinjiang.entity.User;
import com.example.culturalxinjiang.repository.CultureResourceRepository;
import com.example.culturalxinjiang.repository.FavoriteRepository;
import com.example.culturalxinjiang.repository.UserRepository;
import com.example.culturalxinjiang.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final CultureResourceRepository cultureResourceRepository;
    private final HomeRecommendationService homeRecommendationService;

    @Transactional(readOnly = true)
    public List<HomeResourceResponse> getRecommendations(Integer limit) {
        int resolvedLimit = limit != null ? Math.min(Math.max(limit, 3), 12) : 6;
        User user = SecurityUtils.getCurrentUsername()
                .flatMap(username -> userRepository.findByUsername(username))
                .orElse(null);

        if (user == null) {
            return homeRecommendationService.getFeaturedResources(resolvedLimit);
        }

        List<Favorite> favorites = favoriteRepository
                .findTop50ByUserIdAndResourceTypeOrderByCreatedAtDesc(user.getId(), Favorite.ResourceType.CULTURE);
        if (favorites.isEmpty()) {
            return homeRecommendationService.getHotResources(resolvedLimit);
        }

        Set<String> tags = new HashSet<>();
        for (Favorite favorite : favorites) {
            cultureResourceRepository.findById(favorite.getResourceId())
                    .ifPresent(resource -> {
                        if (resource.getTags() != null) {
                            tags.addAll(resource.getTags());
                        }
                    });
        }

        if (tags.isEmpty()) {
            return homeRecommendationService.getHotResources(resolvedLimit);
        }

        List<CultureResource> matched = cultureResourceRepository.findByTags(
                new ArrayList<>(tags),
                PageRequest.of(0, resolvedLimit * 2)
        ).getContent();

        List<HomeResourceResponse> responses = matched.stream()
                .map(this::mapCultureResourceToResponse)
                .collect(Collectors.toList());

        if (responses.size() < resolvedLimit) {
            List<HomeResourceResponse> fallback = homeRecommendationService.getHotResources(resolvedLimit);
            for (HomeResourceResponse resource : fallback) {
                if (responses.stream().noneMatch(r -> r.getId().equals(resource.getId())
                        && r.getSource().equals(resource.getSource()))) {
                    responses.add(resource);
                }
                if (responses.size() >= resolvedLimit) {
                    break;
                }
            }
        }

        if (responses.size() > resolvedLimit) {
            return responses.subList(0, resolvedLimit);
        }
        return responses;
    }

    private HomeResourceResponse mapCultureResourceToResponse(CultureResource resource) {
        List<String> images = resource.getImages() != null ? new ArrayList<>(resource.getImages()) : new ArrayList<>();
        List<String> tags = resource.getTags() != null ? new ArrayList<>(resource.getTags()) : new ArrayList<>();
        return HomeResourceResponse.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .cover(resource.getCover())
                .images(images)
                .content(resource.getContent())
                .tags(tags)
                .region(resource.getRegion())
                .views(resource.getViews())
                .favorites(resource.getFavorites())
                .createdAt(resource.getCreatedAt())
                .updatedAt(resource.getUpdatedAt())
                .source("CULTURE_RESOURCE")
                .resourceType(resource.getType().name())
                .build();
    }
}

















