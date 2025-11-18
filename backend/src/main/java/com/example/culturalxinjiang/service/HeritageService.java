package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.request.HeritageItemRequest;
import com.example.culturalxinjiang.dto.response.CultureResourceResponse;
import com.example.culturalxinjiang.dto.response.HeritageItemResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.entity.HeritageItem;
import com.example.culturalxinjiang.repository.HeritageItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HeritageService {

    private final HeritageItemRepository heritageItemRepository;

    @Transactional(readOnly = true)
    public PageResponse<HeritageItemResponse> search(
            String keyword,
            String region,
            String category,
            String heritageLevel,
            List<String> tags,
            Integer page,
            Integer size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<HeritageItem> heritagePage = heritageItemRepository.search(
                keyword, region, category, heritageLevel, tags, pageable
        );

        List<HeritageItemResponse> responses = heritagePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.of(responses, heritagePage.getTotalElements(), page, size);
    }

    @Transactional
    public HeritageItemResponse getDetail(Long id) {
        HeritageItem item = heritageItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("非遗内容不存在"));
        if (item.getViews() == null) {
            item.setViews(0);
        }
        item.setViews(item.getViews() + 1);
        return mapToResponse(heritageItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<HeritageItemResponse> getRecommendations(Integer limit) {
        int resolvedLimit = limit != null ? Math.min(limit, 6) : 6;
        List<HeritageItem> featured = heritageItemRepository.findTop6ByFeaturedTrueOrderByViewsDesc();
        if (featured.size() < resolvedLimit) {
            List<HeritageItem> popular = heritageItemRepository.findTop10ByOrderByViewsDesc();
            Map<Long, HeritageItem> merged = new HashMap<>();
            featured.forEach(item -> merged.put(item.getId(), item));
            for (HeritageItem item : popular) {
                if (merged.size() >= resolvedLimit) {
                    break;
                }
                merged.putIfAbsent(item.getId(), item);
            }
            return merged.values().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        return featured.stream()
                .limit(resolvedLimit)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public HeritageItemResponse create(HeritageItemRequest request) {
        HeritageItem item = HeritageItem.builder()
                .title(request.getTitle())
                .region(request.getRegion())
                .category(request.getCategory())
                .cover(request.getCover())
                .images(request.getImages() != null ? new ArrayList<>(request.getImages()) : new ArrayList<>())
                .description(request.getDescription())
                .content(request.getContent())
                .videoUrl(request.getVideoUrl())
                .heritageLevel(request.getHeritageLevel())
                .tags(request.getTags() != null ? new ArrayList<>(request.getTags()) : new ArrayList<>())
                .featured(Boolean.TRUE.equals(request.getFeatured()))
                .views(0)
                .build();
        item = heritageItemRepository.save(item);
        return mapToResponse(item);
    }

    @Transactional
    public HeritageItemResponse update(Long id, HeritageItemRequest request) {
        HeritageItem item = heritageItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("非遗内容不存在"));
        if (request.getTitle() != null) {
            item.setTitle(request.getTitle());
        }
        if (request.getRegion() != null) {
            item.setRegion(request.getRegion());
        }
        if (request.getCategory() != null) {
            item.setCategory(request.getCategory());
        }
        if (request.getCover() != null) {
            item.setCover(request.getCover());
        }
        if (request.getImages() != null) {
            item.setImages(new ArrayList<>(request.getImages()));
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getContent() != null) {
            item.setContent(request.getContent());
        }
        if (request.getVideoUrl() != null) {
            item.setVideoUrl(request.getVideoUrl());
        }
        if (request.getHeritageLevel() != null) {
            item.setHeritageLevel(request.getHeritageLevel());
        }
        if (request.getTags() != null) {
            item.setTags(new ArrayList<>(request.getTags()));
        }
        if (request.getFeatured() != null) {
            item.setFeatured(request.getFeatured());
        }
        item = heritageItemRepository.save(item);
        return mapToResponse(item);
    }

    @Transactional
    public void delete(Long id) {
        HeritageItem item = heritageItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("非遗内容不存在"));
        heritageItemRepository.delete(item);
    }

    @Transactional(readOnly = true)
    public List<CultureResourceResponse> highlightInSearch(
            String keyword,
            String region,
            List<String> tags,
            Integer size
    ) {
        int resolvedSize = size != null ? size : 6;
        PageResponse<HeritageItemResponse> page = search(keyword, region, null, null, tags, 1, resolvedSize);
        return page.getList().stream()
                .map(this::mapToCultureResponse)
                .collect(Collectors.toList());
    }

    private HeritageItemResponse mapToResponse(HeritageItem item) {
        return HeritageItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .region(item.getRegion())
                .category(item.getCategory())
                .cover(item.getCover() != null ? item.getCover() :
                        (!CollectionUtils.isEmpty(item.getImages()) ? item.getImages().get(0) : null))
                .images(item.getImages() != null ? new ArrayList<>(item.getImages()) : new ArrayList<>())
                .description(item.getDescription())
                .content(item.getContent())
                .videoUrl(item.getVideoUrl())
                .heritageLevel(item.getHeritageLevel())
                .tags(item.getTags() != null ? new ArrayList<>(item.getTags()) : new ArrayList<>())
                .featured(Boolean.TRUE.equals(item.getFeatured()))
                .views(item.getViews())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .resourceType("HERITAGE")
                .build();
    }

    private CultureResourceResponse mapToCultureResponse(HeritageItemResponse item) {
        return CultureResourceResponse.builder()
                .id(item.getId())
                .type(null)
                .title(item.getTitle())
                .description(item.getDescription())
                .cover(item.getCover())
                .images(item.getImages())
                .videoUrl(item.getVideoUrl())
                .content(item.getContent())
                .tags(item.getTags())
                .region(item.getRegion())
                .views(item.getViews())
                .favorites(0)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .resourceType("HERITAGE")
                .heritageLevel(item.getHeritageLevel())
                .category(item.getCategory())
                .build();
    }
}

