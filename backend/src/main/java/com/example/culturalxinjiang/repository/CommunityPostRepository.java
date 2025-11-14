package com.example.culturalxinjiang.repository;

import com.example.culturalxinjiang.entity.CommunityPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT p FROM CommunityPost p WHERE p.status = :status ORDER BY p.createdAt DESC")
    Page<CommunityPost> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT p FROM CommunityPost p WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:keyword IS NULL OR p.title LIKE CONCAT('%', :keyword, '%') OR p.content LIKE CONCAT('%', :keyword, '%')) " +
           "ORDER BY p.createdAt DESC")
    Page<CommunityPost> findByStatusAndKeyword(
            @Param("status") String status,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT p FROM CommunityPost p WHERE p.author.id = :userId ORDER BY p.createdAt DESC")
    Page<CommunityPost> findByAuthorId(@Param("userId") Long userId, Pageable pageable);
}



