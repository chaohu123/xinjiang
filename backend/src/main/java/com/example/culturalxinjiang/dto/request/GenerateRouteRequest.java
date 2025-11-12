package com.example.culturalxinjiang.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GenerateRouteRequest {
    @NotBlank(message = "起点不能为空")
    private String startLocation;

    @NotBlank(message = "终点不能为空")
    private String endLocation;

    private List<String> interests;

    @NotNull(message = "时长不能为空")
    private Integer duration;
}






