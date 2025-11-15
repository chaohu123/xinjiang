package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.response.HomeResourceResponse;
import com.example.culturalxinjiang.entity.CommunityPost;
import com.example.culturalxinjiang.entity.CultureResource;
import com.example.culturalxinjiang.entity.HomeRecommendation;
import com.example.culturalxinjiang.repository.CommunityPostRepository;
import com.example.culturalxinjiang.repository.CultureResourceRepository;
import com.example.culturalxinjiang.repository.HomeRecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeRecommendationService {

    private final HomeRecommendationRepository recommendationRepository;
    private final CultureResourceRepository cultureResourceRepository;
    private final CommunityPostRepository communityPostRepository;

    /**
     * 获取精选推荐资源
     * 只返回已配置且启用的资源，最多6个
     */
    @Transactional(readOnly = true)
    public List<HomeResourceResponse> getFeaturedResources(Integer limit) {
        // 限制最多6个
        int maxLimit = Math.min(limit != null ? limit : 6, 6);

        List<HomeRecommendation> recommendations = recommendationRepository
                .findByTypeAndEnabledTrueOrderByDisplayOrderAsc(HomeRecommendation.RecommendationType.FEATURED);

        List<HomeResourceResponse> resources = new ArrayList<>();
        for (HomeRecommendation rec : recommendations) {
            if (resources.size() >= maxLimit) break;

            HomeResourceResponse resource = getResourceById(rec.getResourceId(), rec.getSource());
            if (resource != null) {
                resources.add(resource);
            }
        }

        return resources;
    }

    /**
     * 获取热门资源
     * 只返回已配置且启用的资源，最多6个
     */
    @Transactional(readOnly = true)
    public List<HomeResourceResponse> getHotResources(Integer limit) {
        // 限制最多6个
        int maxLimit = Math.min(limit != null ? limit : 6, 6);

        List<HomeRecommendation> recommendations = recommendationRepository
                .findByTypeAndEnabledTrueOrderByDisplayOrderAsc(HomeRecommendation.RecommendationType.HOT);

        List<HomeResourceResponse> resources = new ArrayList<>();
        for (HomeRecommendation rec : recommendations) {
            if (resources.size() >= maxLimit) break;

            HomeResourceResponse resource = getResourceById(rec.getResourceId(), rec.getSource());
            if (resource != null) {
                resources.add(resource);
            }
        }

        return resources;
    }

    /**
     * 根据ID和来源获取资源
     */
    private HomeResourceResponse getResourceById(Long resourceId, HomeRecommendation.ResourceSource source) {
        if (source == HomeRecommendation.ResourceSource.CULTURE_RESOURCE) {
            return cultureResourceRepository.findById(resourceId)
                    .map(this::mapCultureResourceToResponse)
                    .orElse(null);
        } else if (source == HomeRecommendation.ResourceSource.COMMUNITY_POST) {
            return communityPostRepository.findById(resourceId)
                    .filter(post -> "approved".equals(post.getStatus()))
                    .map(this::mapCommunityPostToResponse)
                    .orElse(null);
        }
        return null;
    }

    /**
     * 将文化资源转换为响应对象
     */
    private HomeResourceResponse mapCultureResourceToResponse(CultureResource resource) {
        List<String> images = resource.getImages() != null ? new ArrayList<>(resource.getImages()) : new ArrayList<>();
        List<String> tags = resource.getTags() != null ? new ArrayList<>(resource.getTags()) : new ArrayList<>();

        return HomeResourceResponse.builder()
                .id(resource.getId())
                .title(resource.getTitle())
                .description(resource.getDescription())
                .cover(resource.getCover())
                .images(images)
                .videoUrl(resource.getVideoUrl())
                .audioUrl(resource.getAudioUrl())
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

    /**
     * 将社区投稿转换为响应对象
     */
    private HomeResourceResponse mapCommunityPostToResponse(CommunityPost post) {
        List<String> images = post.getImages() != null ? new ArrayList<>(post.getImages()) : new ArrayList<>();
        List<String> tags = post.getTags() != null ? new ArrayList<>(post.getTags()) : new ArrayList<>();

        // 从内容中提取描述（前200个字符）
        String description = post.getContent() != null && post.getContent().length() > 200
                ? post.getContent().substring(0, 200) + "..."
                : post.getContent();

        // 使用第一张图片作为封面
        String cover = images.isEmpty() ? null : images.get(0);

        return HomeResourceResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(description)
                .cover(cover)
                .images(images)
                .content(post.getContent())
                .tags(tags)
                .views(post.getViews())
                .likes(post.getLikes())
                .comments(post.getComments())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .source("COMMUNITY_POST")
                .build();
    }

    /**
     * 获取所有推荐配置
     */
    @Transactional(readOnly = true)
    public List<HomeRecommendation> getAllRecommendations(HomeRecommendation.RecommendationType type) {
        if (type != null) {
            return recommendationRepository.findByType(type);
        }
        return recommendationRepository.findAll();
    }

    /**
     * 添加推荐配置
     * 限制每个类型最多6个配置
     */
    @Transactional
    public HomeRecommendation addRecommendation(HomeRecommendation.RecommendationType type,
                                                Long resourceId,
                                                HomeRecommendation.ResourceSource source,
                                                Integer displayOrder) {
        // 检查是否已存在
        if (recommendationRepository.existsByTypeAndResourceIdAndSource(type, resourceId, source)) {
            throw new RuntimeException("该资源已在此推荐类型中存在");
        }

        // 检查是否已达到最大配置数量（6个）
        List<HomeRecommendation> existingRecommendations = recommendationRepository.findByType(type);
        if (existingRecommendations.size() >= 6) {
            throw new RuntimeException("该推荐类型最多只能配置6个资源");
        }

        // 如果没有指定排序，设置为当前最大排序值+1
        if (displayOrder == null) {
            int maxOrder = existingRecommendations.stream()
                    .mapToInt(HomeRecommendation::getDisplayOrder)
                    .max()
                    .orElse(-1);
            displayOrder = maxOrder + 1;
        }

        HomeRecommendation recommendation = HomeRecommendation.builder()
                .type(type)
                .resourceId(resourceId)
                .source(source)
                .displayOrder(displayOrder)
                .enabled(true)
                .build();

        return recommendationRepository.save(recommendation);
    }

    /**
     * 删除推荐配置
     */
    @Transactional
    public void deleteRecommendation(Long id) {
        recommendationRepository.deleteById(id);
    }

    /**
     * 更新推荐配置
     */
    @Transactional
    public HomeRecommendation updateRecommendation(Long id, Integer displayOrder, Boolean enabled) {
        HomeRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("推荐配置不存在"));

        if (displayOrder != null) {
            recommendation.setDisplayOrder(displayOrder);
        }
        if (enabled != null) {
            recommendation.setEnabled(enabled);
        }

        return recommendationRepository.save(recommendation);
    }

    /**
     * 获取当前首页实际显示的资源（用于管理员查看）
     * 返回的资源包含是否已配置的信息
     * 只返回已配置的资源（包括启用和禁用的），按displayOrder排序
     */
    @Transactional(readOnly = true)
    public List<HomeResourceWithConfigInfo> getCurrentDisplayedResources(
            HomeRecommendation.RecommendationType type, Integer limit) {
        // 获取所有已配置的推荐（包括启用和禁用的），按displayOrder排序
        List<HomeRecommendation> allRecommendations = recommendationRepository
                .findByType(type)
                .stream()
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .collect(Collectors.toList());

        // 构建结果列表
        List<HomeResourceWithConfigInfo> result = new ArrayList<>();
        for (HomeRecommendation rec : allRecommendations) {
            HomeResourceResponse resource = getResourceById(rec.getResourceId(), rec.getSource());
            if (resource != null) {
                HomeResourceWithConfigInfo info = new HomeResourceWithConfigInfo();
                info.setResource(resource);
                info.setConfigured(true);
                info.setRecommendationId(rec.getId());
                info.setDisplayOrder(rec.getDisplayOrder());
                info.setEnabled(rec.getEnabled());
                result.add(info);
            }
        }

        return result;
    }

    /**
     * 资源与配置信息的包装类
     */
    public static class HomeResourceWithConfigInfo {
        private HomeResourceResponse resource;
        private Boolean configured;
        private Long recommendationId;
        private Integer displayOrder;
        private Boolean enabled;

        // Getters and Setters
        public HomeResourceResponse getResource() {
            return resource;
        }

        public void setResource(HomeResourceResponse resource) {
            this.resource = resource;
        }

        public Boolean getConfigured() {
            return configured;
        }

        public void setConfigured(Boolean configured) {
            this.configured = configured;
        }

        public Long getRecommendationId() {
            return recommendationId;
        }

        public void setRecommendationId(Long recommendationId) {
            this.recommendationId = recommendationId;
        }

        public Integer getDisplayOrder() {
            return displayOrder;
        }

        public void setDisplayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
        }

        public Boolean getEnabled() {
            return enabled;
        }

        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }
    }
}

