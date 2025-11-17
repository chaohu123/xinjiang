package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.request.GenerateRouteRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.provider:deepseek}")
    private String provider; // 可选: openai 或 deepseek

    // DeepSeek 配置
    @Value("${ai.deepseek.api-key:}")
    private String deepseekApiKey;

    @Value("${ai.deepseek.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String deepseekApiUrl;

    @Value("${ai.deepseek.model:deepseek-chat}")
    private String deepseekModel;

    // OpenAI 配置
    @Value("${ai.openai.api-key:}")
    private String openaiApiKey;

    @Value("${ai.openai.api-url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;

    @Value("${ai.openai.model:gpt-3.5-turbo}")
    private String openaiModel;

    @Value("${ai.deepseek.timeout:60000}")
    private long timeout;

    public AIService() {
        // 配置 RestTemplate 超时
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) timeout);
        factory.setReadTimeout((int) timeout);
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * 使用AI生成旅游路线
     */
    public AIRouteResponse generateRoute(GenerateRouteRequest request) {
        // 根据配置的 provider 选择 API（在 try 块外声明，以便在 catch 块中使用）
        String apiKey;
        String apiUrl;
        String model;

        if ("deepseek".equalsIgnoreCase(provider)) {
            apiKey = deepseekApiKey;
            apiUrl = deepseekApiUrl;
            model = deepseekModel;
            log.info("使用 DeepSeek API 生成路线");
        } else {
            apiKey = openaiApiKey;
            apiUrl = openaiApiUrl;
            model = openaiModel;
            log.info("使用 OpenAI API 生成路线");
        }

        try {

            // 检查API密钥是否配置
            if (apiKey == null || apiKey.isEmpty() ||
                apiKey.equals("your-deepseek-api-key") ||
                apiKey.equals("your-openai-api-key")) {
                log.warn("AI API密钥未配置（Provider: {}），使用默认路线生成逻辑", provider);
                return generateDefaultRoute(request);
            }

            // 构建提示词
            String prompt = buildPrompt(request);

            // 构建请求体（DeepSeek 和 OpenAI 使用相同的格式）
            OpenAIRequest aiRequest = new OpenAIRequest();
            aiRequest.setModel(model);
            List<OpenAIRequest.Message> messages = new ArrayList<>();
            OpenAIRequest.Message message = new OpenAIRequest.Message();
            message.setRole("user");
            message.setContent(prompt);
            messages.add(message);
            aiRequest.setMessages(messages);
            aiRequest.setTemperature(0.7);
            aiRequest.setMaxTokens(4000); // 增加最大 token 数，允许生成更详细的路线

            // 发送请求到 AI API
            log.debug("调用 {} API: {}", provider, apiUrl);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<OpenAIRequest> requestEntity = new HttpEntity<>(aiRequest, headers);

            // 发送请求
            log.info("正在调用 {} API 生成路线，URL: {}", provider, apiUrl);
            log.debug("请求体: {}", objectMapper.writeValueAsString(aiRequest));

            ResponseEntity<OpenAIResponse> responseEntity;
            try {
                responseEntity = restTemplate.exchange(
                        apiUrl,
                        HttpMethod.POST,
                        requestEntity,
                        OpenAIResponse.class
                );
            } catch (org.springframework.web.client.HttpClientErrorException e) {
                String responseBody = e.getResponseBodyAsString();
                log.error("{} API返回HTTP错误，状态码: {}, 响应体: {}", provider, e.getStatusCode(), responseBody);

                // 解析错误信息
                String errorMessage = parseApiErrorMessage(responseBody, e.getStatusCode());
                throw new RuntimeException(errorMessage);
            } catch (org.springframework.web.client.ResourceAccessException e) {
                log.error("{} API连接超时或网络错误", provider, e);
                throw new RuntimeException("AI API连接失败，请检查网络连接: " + e.getMessage());
            }

            OpenAIResponse response = responseEntity.getBody();

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                log.error("{} API返回空响应，状态码: {}", provider, responseEntity.getStatusCode());
                throw new RuntimeException("AI API返回空响应，请检查API配置和网络连接");
            }

            String content = response.getChoices().get(0).getMessage().getContent();
            log.info("{} 生成的路线内容（前500字符）: {}", provider,
                    content.length() > 500 ? content.substring(0, 500) + "..." : content);

            // 解析AI返回的JSON
            AIRouteResponse aiRouteResponse = parseAIResponse(content, request);
            log.info("成功解析AI响应，包含 {} 天的行程",
                    aiRouteResponse.getItinerary() != null ? aiRouteResponse.getItinerary().size() : 0);
            return aiRouteResponse;

        } catch (RuntimeException e) {
            // 如果是我们主动抛出的异常，直接抛出，不要使用默认数据
            log.error("AI服务调用失败: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("调用AI服务时发生未预期的错误，Provider: {}, URL: {}", provider, apiUrl, e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建AI提示词
     */
    private String buildPrompt(GenerateRouteRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为我在新疆维吾尔自治区规划一条个性化的旅游路线。\n\n");
        prompt.append("路线要求：\n");
        prompt.append("- 起点：").append(request.getStartLocation()).append("\n");
        prompt.append("- 终点：").append(request.getEndLocation()).append("\n");
        prompt.append("- 行程天数：").append(request.getDuration()).append("天\n");
        prompt.append("- 人数：").append(request.getPeopleCount()).append("人\n");
        prompt.append("- 预算：").append(request.getBudget()).append("元\n");

        if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            prompt.append("- 兴趣标签：").append(String.join("、", request.getInterests())).append("\n");
        }

        if (request.getMustVisitLocations() != null && !request.getMustVisitLocations().isEmpty()) {
            prompt.append("- 必须包含的地点：").append(String.join("、", request.getMustVisitLocations())).append("\n");
        }

        prompt.append("\n请生成一个详细的旅游路线，包括：\n");
        prompt.append("1. 路线标题（简洁吸引人）\n");
        prompt.append("2. 路线描述（200字左右）\n");
        prompt.append("3. 每日行程安排（按天详细列出）\n");
        prompt.append("4. 每日包含的具体景点和活动\n");
        prompt.append("5. 预算分配建议\n");
        prompt.append("6. 实用小贴士（3-5条）\n\n");
        prompt.append("请以JSON格式返回，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"title\": \"路线标题\",\n");
        prompt.append("  \"description\": \"路线描述\",\n");
        prompt.append("  \"itinerary\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"day\": 1,\n");
        prompt.append("      \"title\": \"第一天标题\",\n");
        prompt.append("      \"description\": \"第一天详细描述\",\n");
        prompt.append("      \"locations\": [\n");
        prompt.append("        {\"name\": \"景点名称\", \"lat\": 43.8833, \"lng\": 88.1333, \"description\": \"景点描述\"}\n");
        prompt.append("      ],\n");
        prompt.append("      \"accommodation\": \"住宿建议\",\n");
        prompt.append("      \"meals\": [\"早餐\", \"午餐\", \"晚餐\"]\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"tips\": [\"提示1\", \"提示2\", \"提示3\"]\n");
        prompt.append("}\n");
        prompt.append("\n请确保所有地点都是新疆维吾尔自治区内的真实景点，路线要符合预算和天数要求。");
        prompt.append("\n重要：每个地点必须包含 lat（纬度）和 lng（经度）坐标，坐标必须是有效的数字。");

        return prompt.toString();
    }

    /**
     * 解析AI返回的JSON响应
     */
    private AIRouteResponse parseAIResponse(String content, GenerateRouteRequest request) {
        try {
            // 尝试提取JSON部分（AI可能返回带markdown代码块的JSON）
            String jsonContent = content.trim();
            log.debug("原始AI响应内容: {}", jsonContent);

            if (jsonContent.contains("```json")) {
                int start = jsonContent.indexOf("```json") + 7;
                int end = jsonContent.indexOf("```", start);
                if (end > start) {
                    jsonContent = jsonContent.substring(start, end).trim();
                    log.debug("从markdown代码块中提取JSON");
                }
            } else if (jsonContent.contains("```")) {
                int start = jsonContent.indexOf("```") + 3;
                int end = jsonContent.indexOf("```", start);
                if (end > start) {
                    jsonContent = jsonContent.substring(start, end).trim();
                    log.debug("从代码块中提取JSON");
                }
            }

            // 清理 JSON 内容：移除中文标点符号和其他可能导致解析错误的字符
            jsonContent = cleanJsonContent(jsonContent);

            log.debug("准备解析的JSON内容: {}", jsonContent);
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            AIRouteResponse response = new AIRouteResponse();

            // 解析基本信息
            response.setTitle(rootNode.has("title") ? rootNode.get("title").asText() :
                "从" + request.getStartLocation() + "到" + request.getEndLocation() + "的路线");
            response.setDescription(rootNode.has("description") ? rootNode.get("description").asText() :
                "根据您的要求生成的个性化路线");

            log.debug("解析到标题: {}, 描述: {}", response.getTitle(), response.getDescription());

            // 解析行程安排
            List<AIRouteResponse.ItineraryItem> itinerary = new ArrayList<>();
            if (rootNode.has("itinerary") && rootNode.get("itinerary").isArray()) {
                JsonNode itineraryArray = rootNode.get("itinerary");
                log.debug("找到行程数组，包含 {} 项", itineraryArray.size());

                for (JsonNode itemNode : itineraryArray) {
                    AIRouteResponse.ItineraryItem item = new AIRouteResponse.ItineraryItem();
                    item.setDay(itemNode.has("day") ? itemNode.get("day").asInt() : 1);
                    item.setTitle(itemNode.has("title") ? itemNode.get("title").asText() : "第" + item.getDay() + "天");
                    item.setDescription(itemNode.has("description") ? itemNode.get("description").asText() : "");

                    // 解析地点
                    List<AIRouteResponse.Location> locations = new ArrayList<>();
                    if (itemNode.has("locations") && itemNode.get("locations").isArray()) {
                        JsonNode locationsArray = itemNode.get("locations");
                        log.debug("第{}天找到 {} 个地点", item.getDay(), locationsArray.size());

                        for (JsonNode locNode : locationsArray) {
                            AIRouteResponse.Location loc = new AIRouteResponse.Location();
                            loc.setName(locNode.has("name") ? locNode.get("name").asText() : "");
                            loc.setDescription(locNode.has("description") ? locNode.get("description").asText() : null);

                            // 解析坐标（如果AI提供了）
                            if (locNode.has("lat") && !locNode.get("lat").isNull()) {
                                try {
                                    loc.setLat(locNode.get("lat").asDouble());
                                } catch (Exception e) {
                                    log.warn("无法解析地点 {} 的纬度", loc.getName());
                                    loc.setLat(null);
                                }
                            } else {
                                loc.setLat(null);
                            }

                            if (locNode.has("lng") && !locNode.get("lng").isNull()) {
                                try {
                                    loc.setLng(locNode.get("lng").asDouble());
                                } catch (Exception e) {
                                    log.warn("无法解析地点 {} 的经度", loc.getName());
                                    loc.setLng(null);
                                }
                            } else {
                                loc.setLng(null);
                            }

                            if (!loc.getName().isEmpty()) {
                                locations.add(loc);
                                log.debug("添加地点: {} (lat: {}, lng: {})", loc.getName(), loc.getLat(), loc.getLng());
                            }
                        }
                    }
                    item.setLocations(locations);

                    item.setAccommodation(itemNode.has("accommodation") ? itemNode.get("accommodation").asText() : null);

                    // 解析餐食
                    List<String> meals = new ArrayList<>();
                    if (itemNode.has("meals") && itemNode.get("meals").isArray()) {
                        JsonNode mealsArray = itemNode.get("meals");
                        for (JsonNode mealNode : mealsArray) {
                            meals.add(mealNode.asText());
                        }
                        log.debug("第{}天找到 {} 个餐食", item.getDay(), meals.size());
                    }
                    item.setMeals(meals);

                    itinerary.add(item);
                }
            } else {
                log.warn("AI响应中未找到有效的行程数组");
            }
            response.setItinerary(itinerary);

            // 解析提示
            List<String> tips = new ArrayList<>();
            if (rootNode.has("tips") && rootNode.get("tips").isArray()) {
                JsonNode tipsArray = rootNode.get("tips");
                for (JsonNode tipNode : tipsArray) {
                    tips.add(tipNode.asText());
                }
                log.debug("找到 {} 条提示", tips.size());
            }
            response.setTips(tips);

            log.info("成功解析AI响应: 标题={}, 行程天数={}, 提示数={}",
                    response.getTitle(), itinerary.size(), tips.size());
            return response;

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("JSON解析失败，原始内容: {}", content, e);
            throw new RuntimeException("AI返回的JSON格式不正确: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("解析AI响应时发生错误，原始内容: {}", content, e);
            throw new RuntimeException("解析AI响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 清理 JSON 内容，移除可能导致解析错误的中文标点符号和其他非标准字符
     */
    private String cleanJsonContent(String jsonContent) {
        if (jsonContent == null || jsonContent.isEmpty()) {
            return jsonContent;
        }

        // 移除可能的 BOM 标记
        if (jsonContent.startsWith("\uFEFF")) {
            jsonContent = jsonContent.substring(1);
        }

        // 第一步：在字符串值内转义中文引号
        // 这是关键步骤：将字符串值内的中文引号转义为转义的英文引号
        StringBuilder cleaned = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < jsonContent.length(); i++) {
            char c = jsonContent.charAt(i);

            if (escaped) {
                cleaned.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                cleaned.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                cleaned.append(c);
                continue;
            }

            if (inString) {
                // 在字符串值内，将中文引号转义为转义的英文引号
                if (c == '\u201C' || c == '\u201D') {  // 中文双引号
                    cleaned.append("\\\"");
                } else if (c == '\u2018' || c == '\u2019') {  // 中文单引号
                    cleaned.append("'");  // 单引号在 JSON 字符串中不需要转义
                } else {
                    cleaned.append(c);
                }
            } else {
                // 在 JSON 结构外，替换中文引号为英文引号
                if (c == '\u201C' || c == '\u201D') {
                    cleaned.append('"');
                } else if (c == '\u2018' || c == '\u2019') {
                    cleaned.append('\'');
                } else if (c == '《' || c == '》' || c == '「' || c == '」' || c == '『' || c == '』') {
                    // 移除 JSON 结构中的中文标点符号
                    continue;
                } else {
                    cleaned.append(c);
                }
            }
        }

        jsonContent = cleaned.toString();

        // 第二步：确保所有字符串值都用双引号包裹（而不是单引号）
        // 使用更精确的正则表达式，避免替换字符串值内的单引号
        jsonContent = jsonContent.replaceAll("(\\s|^|[,:{\\[])'([^']*)'(\\s|$|[,}\\]])", "$1\"$2\"$3");

        // 第三步：尝试解析，如果失败则记录错误
        try {
            objectMapper.readTree(jsonContent);
            return jsonContent;
        } catch (Exception e) {
            log.error("JSON 清理后仍无法解析，错误位置: line {}, column {}",
                    e.getMessage().contains("line") ? extractLineNumber(e.getMessage()) : "unknown",
                    e.getMessage().contains("column") ? extractColumnNumber(e.getMessage()) : "unknown");
            log.debug("清理后的 JSON 内容: {}", jsonContent);
            throw new RuntimeException("JSON 格式错误，无法解析: " + e.getMessage(), e);
        }
    }

    /**
     * 从错误消息中提取行号
     */
    private String extractLineNumber(String errorMessage) {
        try {
            int start = errorMessage.indexOf("line:") + 5;
            int end = errorMessage.indexOf(",", start);
            if (end == -1) end = errorMessage.length();
            return errorMessage.substring(start, end).trim();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 从错误消息中提取列号
     */
    private String extractColumnNumber(String errorMessage) {
        try {
            int start = errorMessage.indexOf("column:") + 7;
            int end = errorMessage.length();
            return errorMessage.substring(start, end).trim();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 解析API返回的错误信息
     */
    private String parseApiErrorMessage(String responseBody, org.springframework.http.HttpStatusCode statusCode) {
        if (responseBody == null || responseBody.isEmpty()) {
            return "AI API调用失败: " + statusCode;
        }

        try {
            // 尝试解析JSON格式的错误响应
            JsonNode errorNode = objectMapper.readTree(responseBody);

            // 检查是否有 error 字段
            if (errorNode.has("error")) {
                JsonNode error = errorNode.get("error");

                // 检查是否有 message 字段
                if (error.has("message")) {
                    String message = error.get("message").asText();

                    // 针对特定错误码提供友好提示
                    if (statusCode.value() == 402) {
                        if (message.contains("Insufficient Balance") || message.contains("余额")) {
                            return "AI服务余额不足，请充值后重试。如需帮助，请联系管理员。";
                        }
                        return "AI服务账户余额不足: " + message;
                    } else if (statusCode.value() == 401) {
                        return "AI服务API密钥无效，请检查配置。";
                    } else if (statusCode.value() == 429) {
                        return "AI服务请求过于频繁，请稍后再试。";
                    } else if (statusCode.value() == 500 || statusCode.value() == 503) {
                        return "AI服务暂时不可用，请稍后再试。";
                    }

                    return "AI服务错误: " + message;
                }

                // 检查是否有 code 字段
                if (error.has("code")) {
                    String code = error.get("code").asText();
                    return "AI服务错误 (" + code + "): " + (error.has("message") ? error.get("message").asText() : "未知错误");
                }
            }

            // 如果没有 error 字段，尝试直接读取 message
            if (errorNode.has("message")) {
                return "AI服务错误: " + errorNode.get("message").asText();
            }

        } catch (Exception parseException) {
            log.debug("无法解析错误响应JSON: {}", responseBody, parseException);
        }

        // 如果无法解析，返回原始状态码和响应体（截取前200字符）
        String shortBody = responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody;
        return "AI API调用失败 (" + statusCode + "): " + shortBody;
    }

    /**
     * 生成默认路线（当AI服务不可用时）
     */
    private AIRouteResponse generateDefaultRoute(GenerateRouteRequest request) {
        AIRouteResponse response = new AIRouteResponse();
        response.setTitle("从" + request.getStartLocation() + "到" + request.getEndLocation() + "的路线");
        response.setDescription("根据您的兴趣和需求生成的个性化路线，包含" + request.getDuration() + "天的行程安排。");

        // 新疆主要景点和城市的地图（用于生成示例路线）
        String[][] xinjiangAttractions = {
            {"天山天池", "43.8856", "88.1353", "天山天池是新疆著名的自然风景区，湖水清澈，四周群山环绕"},
            {"喀纳斯湖", "48.7128", "87.0483", "喀纳斯湖被誉为\"人间仙境\"，湖水呈现神秘的蓝绿色"},
            {"吐鲁番葡萄沟", "42.9476", "89.1891", "吐鲁番葡萄沟是新疆著名的葡萄种植基地，可以品尝到各种美味的葡萄"},
            {"那拉提草原", "43.2583", "83.2617", "那拉提草原是新疆最美的草原之一，可以体验骑马和草原文化"},
            {"赛里木湖", "44.6000", "81.1000", "赛里木湖是新疆最大的高山湖泊，湖水清澈见底"},
            {"巴音布鲁克草原", "43.0333", "84.1500", "巴音布鲁克草原是新疆重要的草原生态保护区"},
            {"火焰山", "42.9476", "89.1891", "火焰山是《西游记》中著名的景点，夏季温度极高"},
            {"国际大巴扎", "43.7731", "87.6139", "乌鲁木齐国际大巴扎是新疆最大的民族特色市场"},
            {"红山公园", "43.7731", "87.6139", "红山公园是乌鲁木齐市中心的标志性景点"},
            {"伊犁河谷", "43.9167", "81.3167", "伊犁河谷是新疆最富饶的地区之一，风景优美"}
        };

        List<AIRouteResponse.ItineraryItem> itinerary = new ArrayList<>();
        int attractionIndex = 0;

        for (int i = 1; i <= request.getDuration(); i++) {
            AIRouteResponse.ItineraryItem item = new AIRouteResponse.ItineraryItem();
            item.setDay(i);

            // 根据天数生成不同的标题和描述
            String dayTitle;
            String dayDescription;
            List<AIRouteResponse.Location> locations = new ArrayList<>();

            if (i == 1) {
                dayTitle = "出发日 - 探索" + request.getStartLocation();
                dayDescription = "从" + request.getStartLocation() + "出发，开始您的精彩旅程。";
                // 添加起点附近的地点
                if (attractionIndex < xinjiangAttractions.length) {
                    String[] attraction = xinjiangAttractions[attractionIndex % xinjiangAttractions.length];
                    AIRouteResponse.Location loc = new AIRouteResponse.Location();
                    loc.setName(attraction[0]);
                    loc.setLat(Double.parseDouble(attraction[1]));
                    loc.setLng(Double.parseDouble(attraction[2]));
                    loc.setDescription(attraction[3]);
                    locations.add(loc);
                    attractionIndex++;
                }
            } else if (i == request.getDuration()) {
                dayTitle = "返程日 - 抵达" + request.getEndLocation();
                dayDescription = "前往" + request.getEndLocation() + "，结束愉快的旅程。";
                // 添加终点附近的地点
                if (attractionIndex < xinjiangAttractions.length) {
                    String[] attraction = xinjiangAttractions[attractionIndex % xinjiangAttractions.length];
                    AIRouteResponse.Location loc = new AIRouteResponse.Location();
                    loc.setName(attraction[0]);
                    loc.setLat(Double.parseDouble(attraction[1]));
                    loc.setLng(Double.parseDouble(attraction[2]));
                    loc.setDescription(attraction[3]);
                    locations.add(loc);
                    attractionIndex++;
                }
            } else {
                dayTitle = "第" + i + "天 - 深度游览";
                dayDescription = "继续探索新疆的美丽风光和丰富文化。";
                // 为中间天数添加2-3个地点
                int locationsCount = Math.min(3, xinjiangAttractions.length - attractionIndex);
                for (int j = 0; j < locationsCount && attractionIndex < xinjiangAttractions.length; j++) {
                    String[] attraction = xinjiangAttractions[attractionIndex % xinjiangAttractions.length];
                    AIRouteResponse.Location loc = new AIRouteResponse.Location();
                    loc.setName(attraction[0]);
                    loc.setLat(Double.parseDouble(attraction[1]));
                    loc.setLng(Double.parseDouble(attraction[2]));
                    loc.setDescription(attraction[3]);
                    locations.add(loc);
                    attractionIndex++;
                }
            }

            item.setTitle(dayTitle);
            item.setDescription(dayDescription);
            item.setLocations(locations);

            // 根据兴趣标签生成餐饮建议
            List<String> meals = new ArrayList<>();
            if (request.getInterests() != null && !request.getInterests().isEmpty()) {
                meals.add("特色早餐（推荐：新疆馕、奶茶）");
                meals.add("当地特色午餐");
                meals.add("新疆风味晚餐（推荐：大盘鸡、手抓饭）");
            } else {
                meals.add("早餐");
                meals.add("午餐");
                meals.add("晚餐");
            }
            item.setMeals(meals);

            // 住宿建议
            if (i < request.getDuration()) {
                item.setAccommodation("建议在" + request.getStartLocation() + "附近预订酒店或民宿");
            } else {
                item.setAccommodation("在" + request.getEndLocation() + "住宿");
            }

            itinerary.add(item);
        }

        response.setItinerary(itinerary);

        // 根据兴趣标签生成提示
        List<String> tips = new ArrayList<>();
        tips.add("建议提前预订住宿，特别是在旅游旺季");
        tips.add("注意当地天气变化，新疆昼夜温差较大");
        tips.add("携带身份证件，部分景区需要实名登记");
        if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            tips.add("根据您的兴趣标签：" + String.join("、", request.getInterests()) + "，可以重点关注相关景点");
        }
        tips.add("尊重当地民族文化和习俗");
        response.setTips(tips);

        return response;
    }

    // DTO类
    @Data
    public static class AIRouteResponse {
        private String title;
        private String description;
        private List<ItineraryItem> itinerary;
        private List<String> tips;

        @Data
        public static class ItineraryItem {
            private Integer day;
            private String title;
            private String description;
            private List<Location> locations;
            private String accommodation;
            private List<String> meals;
        }

        @Data
        public static class Location {
            private String name;
            private Double lat;
            private Double lng;
            private String description;
        }
    }

    // OpenAI API请求和响应类
    @Data
    private static class OpenAIRequest {
        private String model;
        private List<Message> messages;
        private Double temperature;
        private Integer maxTokens;

        @Data
        public static class Message {
            private String role;
            private String content;
        }
    }

    @Data
    private static class OpenAIResponse {
        private List<Choice> choices;

        @Data
        public static class Choice {
            private Message message;
        }

        @Data
        public static class Message {
            private String content;
        }
    }
}

