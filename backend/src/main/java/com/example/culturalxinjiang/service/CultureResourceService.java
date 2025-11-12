package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.response.CultureResourceResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.entity.CultureResource;
import com.example.culturalxinjiang.repository.CultureResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CultureResourceService {

    private final CultureResourceRepository repository;

    @Transactional(readOnly = true)
    public PageResponse<CultureResourceResponse> search(String keyword, CultureResource.CultureType type,
                                                       String region, List<String> tags, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CultureResource> resourcePage;

        if (tags != null && !tags.isEmpty()) {
            resourcePage = repository.findByTags(tags, pageable);
        } else {
            resourcePage = repository.search(keyword, type, region, pageable);
        }

        List<CultureResourceResponse> responses = resourcePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, resourcePage.getTotalElements(), page, size);
    }

    @Transactional(readOnly = true)
    public CultureResourceResponse getDetail(CultureResource.CultureType type, Long id) {
        CultureResource resource = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在"));

        if (resource.getType() != type) {
            throw new RuntimeException("资源类型不匹配");
        }

        return mapToResponse(resource);
    }

    @Transactional(readOnly = true)
    public List<CultureResourceResponse> getHotResources(Integer limit) {
        List<CultureResource> resources = repository.findTop10ByOrderByViewsDesc();
        return resources.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CultureResourceResponse> getRecommendedResources(Integer limit) {
        List<CultureResource> resources = repository.findTop10ByOrderByFavoritesDesc();
        return resources.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementViews(Long id) {
        CultureResource resource = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("资源不存在"));
        resource.setViews(resource.getViews() + 1);
        repository.save(resource);
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
                .build();
    }
}


