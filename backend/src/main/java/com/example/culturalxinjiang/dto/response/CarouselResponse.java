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
public class CarouselResponse {
    private Long id;
    private String image;
    private String title;
    private String subtitle;
    private String link;
    private String buttonText;
    private Integer order;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}




