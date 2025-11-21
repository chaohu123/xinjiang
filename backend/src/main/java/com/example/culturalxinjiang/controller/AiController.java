package com.example.culturalxinjiang.controller;

import com.example.culturalxinjiang.dto.request.AiExplainRequest;
import com.example.culturalxinjiang.dto.response.AiExplainResponse;
import com.example.culturalxinjiang.dto.response.ApiResponse;
import com.example.culturalxinjiang.service.AIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AIService aiService;

    @PostMapping("/explain")
    public ApiResponse<AiExplainResponse> explain(@RequestBody AiExplainRequest request) {
        return ApiResponse.success(aiService.explainCulture(request));
    }
}















<<<<<<< HEAD


=======
>>>>>>> d741338a73d40ed487e214d275739d8dd21ddf84
