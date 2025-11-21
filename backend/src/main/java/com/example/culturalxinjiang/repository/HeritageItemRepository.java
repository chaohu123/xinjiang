package com.example.culturalxinjiang.repository;

import com.example.culturalxinjiang.entity.HeritageItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeritageItemRepository extends JpaRepository<HeritageItem, Long> {

    @Query("""
            SELECT h FROM HeritageItem h
            WHERE (:keyword IS NULL OR LOWER(h.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(h.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:region IS NULL OR h.region = :region)
              AND (:category IS NULL OR h.category = :category)
              AND (:heritageLevel IS NULL OR h.heritageLevel = :heritageLevel)
              AND (:tags IS NULL OR EXISTS (
                    SELECT tag FROM h.tags tag WHERE tag IN :tags
                ))
            """)
    Page<HeritageItem> search(
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("category") String category,
            @Param("heritageLevel") String heritageLevel,
            @Param("tags") List<String> tags,
            Pageable pageable
    );

    List<HeritageItem> findTop6ByFeaturedTrueOrderByViewsDesc();

    List<HeritageItem> findTop10ByOrderByViewsDesc();
}















<<<<<<< HEAD


=======
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
