package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.request.CarouselRequest;
import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.CarouselResponse;
import com.example.culturalxinjiang.service.CarouselService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/carousel")
@RequiredArgsConstructor
public class CarouselController {

    private final CarouselService carouselService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    // 获取所有启用的轮播图（公开）
    @GetMapping
    public ApiResponse<List<CarouselResponse>> getCarousels() {
        List<CarouselResponse> carousels = carouselService.getEnabledCarousels();
        return ApiResponse.success(carousels);
    }

    // 获取所有轮播图（管理员）
    @GetMapping("/all")
    public ApiResponse<List<CarouselResponse>> getAllCarousels() {
        List<CarouselResponse> carousels = carouselService.getAllCarousels();
        return ApiResponse.success(carousels);
    }

    // 创建轮播图（管理员）
    @PostMapping
    public ApiResponse<CarouselResponse> createCarousel(@Valid @RequestBody CarouselRequest request) {
        CarouselResponse response = carouselService.createCarousel(request);
        return ApiResponse.success(response);
    }

    // 更新轮播图（管理员）
    @PutMapping("/{id}")
    public ApiResponse<CarouselResponse> updateCarousel(
            @PathVariable Long id,
            @RequestBody CarouselRequest request) {
        CarouselResponse response = carouselService.updateCarousel(id, request);
        return ApiResponse.success(response);
    }

    // 删除轮播图（管理员）
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCarousel(@PathVariable Long id) {
        carouselService.deleteCarousel(id);
        return ApiResponse.success(null);
    }

    // 上传轮播图图片
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("文件不能为空");
        }

        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // 构建访问URL
            String url = contextPath + "/uploads/" + filename;

            Map<String, String> result = new HashMap<>();
            result.put("url", url);

            return ApiResponse.success(result);
        } catch (IOException e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }
}




