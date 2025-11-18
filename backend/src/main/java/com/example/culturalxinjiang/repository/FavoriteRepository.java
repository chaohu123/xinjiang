package com.example.culturalxinjiang.repository;

import com.example.culturalxinjiang.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndResourceTypeAndResourceId(
            Long userId,
            Favorite.ResourceType resourceType,
            Long resourceId
    );

    boolean existsByUserIdAndResourceTypeAndResourceId(
            Long userId,
            Favorite.ResourceType resourceType,
            Long resourceId
    );

    Page<Favorite> findByUserId(Long userId, Pageable pageable);

    Page<Favorite> findByUserIdAndResourceTypeIn(Long userId, List<Favorite.ResourceType> resourceTypes, Pageable pageable);

    void deleteByUserIdAndResourceTypeAndResourceId(
            Long userId,
            Favorite.ResourceType resourceType,
            Long resourceId
    );

    java.util.List<Favorite> findTop50ByUserIdAndResourceTypeOrderByCreatedAtDesc(
            Long userId,
            Favorite.ResourceType resourceType
    );
}






