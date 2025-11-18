package com.example.culturalxinjiang.repository;

import com.example.culturalxinjiang.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
    long countByPostId(Long postId);
    List<Comment> findByAuthorId(Long authorId);
    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);
}






