package com.example.culturalxinjiang.repository;

import com.example.culturalxinjiang.entity.HomeRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomeRecommendationRepository extends JpaRepository<HomeRecommendation, Long> {

    List<HomeRecommendation> findByTypeAndEnabledTrueOrderByDisplayOrderAsc(HomeRecommendation.RecommendationType type);

    List<HomeRecommendation> findByType(HomeRecommendation.RecommendationType type);

    boolean existsByTypeAndResourceIdAndSource(
            HomeRecommendation.RecommendationType type,
            Long resourceId,
            HomeRecommendation.ResourceSource source
    );
}



























<<<<<<< HEAD


=======
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
