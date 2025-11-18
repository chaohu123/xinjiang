package com.example.culturalxinjiang.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class GenerateRouteRequest {
    // 目的地信息（支持多城市，按顺序）
    @NotBlank(message = "目的地不能为空")
    private String destinations; // 格式：城市1,城市2,城市3 或 城市1→城市2→城市3

    // 精确日期或大致时间
    private String travelDates; // 格式：2025-06-10 至 2025-06-16 或 2025年6月

    // 行程天数
    @NotNull(message = "行程天数不能为空")
    @Min(value = 1, message = "天数至少为1天")
    private Integer duration;

    // 到达/离开时段
    private String arrivalTime; // 到达时段，如：上午、下午、晚上
    private String departureTime; // 离开时段，如：上午、下午、晚上

    // 人员信息
    @NotNull(message = "人数不能为空")
    @Min(value = 1, message = "人数至少为1人")
    private Integer peopleCount;

    private String ageGroups; // 年龄段，如：2个成人,1个儿童(5岁),1个老人(70岁)
    private Boolean hasMobilityIssues; // 是否有行动不便者
    private String specialDietary; // 特殊饮食需求，如：素食、清真、无麸质

    // 风格与节奏偏好（多选）
    private List<String> stylePreferences; // 休闲/紧凑/深度文化/美食/摄影/徒步/购物/亲子

    // 预算信息
    private Double totalBudget; // 总预算
    private Double dailyBudget; // 每日预算
    private Boolean includesFlight; // 是否含机票

    // 住宿偏好
    private List<String> accommodationPreferences; // 区域、星级、民宿、Airbnb、靠近地铁等

    // 交通偏好或限制
    private List<String> transportationPreferences; // 自驾/公共交通/不想坐夜车/不租车

    // 必看/必须避开的景点或体验
    private List<String> mustVisit; // 必看景点或体验
    private List<String> mustAvoid; // 必须避开的景点或体验

    // 天气/季节敏感
    private String weatherSensitivity; // 如：不想在雨季徒步、避开高温天气

    // 所需输出格式
    private List<String> outputFormats; // 逐日时间表、Google Maps链接、CSV/JSON、预算明细、预订建议等

    // 额外服务
    private Boolean needRestaurantSuggestions; // 是否需要餐厅预订建议
    private Boolean needTicketSuggestions; // 是否需要门票预订建议
    private Boolean needTransportSuggestions; // 是否需要交通预订建议
    private Boolean needPackingList; // 是否需要打包清单
    private Boolean needSafetyTips; // 是否需要安全提示
    private Boolean needVisaInfo; // 是否需要签证/入境提醒

    // 保留旧字段以兼容（可选）
    private String startLocation; // 兼容旧版本
    private String endLocation; // 兼容旧版本
    private List<String> interests; // 兼容旧版本
    private Double budget; // 兼容旧版本
    private List<String> mustVisitLocations; // 兼容旧版本
}






