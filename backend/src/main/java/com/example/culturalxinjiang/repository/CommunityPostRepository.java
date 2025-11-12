package com.example.culturalxinjiang.repository;

import com.example.culturalxinjiang.entity.CommunityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    
    @Query("SELECT p FROM CommunityPost p ORDER BY p.createdAt DESC")
    Page<CommunityPost> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT p FROM CommunityPost p ORDER BY p.likes DESC, p.comments DESC")
    Page<CommunityPost> findAllOrderByHot(Pageable pageable);
    
    Page<CommunityPost> findByTitleContainingOrContentContaining(
            String title, String content, Pageable pageable);
}



