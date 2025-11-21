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




















<<<<<<< HEAD


=======
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
