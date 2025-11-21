package com.example.culturalxinjiang.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiExplainResponse {
    private String title;
    private String summary;
    private List<String> highlights;
    private List<String> references;
    private String mediaInsight;
}

















