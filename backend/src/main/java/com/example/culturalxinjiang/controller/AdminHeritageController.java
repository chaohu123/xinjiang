package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.request.HeritageItemRequest;
import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.dto.response.HeritageItemResponse;
import com.example.culturalxinjiang.dto.response.PageResponse;
import com.example.culturalxinjiang.service.HeritageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/heritage")
@RequiredArgsConstructor
public class AdminHeritageController {

    private final HeritageService heritageService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @GetMapping
    public ApiResponse<PageResponse<HeritageItemResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String heritageLevel,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        PageResponse<HeritageItemResponse> response = heritageService.search(
                keyword, region, category, heritageLevel, null, page, size
        );
        return ApiResponse.success(response);
    }

    @PostMapping
    public ApiResponse<HeritageItemResponse> create(@RequestBody HeritageItemRequest request) {
        return ApiResponse.success(heritageService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<HeritageItemResponse> update(
            @PathVariable Long id,
            @RequestBody HeritageItemRequest request
    ) {
        return ApiResponse.success(heritageService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        heritageService.delete(id);
        return ApiResponse.success(null);
    }

    // 上传非遗资源图片或视频
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Map<String, String>> uploadMedia(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.startsWith("video/"))) {
            return ApiResponse.error("仅支持上传图片或视频文件");
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
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 构建访问URL
            String url = contextPath + "/uploads/" + filename;
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("type", contentType);

            return ApiResponse.success(result);
        } catch (IOException e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }
}









