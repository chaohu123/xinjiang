package com.example.culturalxinjiang.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CarouselRequest {
    @NotBlank(message = "图片不能为空")
    private String image;

    private String title;

    private String subtitle;

    private String link;

    private String buttonText;

    private Integer order;

    private Boolean enabled;
}




