package com.example.culturalxinjiang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCommentResponse {
    private Long id;
    private String content;
    private Long postId;
    private String postTitle;
    private String postCover;
    private String postStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}




