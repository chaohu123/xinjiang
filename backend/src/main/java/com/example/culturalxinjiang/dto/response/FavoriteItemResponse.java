package com.example.culturalxinjiang.dto.response;

import com.example.culturalxinjiang.entity.Favorite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteItemResponse {
    private Long id;
    private Favorite.ResourceType resourceType;
    private String title;
    private String description;
    private String cover;
    private String region;
    private String type;
    private Integer favorites;
    private String startLocation;
    private String endLocation;
    private Integer duration;
    private Double distance;
    private String theme;
}










