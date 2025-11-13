package com.example.culturalxinjiang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostResponse {
    private Long id;
    private String title;
    private String content;
    private List<String> images;
    private AuthorInfo author;
    private List<String> tags;
    private Integer likes;
    private Integer comments;
    private Integer views;
    private String status;
    private Boolean isLiked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String username;
        private String avatar;
    }
}

