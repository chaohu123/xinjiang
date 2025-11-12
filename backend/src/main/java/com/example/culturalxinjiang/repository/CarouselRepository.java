package com.example.culturalxinjiang.repository;

import com.example.culturalxinjiang.entity.Carousel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarouselRepository extends JpaRepository<Carousel, Long> {
    List<Carousel> findByEnabledTrueOrderByOrderAsc();
}




