package com.example.culturalxinjiang.repository;

import com.example.culturalxinjiang.entity.CultureResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CultureResourceRepository extends JpaRepository<CultureResource, Long> {

    @Query("SELECT c FROM CultureResource c WHERE " +
           "(:keyword IS NULL OR c.title LIKE %:keyword% OR c.description LIKE %:keyword%) AND " +
           "(:type IS NULL OR c.type = :type) AND " +
           "(:region IS NULL OR c.region = :region)")
    Page<CultureResource> search(
            @Param("keyword") String keyword,
            @Param("type") CultureResource.CultureType type,
            @Param("region") String region,
            Pageable pageable
    );

    @Query("SELECT DISTINCT c FROM CultureResource c JOIN c.tags t WHERE t IN :tags")
    Page<CultureResource> findByTags(@Param("tags") List<String> tags, Pageable pageable);

    List<CultureResource> findTop10ByOrderByViewsDesc();
    List<CultureResource> findTop10ByOrderByFavoritesDesc();

    Page<CultureResource> findByType(CultureResource.CultureType type, Pageable pageable);
    Page<CultureResource> findByRegion(String region, Pageable pageable);

    @Query("SELECT c FROM CultureResource c WHERE c.location.lat IS NOT NULL AND c.location.lng IS NOT NULL")
    List<CultureResource> findAllWithLocation();
}






