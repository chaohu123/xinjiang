package com.example.culturalxinjiang.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class AiExplainRequest {
    private String query;
    private String imageUrl;
    private String context;
    private String audience;
    private String tone;
    /**
     * 输出长度：short / medium / detailed
     */
    private String length;
    private List<String> focusPoints;
}





