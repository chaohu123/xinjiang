package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.request.CarouselRequest;
import com.example.culturalxinjiang.dto.response.CarouselResponse;
import com.example.culturalxinjiang.entity.Carousel;
import com.example.culturalxinjiang.repository.CarouselRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarouselService {

    private final CarouselRepository carouselRepository;

    public List<CarouselResponse> getAllCarousels() {
        return carouselRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CarouselResponse> getEnabledCarousels() {
        return carouselRepository.findByEnabledTrueOrderByOrderAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CarouselResponse createCarousel(CarouselRequest request) {
        Carousel carousel = Carousel.builder()
                .image(request.getImage())
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .link(request.getLink())
                .buttonText(request.getButtonText())
                .order(request.getOrder() != null ? request.getOrder() : 0)
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .build();
        Carousel saved = carouselRepository.save(carousel);
        return toResponse(saved);
    }

    public CarouselResponse updateCarousel(Long id, CarouselRequest request) {
        Carousel carousel = carouselRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("轮播图不存在"));

        if (request.getImage() != null) {
            carousel.setImage(request.getImage());
        }
        if (request.getTitle() != null) {
            carousel.setTitle(request.getTitle());
        }
        if (request.getSubtitle() != null) {
            carousel.setSubtitle(request.getSubtitle());
        }
        if (request.getLink() != null) {
            carousel.setLink(request.getLink());
        }
        if (request.getButtonText() != null) {
            carousel.setButtonText(request.getButtonText());
        }
        if (request.getOrder() != null) {
            carousel.setOrder(request.getOrder());
        }
        if (request.getEnabled() != null) {
            carousel.setEnabled(request.getEnabled());
        }

        Carousel updated = carouselRepository.save(carousel);
        return toResponse(updated);
    }

    @Transactional
    public void deleteCarousel(Long id) {
        if (!carouselRepository.existsById(id)) {
            throw new RuntimeException("轮播图不存在");
        }
        carouselRepository.deleteById(id);
    }

    private CarouselResponse toResponse(Carousel carousel) {
        return CarouselResponse.builder()
                .id(carousel.getId())
                .image(carousel.getImage())
                .title(carousel.getTitle())
                .subtitle(carousel.getSubtitle())
                .link(carousel.getLink())
                .buttonText(carousel.getButtonText())
                .order(carousel.getOrder())
                .enabled(carousel.getEnabled())
                .createdAt(carousel.getCreatedAt())
                .updatedAt(carousel.getUpdatedAt())
                .build();
    }
}




