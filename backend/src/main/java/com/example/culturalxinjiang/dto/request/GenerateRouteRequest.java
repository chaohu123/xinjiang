package com.example.culturalxinjiang.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class GenerateRouteRequest {
    @NotBlank(message = "起点不能为空")
    private String startLocation;

    @NotBlank(message = "终点不能为空")
    private String endLocation;

    private List<String> interests;

    @NotNull(message = "人数不能为空")
    @Min(value = 1, message = "人数至少为1人")
    private Integer peopleCount;

    @NotNull(message = "预算不能为空")
    @Min(value = 0, message = "预算不能为负数")
    private Double budget;

    private List<String> mustVisitLocations;

    @NotNull(message = "时长不能为空")
    @Min(value = 1, message = "天数至少为1天")
    private Integer duration;
}






