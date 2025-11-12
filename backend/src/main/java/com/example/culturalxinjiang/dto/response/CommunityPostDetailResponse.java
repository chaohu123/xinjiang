package com.example.culturalxinjiang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CommunityPostDetailResponse extends CommunityPostResponse {
    private List<CommentResponse> commentList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentResponse {
        private Long id;
        private String content;
        private AuthorInfo author;
        private LocalDateTime createdAt;
        private List<CommentResponse> replies;
    }
}

