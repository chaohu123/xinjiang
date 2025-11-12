package com.example.culturalxinjiang.repository;

import com.example.culturalxinjiang.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
    
    void deleteByUserIdAndResourceTypeAndResourceId(
            Long userId, 
            Favorite.ResourceType resourceType, 
            Long resourceId
    );
}






