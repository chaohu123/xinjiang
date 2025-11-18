package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.MapPoiResponse;
import com.example.culturalxinjiang.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @GetMapping("/pois")
    public ApiResponse<MapPoiResponse> getMapPois(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "all") String category,
            @RequestParam(required = false) Double north,
            @RequestParam(required = false) Double south,
            @RequestParam(required = false) Double east,
            @RequestParam(required = false) Double west,
            @RequestParam(required = false, defaultValue = "false") boolean cluster,
            @RequestParam(required = false) Double zoom,
            @RequestParam(required = false, defaultValue = "500") Integer limit
    ) {
        MapService.Bounds bounds = (north != null || south != null || east != null || west != null)
                ? new MapService.Bounds(north, south, east, west)
                : null;
        Integer zoomValue = zoom != null ? (int) Math.round(zoom) : null;
        MapPoiResponse response = mapService.getPois(keyword, category, bounds, cluster, zoomValue, limit);
        return ApiResponse.success(response);
    }
}


