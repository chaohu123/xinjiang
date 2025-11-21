package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.request.AiExplainRequest;
import com.example.culturalxinjiang.dto.request.GenerateRouteRequest;
import com.example.culturalxinjiang.dto.response.AiExplainResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class AIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final int MAX_GENERATION_ATTEMPTS = 3;

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

    @Value("${ai.deepseek.timeout:300000}")
    private long timeout;

    public String getProvider() {
        return provider;
    }

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

            // 检查API密钥是否配置
            if (isApiKeyMissing(apiKey)) {
                log.warn("AI API密钥未配置（Provider: {}），使用默认路线生成逻辑", provider);
                return generateDefaultRoute(request);
            }

        String retryHint = null;
        for (int attempt = 1; attempt <= MAX_GENERATION_ATTEMPTS; attempt++) {
            try {

            // 构建提示词
            String prompt = buildPrompt(request);
            if (retryHint != null) {
                prompt = prompt + "\n\n【系统提醒】" + retryHint + "。请重新生成符合 schema 的 JSON，务必在返回前自检语法。";
            }

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
            aiRequest.setMaxTokens(16000); // 增加最大 token 数，允许生成更详细的路线（DeepSeek 支持最多 16k tokens）

            // 发送请求到 AI API
            log.debug("调用 {} API: {}", provider, apiUrl);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<OpenAIRequest> requestEntity = new HttpEntity<>(aiRequest, headers);

            // 发送请求
            log.debug("正在调用 {} API 生成路线", provider);

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

            AIRouteResponse aiRouteResponse;
            try {
                // 解析AI返回的JSON
                aiRouteResponse = parseAIResponse(content, request);
            } catch (RuntimeException parseException) {
                log.error("AI响应解析失败，将降级为默认路线: {}", parseException.getMessage(), parseException);
                aiRouteResponse = buildFallbackRoute(request, parseException.getMessage());
            }

            // 验证行程完整性（确保天数与用户请求一致）
            validateItineraryCompleteness(aiRouteResponse, request);

            // 只输出是否成功接收数据
            System.out.println("接收数据成功");
            log.info("接收数据成功");

            return aiRouteResponse;

            } catch (JsonFormatException jsonError) {
                retryHint = "上一次响应的 JSON 语法错误: " + jsonError.getMessage();
                log.warn("AI响应 JSON 语法错误（尝试 {}/{}）：{}", attempt, MAX_GENERATION_ATTEMPTS, jsonError.getMessage());
                if (attempt == MAX_GENERATION_ATTEMPTS) {
                    log.warn("多次尝试仍无法解析 JSON，使用兜底路线");
                    return buildFallbackRoute(request, jsonError.getMessage());
                }
                safeRetryPause();
            } catch (IncompleteItineraryException incomplete) {
                retryHint = incomplete.getMessage();
                log.warn("AI响应天数不完整（尝试 {}/{}）：{}", attempt, MAX_GENERATION_ATTEMPTS, incomplete.getMessage());

                // 如果天数不完整且请求天数较多（>5天），尝试分段生成
                // 在第一次检测到不完整时就开始分段生成，避免浪费时间重试
                if (request.getDuration() > 5 && attempt == 1) {
                    log.info("检测到长行程请求（{}天），立即尝试分段生成", request.getDuration());
                    try {
                        AIRouteResponse segmentedResponse = generateRouteSegmented(request, apiKey, apiUrl, model);
                        if (segmentedResponse != null) {
                            // 验证分段生成的结果是否完整
                            try {
                                validateItineraryCompleteness(segmentedResponse, request);
                                log.info("分段生成成功，返回完整行程（{}天）", segmentedResponse.getItinerary() != null ? segmentedResponse.getItinerary().size() : 0);
                                return segmentedResponse;
                            } catch (IncompleteItineraryException validationException) {
                                log.warn("分段生成的结果仍不完整: {}", validationException.getMessage());
                            }
                        }
                    } catch (Exception segException) {
                        log.warn("分段生成失败: {}", segException.getMessage(), segException);
                    }
                }

                if (attempt == MAX_GENERATION_ATTEMPTS) {
                    log.warn("多次尝试仍未获得完整行程，使用兜底路线");
                    return buildFallbackRoute(request, incomplete.getMessage());
                }
                safeRetryPause();
        } catch (RuntimeException e) {
            // 如果是我们主动抛出的异常，直接抛出，不要使用默认数据
            System.out.println("失败");
            log.error("失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("调用AI服务时发生未预期的错误，Provider: {}, URL: {}", provider, apiUrl, e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage(), e);
        }
        }

        // 理论上不会触发，添加兜底返回
        return buildFallbackRoute(request, "AI响应未包含完整行程");
    }

    /**
     * 分段生成路线（用于长行程，将总天数拆分成多个批次生成）
     */
    private AIRouteResponse generateRouteSegmented(GenerateRouteRequest request, String apiKey, String apiUrl, String model) {
        int totalDays = request.getDuration();
        int batchSize = 4; // 每批生成4天，确保不超过AI限制
        int batchCount = (totalDays + batchSize - 1) / batchSize; // 向上取整

        log.info("开始分段生成路线：总天数={}，分{}批，每批约{}天", totalDays, batchCount, batchSize);

        List<AIRouteResponse.ItineraryItem> allItineraryItems = new ArrayList<>();
        String title = null;
        String description = null;
        List<String> tips = new ArrayList<>();

        for (int batch = 0; batch < batchCount; batch++) {
            int startDay = batch * batchSize + 1;
            int endDay = Math.min((batch + 1) * batchSize, totalDays);
            int currentBatchDays = endDay - startDay + 1;

            log.info("生成第{}批行程：第{}天到第{}天（共{}天）", batch + 1, startDay, endDay, currentBatchDays);

            try {
                // 创建批次请求
                GenerateRouteRequest batchRequest = createBatchRequest(request, startDay, endDay, currentBatchDays);

                // 构建提示词（指定天数范围）
                String prompt = buildPromptForBatch(batchRequest, startDay, endDay, batch, batchCount);

                // 调用AI API
                OpenAIRequest aiRequest = new OpenAIRequest();
                aiRequest.setModel(model);
                List<OpenAIRequest.Message> messages = new ArrayList<>();
                OpenAIRequest.Message message = new OpenAIRequest.Message();
                message.setRole("user");
                message.setContent(prompt);
                messages.add(message);
                aiRequest.setMessages(messages);
                aiRequest.setTemperature(0.7);
                aiRequest.setMaxTokens(16000);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBearerAuth(apiKey);

                HttpEntity<OpenAIRequest> requestEntity = new HttpEntity<>(aiRequest, headers);

                ResponseEntity<OpenAIResponse> responseEntity = restTemplate.exchange(
                        apiUrl,
                        HttpMethod.POST,
                        requestEntity,
                        OpenAIResponse.class
                );

                OpenAIResponse response = responseEntity.getBody();
                if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                    log.error("第{}批API返回空响应", batch + 1);
                    return null;
                }

                String content = response.getChoices().get(0).getMessage().getContent();

                // 解析响应
                AIRouteResponse batchResponse = parseAIResponse(content, batchRequest);

                // 提取并合并数据
                if (batch == 0) {
                    // 第一批：提取标题和描述
                    title = batchResponse.getTitle();
                    description = batchResponse.getDescription();
                    if (batchResponse.getTips() != null) {
                        tips.addAll(batchResponse.getTips());
                    }
                } else {
                    // 后续批次：合并提示
                    if (batchResponse.getTips() != null) {
                        tips.addAll(batchResponse.getTips());
                    }
                }

                // 合并行程项，确保天数连续
                if (batchResponse.getItinerary() != null) {
                    // 按 day 字段排序，确保顺序正确
                    List<AIRouteResponse.ItineraryItem> sortedItems = new ArrayList<>(batchResponse.getItinerary());
                    sortedItems.sort((a, b) -> Integer.compare(a.getDay(), b.getDay()));

                    int itemIndex = 0;
                    for (AIRouteResponse.ItineraryItem item : sortedItems) {
                        // 确保天数正确（从startDay开始，连续递增）
                        int adjustedDay = startDay + itemIndex;
                        item.setDay(adjustedDay);
                        allItineraryItems.add(item);
                        itemIndex++;
                    }

                    // 验证批次天数是否正确
                    if (itemIndex != currentBatchDays) {
                        log.warn("第{}批返回的天数不匹配：期望{}天，实际{}天", batch + 1, currentBatchDays, itemIndex);
                    }
                }

                // 批次间暂停，避免API限流
                if (batch < batchCount - 1) {
                    safeRetryPause();
                }

            } catch (Exception e) {
                log.error("第{}批生成失败: {}", batch + 1, e.getMessage(), e);
                return null;
            }
        }

        // 验证合并后的行程天数
        if (allItineraryItems.size() < totalDays) {
            log.warn("分段生成后仍缺少天数：期望{}天，实际{}天", totalDays, allItineraryItems.size());
            return null;
        }

        // 构建完整响应
        AIRouteResponse fullResponse = new AIRouteResponse();
        fullResponse.setTitle(title != null ? title : "从" + request.getStartLocation() + "到" + request.getEndLocation() + "的路线");
        fullResponse.setDescription(description != null ? description : "根据您的要求生成的个性化路线，包含" + totalDays + "天的行程安排。");
        fullResponse.setItinerary(allItineraryItems);
        fullResponse.setTips(tips);

        log.info("分段生成完成：共{}天行程，{}个行程项", totalDays, allItineraryItems.size());
        return fullResponse;
    }

    /**
     * 创建批次请求（用于分段生成）
     */
    private GenerateRouteRequest createBatchRequest(GenerateRouteRequest original, int startDay, int endDay, int batchDays) {
        GenerateRouteRequest batchRequest = new GenerateRouteRequest();
        // 复制原始请求的所有字段
        batchRequest.setDestinations(original.getDestinations());
        batchRequest.setStartLocation(original.getStartLocation());
        batchRequest.setEndLocation(original.getEndLocation());
        batchRequest.setTravelDates(original.getTravelDates());
        batchRequest.setDuration(batchDays); // 使用批次天数
        batchRequest.setArrivalTime(original.getArrivalTime());
        batchRequest.setDepartureTime(original.getDepartureTime());
        batchRequest.setPeopleCount(original.getPeopleCount());
        batchRequest.setAgeGroups(original.getAgeGroups());
        batchRequest.setHasMobilityIssues(original.getHasMobilityIssues());
        batchRequest.setSpecialDietary(original.getSpecialDietary());
        batchRequest.setStylePreferences(original.getStylePreferences());
        batchRequest.setInterests(original.getInterests());
        batchRequest.setTotalBudget(original.getTotalBudget());
        batchRequest.setDailyBudget(original.getDailyBudget());
        batchRequest.setBudget(original.getBudget());
        batchRequest.setIncludesFlight(original.getIncludesFlight());
        batchRequest.setAccommodationPreferences(original.getAccommodationPreferences());
        batchRequest.setTransportationPreferences(original.getTransportationPreferences());
        batchRequest.setMustVisit(original.getMustVisit());
        batchRequest.setMustAvoid(original.getMustAvoid());
        batchRequest.setMustVisitLocations(original.getMustVisitLocations());
        batchRequest.setWeatherSensitivity(original.getWeatherSensitivity());
        batchRequest.setNeedRestaurantSuggestions(original.getNeedRestaurantSuggestions());
        batchRequest.setNeedTicketSuggestions(original.getNeedTicketSuggestions());
        batchRequest.setNeedTransportSuggestions(original.getNeedTransportSuggestions());
        batchRequest.setNeedPackingList(original.getNeedPackingList());
        batchRequest.setNeedSafetyTips(original.getNeedSafetyTips());
        batchRequest.setNeedVisaInfo(original.getNeedVisaInfo());
        batchRequest.setOutputFormats(original.getOutputFormats());
        return batchRequest;
    }

    /**
     * 构建批次提示词（指定天数范围）
     */
    private String buildPromptForBatch(GenerateRouteRequest request, int startDay, int endDay, int batchIndex, int totalBatches) {
        String basePrompt = buildPrompt(request);

        // 在提示词开头添加批次说明
        StringBuilder batchPrompt = new StringBuilder();
        batchPrompt.append("【重要说明】这是分段生成的第").append(batchIndex + 1).append("批（共").append(totalBatches).append("批）。\n");
        batchPrompt.append("请仅生成第").append(startDay).append("天到第").append(endDay).append("天的行程安排。\n");
        if (batchIndex == 0) {
            batchPrompt.append("这是行程的开始部分，请包含路线标题、描述和总体信息。\n");
        } else if (batchIndex == totalBatches - 1) {
            batchPrompt.append("这是行程的结束部分，请确保行程自然结束于终点。\n");
        } else {
            batchPrompt.append("这是行程的中间部分，请确保与前后行程衔接自然。\n");
        }
        batchPrompt.append("itinerary 数组中必须包含且仅包含第").append(startDay).append("天到第").append(endDay).append("天的行程项，day 字段必须从").append(startDay).append("开始递增。\n\n");

        batchPrompt.append(basePrompt);

        return batchPrompt.toString();
    }

    /**
     * 构建AI提示词
     */
    private String buildPrompt(GenerateRouteRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为我在新疆维吾尔自治区规划一条个性化的旅游路线。\n\n");

        // 处理目的地：提取起点和终点
        String destinations = request.getDestinations();
        String startLocation = request.getStartLocation();
        String endLocation = request.getEndLocation();

        if (destinations != null && !destinations.isEmpty()) {
            // 从 destinations 中提取起点和终点
            String[] cities = destinations.split("[,，→]|->");
            if (cities.length > 0) {
                startLocation = cities[0].trim();
                if (cities.length > 1) {
                    endLocation = cities[cities.length - 1].trim();
                } else {
                    endLocation = startLocation; // 单城市路线
                }
            }
        }

        // 如果仍然没有起点和终点，使用旧字段
        if ((startLocation == null || startLocation.isEmpty()) &&
            (request.getStartLocation() != null && !request.getStartLocation().isEmpty())) {
            startLocation = request.getStartLocation();
        }
        if ((endLocation == null || endLocation.isEmpty()) &&
            (request.getEndLocation() != null && !request.getEndLocation().isEmpty())) {
            endLocation = request.getEndLocation();
        }

        prompt.append("【目的地与时间】\n");
        if (startLocation != null && !startLocation.isEmpty()) {
            prompt.append("- 起点：").append(startLocation).append("\n");
        }
        if (endLocation != null && !endLocation.isEmpty()) {
            prompt.append("- 终点：").append(endLocation).append("\n");
        }
        if (destinations != null && !destinations.isEmpty() && destinations.contains(",")) {
            prompt.append("- 完整路线（按顺序）：").append(destinations).append("\n");
        }
        if (request.getTravelDates() != null && !request.getTravelDates().isEmpty()) {
            prompt.append("- 旅行日期：").append(request.getTravelDates()).append("\n");
        }
        prompt.append("- 行程天数：").append(request.getDuration()).append("天\n");
        if (request.getArrivalTime() != null && !request.getArrivalTime().isEmpty()) {
            prompt.append("- 到达时段：").append(request.getArrivalTime()).append("\n");
        }
        if (request.getDepartureTime() != null && !request.getDepartureTime().isEmpty()) {
            prompt.append("- 离开时段：").append(request.getDepartureTime()).append("\n");
        }

        prompt.append("\n【人员信息】\n");
        prompt.append("- 人数：").append(request.getPeopleCount()).append("人\n");
        if (request.getAgeGroups() != null && !request.getAgeGroups().isEmpty()) {
            prompt.append("- 年龄段：").append(request.getAgeGroups()).append("\n");
        }
        if (request.getHasMobilityIssues() != null && request.getHasMobilityIssues()) {
            prompt.append("- 行动不便者：是（路线需考虑无障碍设施）\n");
        }
        if (request.getSpecialDietary() != null && !request.getSpecialDietary().isEmpty()) {
            prompt.append("- 特殊饮食需求：").append(request.getSpecialDietary()).append("\n");
        }

        prompt.append("\n【风格与节奏偏好】\n");
        if (request.getStylePreferences() != null && !request.getStylePreferences().isEmpty()) {
            prompt.append("- 风格偏好：").append(String.join("、", request.getStylePreferences())).append("\n");
        } else if (request.getInterests() != null && !request.getInterests().isEmpty()) {
            prompt.append("- 兴趣标签：").append(String.join("、", request.getInterests())).append("\n");
        }

        prompt.append("\n【预算信息】\n");
        Double totalBudget = request.getTotalBudget();
        Double dailyBudget = request.getDailyBudget();
        Double budget = request.getBudget();
        if (totalBudget != null && totalBudget > 0) {
            prompt.append("- 总预算：").append(totalBudget).append("元");
            if (request.getIncludesFlight() != null && request.getIncludesFlight()) {
                prompt.append("（含机票）");
            } else {
                prompt.append("（不含机票）");
            }
            prompt.append("\n");
        } else if (dailyBudget != null && dailyBudget > 0) {
            prompt.append("- 每日预算：").append(dailyBudget).append("元/天\n");
        } else if (budget != null && budget > 0) {
            prompt.append("- 预算：").append(budget).append("元\n");
        }

        prompt.append("\n【住宿偏好】\n");
        if (request.getAccommodationPreferences() != null && !request.getAccommodationPreferences().isEmpty()) {
            prompt.append("- 住宿偏好：").append(String.join("、", request.getAccommodationPreferences())).append("\n");
        }

        prompt.append("\n【交通偏好】\n");
        if (request.getTransportationPreferences() != null && !request.getTransportationPreferences().isEmpty()) {
            prompt.append("- 交通偏好：").append(String.join("、", request.getTransportationPreferences())).append("\n");
        }

        prompt.append("\n【景点偏好】\n");
        if (request.getMustVisit() != null && !request.getMustVisit().isEmpty()) {
            prompt.append("- 必看景点/体验：").append(String.join("、", request.getMustVisit())).append("\n");
        }
        if (request.getMustAvoid() != null && !request.getMustAvoid().isEmpty()) {
            prompt.append("- 必须避开：").append(String.join("、", request.getMustAvoid())).append("\n");
        }
        // 兼容旧版本
        if (request.getMustVisitLocations() != null && !request.getMustVisitLocations().isEmpty()) {
            prompt.append("- 必须包含的地点：").append(String.join("、", request.getMustVisitLocations())).append("\n");
        }

        prompt.append("\n【其他要求】\n");
        if (request.getWeatherSensitivity() != null && !request.getWeatherSensitivity().isEmpty()) {
            prompt.append("- 天气/季节敏感：").append(request.getWeatherSensitivity()).append("\n");
        }

        prompt.append("\n【额外服务需求】\n");
        if (request.getNeedRestaurantSuggestions() != null && request.getNeedRestaurantSuggestions()) {
            prompt.append("- 需要餐厅预订建议\n");
        }
        if (request.getNeedTicketSuggestions() != null && request.getNeedTicketSuggestions()) {
            prompt.append("- 需要门票预订建议\n");
        }
        if (request.getNeedTransportSuggestions() != null && request.getNeedTransportSuggestions()) {
            prompt.append("- 需要交通预订建议\n");
        }
        if (request.getNeedPackingList() != null && request.getNeedPackingList()) {
            prompt.append("- 需要打包清单\n");
        }
        if (request.getNeedSafetyTips() != null && request.getNeedSafetyTips()) {
            prompt.append("- 需要安全提示\n");
        }
        if (request.getNeedVisaInfo() != null && request.getNeedVisaInfo()) {
            prompt.append("- 需要签证/入境提醒\n");
        }

        prompt.append("\n重要提示：路线标题和描述中必须明确包含起点和终点的城市名称，不能使用null或占位符。\n");
        prompt.append("例如：如果起点是乌鲁木齐，终点是喀什，标题应该是\"乌鲁木齐到喀什的丝路文化之旅\"，而不是\"从null到null的路线\"。\n\n");

        prompt.append("你是专业旅行规划师。请在保证落地性的前提下，生成一份结构清晰、信息准确的新疆行程方案，并重点覆盖以下要素：\n\n");

        prompt.append("【输出结构】\n");
        prompt.append("1. 路线标题：突出主题并包含起点/终点城市。\n");
        prompt.append("2. 路线描述：300-400字，说明亮点、适合人群、最佳季节与预期体验。\n");
        prompt.append("3. 行程安排：按天输出，每天包含：\n");
        prompt.append("   - 当日标题 + 150-200字概述\n");
        prompt.append("   - 时间轴（精确到小时）+ 交通方式与费用\n");
        prompt.append("   - 主要景点列表：名称、100字说明、开放时间、门票、最佳时段、建议时长、优先级、替代方案、经纬度\n");
        prompt.append("   - 餐饮：1-3 家/次推荐，含餐厅类型或名称、代表菜、人均消费、是否清真/素食\n");
        prompt.append("   - 住宿：推荐区域 + 经济/舒适/高端各1个，含价格区间与预订建议\n");
        prompt.append("   - 当日预算小结：交通/餐饮/住宿/门票/其他及总计\n");
        prompt.append("4. 总体预算：汇总各成本类别与舒适/节省两档范围。\n");
        prompt.append("5. Packing List：证件、衣物、用品、其他（各列举3-5项）。\n");
        prompt.append("6. 预订建议：列出需提前预约/购买的景点、交通或餐饮。\n");
        prompt.append("7. 全局提示 tips：气候、安全、文化礼仪等至少5条。\n\n");
        prompt.append("   - 需要提前预订的景点（门票、预约时间、预订渠道）\n");
        prompt.append("   - 需要提前预订的餐厅（热门餐厅、预订方式）\n");
        prompt.append("   - 需要提前预订的住宿（旺季建议、预订平台）\n");
        prompt.append("   - 需要提前预订的交通（机票、火车票、包车等）\n");
        prompt.append("7. 实用小贴士（8-12条，必须包含）：\n");
        prompt.append("   - 天气和穿着建议（具体季节的天气特点、必备衣物）\n");
        prompt.append("   - 交通注意事项（自驾注意事项、公共交通使用）\n");
        prompt.append("   - 安全注意事项（高原反应、防晒、防寒等）\n");
        prompt.append("   - 文化习俗（尊重当地文化、禁忌事项）\n");
        prompt.append("   - 最佳旅游季节（各季节的优缺点）\n");
        prompt.append("   - 证件和准备（身份证、边防证、常用药品等）\n");
        prompt.append("   - 通讯和网络（信号覆盖、网络情况）\n");
        prompt.append("   - 购物建议（特产推荐、购物地点、价格参考）\n\n");
        prompt.append("请以JSON格式返回，格式如下（所有字段都必须填写详细内容）：\n");
        prompt.append("{\n");
        prompt.append("  \"title\": \"路线标题（突出特色和主题）\",\n");
        prompt.append("  \"description\": \"路线描述（300-500字，包含亮点、适合人群、最佳季节、路线特色、文化背景、预期体验）\",\n");
        prompt.append("  \"itinerary\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"day\": 1,\n");
        prompt.append("      \"title\": \"第一天标题（突出当日主题和核心体验）\",\n");
        prompt.append("      \"description\": \"第一天详细描述（200-300字，包含当日行程概述、主要活动、文化体验、亮点推荐、注意事项）\",\n");
        prompt.append("      \"locations\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"name\": \"景点名称（真实存在的新疆景点）\",\n");
        prompt.append("          \"lat\": 43.8833,\n");
        prompt.append("          \"lng\": 88.1333,\n");
        prompt.append("          \"description\": \"景点详细描述（100-150字，包含：历史背景、文化意义、自然特色、游览亮点、拍照打卡点、最佳观赏角度、开放时间、门票价格、最佳游览时间、建议游览时长、游览路线建议、特色活动、注意事项（必须包含，如：尊重当地居民隐私、不要随意进入民居等））\",\n");
        prompt.append("          \"priority\": \"必去/推荐/可选\",\n");
        prompt.append("          \"alternative\": \"替代景点名称（如果该景点不可行）\"\n");
        prompt.append("        }\n");
        prompt.append("      ],\n");
        prompt.append("      \"accommodation\": {\n");
        prompt.append("        \"area\": \"推荐住宿区域\",\n");
        prompt.append("        \"budget\": {\"name\": \"经济型酒店名称\", \"address\": \"地址\", \"price\": \"200-300元/晚\", \"features\": \"特色\", \"booking\": \"预订建议\"},\n");
        prompt.append("        \"comfort\": {\"name\": \"舒适型酒店名称\", \"address\": \"地址\", \"price\": \"300-500元/晚\", \"features\": \"特色\", \"booking\": \"预订建议\"},\n");
        prompt.append("        \"luxury\": {\"name\": \"豪华型酒店名称\", \"address\": \"地址\", \"price\": \"500-800元/晚\", \"features\": \"特色\", \"booking\": \"预订建议\"}\n");
        prompt.append("      },\n");
        prompt.append("      \"meals\": [\n");
        prompt.append("        \"早餐：XX餐厅位于XX路，推荐XX、XX、XX（说明菜品特色），人均XX-XX元，特色说明（老字号/民族特色/环境等），用餐时间建议\",\n");
        prompt.append("        \"午餐：XX餐厅位于XX路，推荐XX、XX、XX（说明菜品特色），人均XX-XX元，特色说明，用餐时间建议\",\n");
        prompt.append("        \"晚餐：XX餐厅位于XX路，推荐XX、XX、XX（说明菜品特色），人均XX-XX元，特色说明，用餐时间建议\"\n");
        prompt.append("      ],\n");
        prompt.append("      \"transportation\": \"交通信息（详细说明：景点间交通方式、具体距离、预计时间、费用、路况说明、停车信息）\",\n");
        prompt.append("      \"timeSchedule\": \"时间安排（详细到小时，例如：08:00-09:00 酒店早餐；09:00-10:30 前往XX景点；10:30-12:00 游览XX景点；12:00-13:30 午餐；13:30-17:00 继续游览；17:00-18:00 休息；18:00-19:30 晚餐；19:30-21:00 夜游；21:00 返回酒店）\",\n");
        prompt.append("      \"dailyBudget\": \"每日预算分配（详细列出：交通XX元、早餐XX元、午餐XX元、晚餐XX元、住宿XX元、门票XX元、其他XX元，总计XX元）\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"budgetBreakdown\": {\n");
        prompt.append("    \"transportation\": \"交通费用说明和预算（往返交通、当地交通、详细说明）\",\n");
        prompt.append("    \"accommodation\": \"住宿费用说明和预算（按天数计算，不同档次选择）\",\n");
        prompt.append("    \"meals\": \"餐饮费用说明和预算（按天数计算，不同档次选择）\",\n");
        prompt.append("    \"tickets\": \"门票费用说明和预算（各景点门票合计）\",\n");
        prompt.append("    \"other\": \"其他费用说明和预算（购物、娱乐、保险等）\",\n");
        prompt.append("    \"totalRange\": \"总预算范围（最低预算XX元，舒适预算XX元）\"\n");
        prompt.append("  },\n");
        prompt.append("  \"packingList\": {\n");
        prompt.append("    \"documents\": [\"身份证\", \"边防证\", \"学生证等\"],\n");
        prompt.append("    \"clothing\": [\"根据季节列出具体衣物\"],\n");
        prompt.append("    \"essentials\": [\"防晒用品\", \"药品\", \"电子设备等\"],\n");
        prompt.append("    \"other\": [\"其他必需品\"]\n");
        prompt.append("  },\n");
        prompt.append("  \"bookingAdvice\": {\n");
        prompt.append("    \"attractions\": [\"需要提前预订的景点及预订方式\"],\n");
        prompt.append("    \"restaurants\": [\"需要提前预订的餐厅及预订方式\"],\n");
        prompt.append("    \"accommodation\": \"住宿预订建议\",\n");
        prompt.append("    \"transportation\": \"交通预订建议\"\n");
        prompt.append("  },\n");
        prompt.append("  \"tips\": [\n");
        prompt.append("    \"天气和穿着建议（具体季节的天气特点、必备衣物）\",\n");
        prompt.append("    \"交通注意事项（自驾注意事项、公共交通使用）\",\n");
        prompt.append("    \"安全注意事项（高原反应、防晒、防寒等）\",\n");
        prompt.append("    \"文化习俗（尊重当地文化、禁忌事项）\",\n");
        prompt.append("    \"最佳旅游季节（各季节的优缺点）\",\n");
        prompt.append("    \"证件和准备（身份证、边防证、常用药品等）\",\n");
        prompt.append("    \"通讯和网络（信号覆盖、网络情况）\",\n");
        prompt.append("    \"购物建议（特产推荐、购物地点、价格参考）\"\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("\n重要要求：\n");
        prompt.append("1. 所有地点必须是新疆维吾尔自治区内的真实景点，确保路线合理可行\n");
        prompt.append("2. 每个地点必须包含精确的 lat（纬度）和 lng（经度）坐标，坐标必须是新疆境内的有效数字\n");
        prompt.append("3. 景点之间的距离要合理，同一天安排的景点不能距离过远（建议单日行程不超过300公里）\n");
        prompt.append("4. 路线要符合预算要求，总费用应在预算范围内\n");
        prompt.append("5. 餐饮和住宿推荐要符合预算水平，提供不同价位的选择\n");
        prompt.append("6. 时间安排要合理，考虑景点开放时间和交通时间\n");
        prompt.append("7. 描述要详细、生动，突出新疆的地域特色和文化魅力\n");
        prompt.append("8. 所有信息要准确、实用，便于用户实际使用\n");

        prompt.append("\n【严格格式要求】\n");
        prompt.append("A. 仅输出合法 JSON，禁止使用 Markdown、额外文字或注释。\n");
        prompt.append("B. 返回前请逐字段自检，保证所有引号、逗号、括号成对出现，没有 19:\"00 这类时间格式错误。\n");
        prompt.append("C. 若无法满足要求，请直接重写结果，不要部分输出。\n");

        return prompt.toString();
    }

    /**
     * 从已清理的 JSON 内容解析（内部方法，避免重复清理）
     */
    private AIRouteResponse parseAIResponseFromCleanedJson(String jsonContent, GenerateRouteRequest request) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            return buildRouteResponseFromJson(rootNode, request);
        } catch (Exception e) {
            log.error("从已清理的 JSON 解析失败: {}", e.getMessage());
            throw new RuntimeException("解析修复后的 JSON 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析AI返回的JSON响应
     */
    private AIRouteResponse parseAIResponse(String content, GenerateRouteRequest request) {
        String jsonContent = null;
        try {
            // 尝试提取JSON部分（AI可能返回带markdown代码块的JSON）
            jsonContent = content.trim();
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

            // 先检测并修复不完整的 JSON（如果响应被截断）
            // 注意：在清理之前先修复，因为清理过程可能会因为不完整的 JSON 而失败
            jsonContent = fixIncompleteJson(jsonContent);

            // 清理 JSON 内容：移除中文标点符号和其他可能导致解析错误的字符
            jsonContent = cleanJsonContent(jsonContent);
            jsonContent = repairJsonStructure(jsonContent);

            log.debug("准备解析的JSON内容长度: {} 字符", jsonContent.length());
            if (jsonContent.length() > 10000) {
                log.debug("JSON内容前1000字符: {}", jsonContent.substring(0, Math.min(1000, jsonContent.length())));
            } else {
            log.debug("准备解析的JSON内容: {}", jsonContent);
            }
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            return buildRouteResponseFromJson(rootNode, request);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("JSON解析失败，错误信息: {}", e.getMessage());
            log.error("原始内容长度: {} 字符", content != null ? content.length() : 0);

            // 如果 jsonContent 未初始化，使用原始 content
            if (jsonContent == null) {
                jsonContent = content != null ? content.trim() : "";
            }

            // 尝试修复 JSON（可能是响应被截断）
            String fixedContent = attemptJsonFix(jsonContent, e);
            if (fixedContent != null && !fixedContent.equals(jsonContent)) {
                try {
                    log.info("尝试使用修复后的 JSON 重新解析");
                    // 重新清理和修复
                    fixedContent = cleanJsonContent(fixedContent);
                    fixedContent = repairJsonStructure(fixedContent);
                    fixedContent = fixIncompleteJson(fixedContent);
                    // 再次应用逗号和引号修复
                    fixedContent = fixMissingCommasAndQuotes(fixedContent);
                    JsonNode rootNode = objectMapper.readTree(fixedContent);
                    log.info("JSON 修复成功，使用修复后的内容重新解析");
                    // 直接使用修复后的内容解析（跳过重复的清理步骤）
                    return parseAIResponseFromCleanedJson(fixedContent, request);
                } catch (Exception retryException) {
                    log.error("修复后的 JSON 仍无法解析: {}", retryException.getMessage());
                    throw new JsonFormatException("AI返回的JSON格式不正确，且无法自动修复: " + e.getMessage(), e);
                }
            }

            // 如果 attemptJsonFix 没有修复，尝试直接应用逗号和引号修复
            try {
                log.info("尝试直接修复缺少逗号和引号的问题");
                String directFixed = fixMissingCommasAndQuotes(jsonContent);
                directFixed = cleanJsonContent(directFixed);
                directFixed = repairJsonStructure(directFixed);
                JsonNode rootNode = objectMapper.readTree(directFixed);
                log.info("直接修复成功，使用修复后的内容重新解析");
                return parseAIResponseFromCleanedJson(directFixed, request);
            } catch (Exception directFixException) {
                log.debug("直接修复也失败: {}", directFixException.getMessage());
            }

            throw new JsonFormatException("AI返回的JSON格式不正确: " + e.getMessage()
                    + "。可能是响应被截断，建议检查提示词或减小输出体量。", e);
        } catch (Exception e) {
            log.error("解析AI响应时发生错误，原始内容: {}", content, e);
            throw new RuntimeException("解析AI响应失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从 JSON 节点构建路线响应
     */
    private AIRouteResponse buildRouteResponseFromJson(JsonNode rootNode, GenerateRouteRequest request) {
            AIRouteResponse response = new AIRouteResponse();

            // 提取起点和终点（用于生成默认标题）
            String startLocation = request.getStartLocation();
            String endLocation = request.getEndLocation();
            if (request.getDestinations() != null && !request.getDestinations().isEmpty()) {
                String[] cities = request.getDestinations().split("[,，→]|->");
                if (cities.length > 0) {
                    startLocation = cities[0].trim();
                    if (cities.length > 1) {
                        endLocation = cities[cities.length - 1].trim();
                    } else {
                        endLocation = startLocation;
                    }
                }
            }
            if (startLocation == null || startLocation.isEmpty()) {
                startLocation = "起点";
            }
            if (endLocation == null || endLocation.isEmpty()) {
                endLocation = "终点";
            }

            // 解析基本信息
            String aiTitle = rootNode.has("title") ? rootNode.get("title").asText() : null;
            if (aiTitle == null || aiTitle.isEmpty() || aiTitle.contains("null")) {
                // 如果 AI 返回的标题为空或包含 null，使用默认标题
                response.setTitle("从" + startLocation + "到" + endLocation + "的路线");
            } else {
                response.setTitle(aiTitle);
            }
            response.setDescription(rootNode.has("description") ? rootNode.get("description").asText() :
                "根据您的要求生成的个性化路线，包含" + request.getDuration() + "天的行程安排。");

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

                    // 解析描述
                    item.setDescription(itemNode.has("description") ? itemNode.get("description").asText() : "");

                    // 解析时间安排（作为独立字段）
                    item.setTimeSchedule(itemNode.has("timeSchedule") ? itemNode.get("timeSchedule").asText() : null);

                    // 解析交通信息（作为独立字段）
                    item.setTransportation(itemNode.has("transportation") ? itemNode.get("transportation").asText() : null);

                    // 解析每日预算（作为独立字段）
                    item.setDailyBudget(itemNode.has("dailyBudget") ? itemNode.get("dailyBudget").asText() : null);

                    // 解析地点
                    List<AIRouteResponse.Location> locations = new ArrayList<>();
                    if (itemNode.has("locations") && itemNode.get("locations").isArray()) {
                        JsonNode locationsArray = itemNode.get("locations");
                        log.debug("第{}天找到 {} 个地点", item.getDay(), locationsArray.size());

                        for (JsonNode locNode : locationsArray) {
                            AIRouteResponse.Location loc = new AIRouteResponse.Location();
                            loc.setName(locNode.has("name") ? locNode.get("name").asText() : "");

                            // 解析地点描述（包含开放时间、门票、最佳游览时间等详细信息）
                            StringBuilder locDescBuilder = new StringBuilder();
                            String originalDescription = locNode.has("description") ? locNode.get("description").asText() : "";
                            if (!originalDescription.isEmpty()) {
                                locDescBuilder.append(originalDescription);
                            }

                            // 解析优先等级
                            if (locNode.has("priority") && !locNode.get("priority").isNull()) {
                                String priority = locNode.get("priority").asText();
                                if (!priority.isEmpty()) {
                                    locDescBuilder.append("\n\n【优先等级】").append(priority);
                                }
                            }

                            // 解析替代方案
                            if (locNode.has("alternative") && !locNode.get("alternative").isNull()) {
                                String alternative = locNode.get("alternative").asText();
                                if (!alternative.isEmpty()) {
                                    locDescBuilder.append("\n【替代方案】").append(alternative);
                                }
                            }

                            // 提取注意事项（从原始描述中提取，替换地图链接）
                            // 检查描述中是否已经包含【注意事项】标签
                            String currentDesc = locDescBuilder.toString();
                            if (!currentDesc.contains("【注意事项】") && !currentDesc.contains("注意事项：")) {
                                String notes = extractNotes(originalDescription);
                                if (notes != null && !notes.isEmpty()) {
                                    locDescBuilder.append("\n【注意事项】").append(notes);
                                }
                            }

                            loc.setDescription(locDescBuilder.length() > 0 ? locDescBuilder.toString() : null);

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

                    // 解析住宿（支持字符串或对象格式）
                    if (itemNode.has("accommodation")) {
                        JsonNode accommodationNode = itemNode.get("accommodation");
                        if (accommodationNode.isObject()) {
                            // 如果是对象格式，构建详细的住宿信息
                            StringBuilder accBuilder = new StringBuilder();
                            if (accommodationNode.has("area")) {
                                accBuilder.append("【推荐住宿区域】").append(accommodationNode.get("area").asText()).append("\n");
                            }
                            if (accommodationNode.has("budget")) {
                                JsonNode budget = accommodationNode.get("budget");
                                accBuilder.append("【经济型】").append(budget.has("name") ? budget.get("name").asText() : "")
                                    .append(" - ").append(budget.has("price") ? budget.get("price").asText() : "")
                                    .append("，").append(budget.has("address") ? budget.get("address").asText() : "")
                                    .append("\n");
                            }
                            if (accommodationNode.has("comfort")) {
                                JsonNode comfort = accommodationNode.get("comfort");
                                accBuilder.append("【舒适型】").append(comfort.has("name") ? comfort.get("name").asText() : "")
                                    .append(" - ").append(comfort.has("price") ? comfort.get("price").asText() : "")
                                    .append("，").append(comfort.has("address") ? comfort.get("address").asText() : "")
                                    .append("\n");
                            }
                            if (accommodationNode.has("luxury")) {
                                JsonNode luxury = accommodationNode.get("luxury");
                                accBuilder.append("【豪华型】").append(luxury.has("name") ? luxury.get("name").asText() : "")
                                    .append(" - ").append(luxury.has("price") ? luxury.get("price").asText() : "")
                                    .append("，").append(luxury.has("address") ? luxury.get("address").asText() : "");
                            }
                            item.setAccommodation(accBuilder.toString());
                        } else {
                            // 如果是字符串格式，直接使用
                            item.setAccommodation(accommodationNode.asText());
                        }
                    } else {
                        item.setAccommodation(null);
                    }

                    // 解析餐食（包含详细信息：餐厅名称、推荐菜品、价格等）
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

            // 添加预算分配信息到提示中
            if (rootNode.has("budgetBreakdown")) {
                JsonNode budgetNode = rootNode.get("budgetBreakdown");
                StringBuilder budgetTip = new StringBuilder("【预算分配建议】\n");
                if (budgetNode.has("transportation")) {
                    budgetTip.append("交通：").append(budgetNode.get("transportation").asText()).append("\n");
                }
                if (budgetNode.has("accommodation")) {
                    budgetTip.append("住宿：").append(budgetNode.get("accommodation").asText()).append("\n");
                }
                if (budgetNode.has("meals")) {
                    budgetTip.append("餐饮：").append(budgetNode.get("meals").asText()).append("\n");
                }
                if (budgetNode.has("tickets")) {
                    budgetTip.append("门票：").append(budgetNode.get("tickets").asText()).append("\n");
                }
                if (budgetNode.has("other")) {
                    budgetTip.append("其他：").append(budgetNode.get("other").asText()).append("\n");
                }
                if (budgetNode.has("totalRange")) {
                    budgetTip.append("总预算范围：").append(budgetNode.get("totalRange").asText());
                }
                tips.add(budgetTip.toString());
            }

            // 添加打包清单到提示中
            if (rootNode.has("packingList")) {
                JsonNode packingNode = rootNode.get("packingList");
                StringBuilder packingTip = new StringBuilder("【必备行前准备与打包清单】\n");
                if (packingNode.has("documents") && packingNode.get("documents").isArray()) {
                    List<String> docs = new ArrayList<>();
                    for (JsonNode doc : packingNode.get("documents")) {
                        docs.add(doc.asText());
                    }
                    packingTip.append("证件类：").append(String.join("、", docs)).append("\n");
                }
                if (packingNode.has("clothing") && packingNode.get("clothing").isArray()) {
                    List<String> clothes = new ArrayList<>();
                    for (JsonNode cloth : packingNode.get("clothing")) {
                        clothes.add(cloth.asText());
                    }
                    packingTip.append("衣物类：").append(String.join("、", clothes)).append("\n");
                }
                if (packingNode.has("essentials") && packingNode.get("essentials").isArray()) {
                    List<String> essentials = new ArrayList<>();
                    for (JsonNode essential : packingNode.get("essentials")) {
                        essentials.add(essential.asText());
                    }
                    packingTip.append("用品类：").append(String.join("、", essentials)).append("\n");
                }
                if (packingNode.has("other") && packingNode.get("other").isArray()) {
                    List<String> others = new ArrayList<>();
                    for (JsonNode other : packingNode.get("other")) {
                        others.add(other.asText());
                    }
                    packingTip.append("其他：").append(String.join("、", others));
                }
                tips.add(packingTip.toString());
            }

            // 添加预订建议到提示中
            if (rootNode.has("bookingAdvice")) {
                JsonNode bookingNode = rootNode.get("bookingAdvice");
                StringBuilder bookingTip = new StringBuilder("【预订建议】\n");
                if (bookingNode.has("attractions") && bookingNode.get("attractions").isArray()) {
                    List<String> attractions = new ArrayList<>();
                    for (JsonNode attr : bookingNode.get("attractions")) {
                        attractions.add(attr.asText());
                    }
                    bookingTip.append("景点预订：").append(String.join("；", attractions)).append("\n");
                }
                if (bookingNode.has("restaurants") && bookingNode.get("restaurants").isArray()) {
                    List<String> restaurants = new ArrayList<>();
                    for (JsonNode rest : bookingNode.get("restaurants")) {
                        restaurants.add(rest.asText());
                    }
                    bookingTip.append("餐厅预订：").append(String.join("；", restaurants)).append("\n");
                }
                if (bookingNode.has("accommodation")) {
                    bookingTip.append("住宿预订：").append(bookingNode.get("accommodation").asText()).append("\n");
                }
                if (bookingNode.has("transportation")) {
                    bookingTip.append("交通预订：").append(bookingNode.get("transportation").asText());
                }
                tips.add(bookingTip.toString());
            }

            response.setTips(tips);

            log.info("成功解析AI响应: 标题={}, 行程天数={}, 提示数={}",
                    response.getTitle(), itinerary.size(), tips.size());
            return response;
    }

    /**
     * 尝试修复 JSON（当解析失败时）
     */
    private String attemptJsonFix(String jsonContent, Exception originalException) {
        if (jsonContent == null || jsonContent.isEmpty()) {
            return null;
        }

        // 如果错误信息包含 "expecting closing quote"，说明有未闭合的字符串
        if (originalException.getMessage().contains("expecting closing quote")) {
            log.warn("检测到未闭合的字符串，尝试修复");

            // 从错误信息中提取行号和列号（如果可用）
            String errorMsg = originalException.getMessage();
            int errorLine = 1;
            int errorCol = 0;

            // 尝试从错误信息中提取行号和列号
            // 错误信息格式: "... line: 226, column: 84]"
            try {
                // 查找 "line:" 后面的数字
                int lineIndex = errorMsg.indexOf("line:");
                if (lineIndex >= 0) {
                    int lineStart = lineIndex + 5;
                    // 跳过空格
                    while (lineStart < errorMsg.length() && Character.isWhitespace(errorMsg.charAt(lineStart))) {
                        lineStart++;
                    }
                    // 找到数字结束位置（逗号或空格或]）
                    int lineEnd = lineStart;
                    while (lineEnd < errorMsg.length() && Character.isDigit(errorMsg.charAt(lineEnd))) {
                        lineEnd++;
                    }
                    if (lineEnd > lineStart) {
                        errorLine = Integer.parseInt(errorMsg.substring(lineStart, lineEnd).trim());
                    }
                }

                // 查找 "column:" 后面的数字
                int colIndex = errorMsg.indexOf("column:");
                if (colIndex >= 0) {
                    int colStart = colIndex + 7;
                    // 跳过空格
                    while (colStart < errorMsg.length() && Character.isWhitespace(errorMsg.charAt(colStart))) {
                        colStart++;
                    }
                    // 找到数字结束位置（] 或空格）
                    int colEnd = colStart;
                    while (colEnd < errorMsg.length() && Character.isDigit(errorMsg.charAt(colEnd))) {
                        colEnd++;
                    }
                    if (colEnd > colStart) {
                        errorCol = Integer.parseInt(errorMsg.substring(colStart, colEnd).trim());
                    }
                }
        } catch (Exception e) {
                log.debug("无法从错误信息中提取行号和列号: {}", e.getMessage());
            }

            log.info("错误位置: 行 {}, 列 {}", errorLine, errorCol);

            // 方法1: 从后往前找到最后一个未闭合的字符串字段并移除
            StringBuilder fixed = new StringBuilder(jsonContent);

            // 从后往前扫描，找到最后一个 ":" 后面未闭合的字符串
            int lastColon = fixed.lastIndexOf(":");
            if (lastColon > 0) {
                // 找到最后一个字段的键名开始位置
                int keyStart = fixed.lastIndexOf("\"", lastColon);
                if (keyStart > 0) {
                    // 找到键名结束位置
                    int keyEnd = fixed.indexOf("\"", keyStart + 1);
                    if (keyEnd > keyStart && keyEnd < lastColon) {
                        // 找到这个字段的开始位置（向前查找逗号、大括号或中括号）
                        int fieldStart = -1;
                        for (int i = lastColon - 1; i >= 0; i--) {
                            char c = fixed.charAt(i);
                            if (c == ',') {
                                fieldStart = i + 1;
                                break;
                            } else if (c == '{' || c == '[') {
                                fieldStart = i;
                                break;
                            } else if (c == '}' || c == ']') {
                                // 跳过已闭合的对象或数组
                                break;
                            }
                        }

                        if (fieldStart >= 0) {
                            // 移除从字段开始到结尾的所有内容
                            int removeStart = fieldStart;
                            // 如果前面有换行，也一起移除
                            while (removeStart > 0 && Character.isWhitespace(fixed.charAt(removeStart - 1))) {
                                removeStart--;
                            }
                            fixed.delete(removeStart, fixed.length());
                            log.info("移除了最后一个不完整的字段（从位置 {} 开始），新长度: {}", removeStart, fixed.length());

                            // 确保 JSON 结构完整（添加缺失的闭合括号）
                            fixed = ensureJsonComplete(fixed);
                            return fixed.toString();
                        }
                    }
                }
            }

            // 方法2: 如果方法1失败，尝试从指定位置移除未闭合的字符串
            if (errorCol > 0) {
                // 计算实际字符位置（考虑换行符）
                int charPos = 0;
                int currentLine = 1;
                for (int i = 0; i < fixed.length() && currentLine < errorLine; i++) {
                    if (fixed.charAt(i) == '\n') {
                        currentLine++;
                    }
                    charPos = i;
                }
                charPos += errorCol;

                if (charPos < fixed.length()) {
                    // 向前查找这个字符串的开始位置
                    int stringStart = -1;
                    for (int i = charPos; i >= 0; i--) {
                        if (fixed.charAt(i) == '"' && (i == 0 || fixed.charAt(i - 1) != '\\')) {
                            // 检查这是字符串的开始还是结束
                            int colonBefore = fixed.lastIndexOf(":", i);
                            if (colonBefore > 0 && colonBefore < i) {
                                stringStart = i;
                                break;
                            }
                        }
                    }

                    if (stringStart > 0) {
                        // 找到字段开始位置
                        int fieldStart = fixed.lastIndexOf(",", stringStart);
                        if (fieldStart < 0) {
                            fieldStart = fixed.lastIndexOf("{", stringStart);
                            if (fieldStart < 0) {
                                fieldStart = fixed.lastIndexOf("[", stringStart);
                            }
                        }

                        if (fieldStart >= 0) {
                            fixed.delete(fieldStart + (fixed.charAt(fieldStart) == ',' ? 1 : 0), fixed.length());
                            fixed = ensureJsonComplete(fixed);
                            log.info("使用方法2修复，从位置 {} 移除，新长度: {}", fieldStart, fixed.length());
                            return fixed.toString();
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * 确保 JSON 结构完整（添加缺失的闭合括号）
     */
    private StringBuilder ensureJsonComplete(StringBuilder json) {
        int openBraces = 0;
        int openBrackets = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (!inString) {
                if (c == '{') {
                    openBraces++;
                } else if (c == '}') {
                    openBraces--;
                } else if (c == '[') {
                    openBrackets++;
                } else if (c == ']') {
                    openBrackets--;
                }
            }
        }

        // 移除末尾的逗号
        while (json.length() > 0 && (json.charAt(json.length() - 1) == ',' ||
               Character.isWhitespace(json.charAt(json.length() - 1)))) {
            json.deleteCharAt(json.length() - 1);
        }

        // 添加缺失的闭合括号
        for (int i = 0; i < openBrackets; i++) {
            json.append(']');
        }
        for (int i = 0; i < openBraces; i++) {
            json.append('}');
        }

        return json;
    }

    /**
     * 修复不完整的 JSON（如果响应被截断）
     */
    private String fixIncompleteJson(String jsonContent) {
        if (jsonContent == null || jsonContent.isEmpty()) {
            return jsonContent;
        }

        // 检查 JSON 是否完整（检查括号是否匹配）
        int openBraces = 0;
        int openBrackets = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < jsonContent.length(); i++) {
            char c = jsonContent.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (!inString) {
                if (c == '{') {
                    openBraces++;
                } else if (c == '}') {
                    openBraces--;
                } else if (c == '[') {
                    openBrackets++;
                } else if (c == ']') {
                    openBrackets--;
                }
            }
        }

        // 如果 JSON 不完整，尝试修复
        if (openBraces > 0 || openBrackets > 0 || inString) {
            log.warn("检测到不完整的 JSON，尝试修复。未闭合的括号: {}({}), []({})，未闭合的字符串: {}",
                    openBraces > 0 ? openBraces : 0,
                    openBrackets > 0 ? openBrackets : 0,
                    inString);

            // 从后往前查找最后一个不完整的字符串或对象
            StringBuilder fixed = new StringBuilder(jsonContent);

            // 如果字符串未闭合，需要找到最后一个未闭合的字符串并修复
            if (inString) {
                // 从后往前查找最后一个 ":" 后面跟着未闭合的字符串
                int lastColon = fixed.lastIndexOf(":");
                if (lastColon > 0) {
                    // 从 lastColon 之后查找字符串开始位置
                    int stringStart = -1;
                    for (int i = lastColon + 1; i < fixed.length(); i++) {
                        char c = fixed.charAt(i);
                        if (c == '"' && (i == 0 || fixed.charAt(i - 1) != '\\')) {
                            stringStart = i;
                            break;
                        }
                    }

                    // 如果找到了未闭合的字符串开始位置，关闭它
                    if (stringStart > 0) {
                        log.warn("发现未闭合的字符串，从位置 {} 开始，关闭字符串", stringStart);
                        // 在末尾添加闭合引号
                        fixed.append("\"");
                    } else {
                        // 如果没有找到引号，说明字符串值没有引号，需要添加
                        // 找到最后一个 ":" 的位置，在其后添加引号（如果还没有）
                        if (lastColon > 0 && lastColon < fixed.length() - 1) {
                            char afterColon = fixed.charAt(lastColon + 1);
                            if (afterColon != '"' && afterColon != ' ') {
                                // 在 ":" 后插入引号
                                fixed.insert(lastColon + 1, "\"");
                                // 在末尾添加闭合引号
                                fixed.append("\"");
                            }
                        }
                    }
                }
            }

            // 查找最后一个未闭合的字符串（即使 inString 为 false，也可能有未闭合的字符串）
            int lastColon = fixed.lastIndexOf(":");
            if (lastColon > 0) {
                // 检查从 lastColon 之后是否有未闭合的字符串
                boolean foundUnclosedString = false;
                int stringStart = -1;
                boolean inStringCheck = false;
                for (int i = lastColon + 1; i < fixed.length(); i++) {
                    char c = fixed.charAt(i);
                    if (c == '\\' && i < fixed.length() - 1) {
                        i++; // 跳过转义字符
                        continue;
                    }
                    if (c == '"') {
                        if (!inStringCheck) {
                            stringStart = i;
                            inStringCheck = true;
                        } else {
                            // 找到了闭合引号
                            stringStart = -1;
                            inStringCheck = false;
                        }
                    }
                }

                // 如果找到了未闭合的字符串，关闭它
                if (stringStart > 0 && inStringCheck) {
                    log.warn("发现未闭合的字符串，从位置 {} 开始，关闭字符串", stringStart);
                    fixed.append("\"");
                }
            }

            // 移除最后一个不完整的字段（如果以逗号结尾）
            while (fixed.length() > 0 && (fixed.charAt(fixed.length() - 1) == ',' ||
                   Character.isWhitespace(fixed.charAt(fixed.length() - 1)))) {
                fixed.deleteCharAt(fixed.length() - 1);
            }

            // 添加缺失的闭合括号
            for (int i = 0; i < openBrackets; i++) {
                fixed.append(']');
            }
            for (int i = 0; i < openBraces; i++) {
                fixed.append('}');
            }

            log.info("修复后的 JSON 长度: {} 字符（原长度: {} 字符）", fixed.length(), jsonContent.length());
            return fixed.toString();
        }

        return jsonContent;
    }

    /**
     * 清理 JSON 内容，移除可能导致解析错误的中文标点符号和其他非标准字符
     */
    private String cleanJsonContent(String jsonContent) {
        if (jsonContent == null || jsonContent.isEmpty()) {
            return jsonContent;
        }

        // 统一去掉 Markdown 代码块围栏（无论 parse 阶段是否处理过）
        jsonContent = stripCodeFences(jsonContent);

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

        // 第三步：修复常见的时间格式错误（如 19:"00）
        jsonContent = fixCommonTimeTypos(jsonContent);

        // 第四步：修复缺少逗号和未转义引号的问题
        jsonContent = fixMissingCommasAndQuotes(jsonContent);

        // 第五步：尝试解析验证，如果失败则记录警告但不抛出异常
        // 注意：这里不抛出异常，让后续的修复逻辑可以继续执行
        try {
            objectMapper.readTree(jsonContent);
            return jsonContent;
        } catch (Exception e) {
            log.warn("JSON 清理后仍无法解析，将在后续步骤中尝试修复。错误位置: line {}, column {}",
                    e.getMessage().contains("line") ? extractLineNumber(e.getMessage()) : "unknown",
                    e.getMessage().contains("column") ? extractColumnNumber(e.getMessage()) : "unknown");
            // 不抛出异常，返回清理后的内容，让后续的修复逻辑处理
            return jsonContent;
        }
    }

    private String fixCommonTimeTypos(String jsonContent) {
        if (!StringUtils.hasText(jsonContent)) {
            return jsonContent;
        }
        String fixed = jsonContent.replaceAll("(\\d{1,2}):\"(\\d{2})", "$1:$2\"");
        fixed = fixed.replaceAll("(\\d{1,2}):(\\d{2})-\\s*(\\d{1,2}):\"(\\d{2})", "$1:$2-$3:$4\"");
        return fixed;
    }

    /**
     * 修复缺少逗号和未转义引号的问题
     * 处理常见的 JSON 格式错误：
     * 1. 缺少逗号分隔符（如 "key1" "key2"）
     * 2. 字符串中未转义的引号
     */
    private String fixMissingCommasAndQuotes(String jsonContent) {
        if (!StringUtils.hasText(jsonContent)) {
            return jsonContent;
        }

        StringBuilder fixed = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < jsonContent.length(); i++) {
            char c = jsonContent.charAt(i);

            if (escaped) {
                fixed.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                fixed.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                if (inString) {
                    // 检查是否是字符串结束
                    // 跳过空白字符，查看下一个非空白字符
                    int j = i + 1;
                    while (j < jsonContent.length() && Character.isWhitespace(jsonContent.charAt(j))) {
                        j++;
                    }

                    if (j >= jsonContent.length()) {
                        // 字符串在末尾，结束字符串
                        inString = false;
                        fixed.append(c);
                    } else {
                        char nextChar = jsonContent.charAt(j);
                        // 如果下一个字符是 : , } ] 或换行，则字符串结束
                        if (nextChar == ':' || nextChar == ',' || nextChar == '}' ||
                            nextChar == ']' || nextChar == '\n' || nextChar == '\r') {
                            inString = false;
                            fixed.append(c);
                        } else {
                            // 字符串中的引号，需要转义
                            fixed.append("\\\"");
                        }
                    }
                } else {
                    // 字符串开始
                    inString = true;
                    fixed.append(c);
                }
                continue;
            }

            fixed.append(c);
        }

        String result = fixed.toString();

        // 修复常见的缺少逗号的模式（仅在字符串外）
        // 使用更精确的正则，避免在字符串内替换
        // 模式1: "key" "value" -> "key", "value"
        result = result.replaceAll("(\")\\s+(?=\")", "$1,");
        // 模式2: "key" { -> "key", {
        result = result.replaceAll("(\")\\s+(?=\\{)", "$1,");
        // 模式3: "key" [ -> "key", [
        result = result.replaceAll("(\")\\s+(?=\\[)", "$1,");
        // 模式4: } "key" -> }, "key"
        result = result.replaceAll("(\\})\\s+(?=\")", "$1,");
        // 模式5: ] "key" -> ], "key"
        result = result.replaceAll("(\\])\\s+(?=\")", "$1,");

        return result;
    }

    /**
     * 去掉前后 ```json/``` 包裹，兼容 ```JSON、``` 等不同写法
     */
    private String stripCodeFences(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            int firstLineBreak = trimmed.indexOf('\n');
            if (firstLineBreak > 0) {
                trimmed = trimmed.substring(firstLineBreak + 1);
            } else {
                trimmed = trimmed.substring(3);
            }
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.lastIndexOf("```"));
        }
        return trimmed.trim();
    }

    /**
     * 修复常见的 JSON 结构错误（括号、方括号不匹配等）
     */
    private String repairJsonStructure(String jsonContent) {
        if (!StringUtils.hasText(jsonContent)) {
            return jsonContent;
        }

        StringBuilder builder = new StringBuilder();
        Deque<Character> stack = new ArrayDeque<>();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < jsonContent.length(); i++) {
            char c = jsonContent.charAt(i);

            if (escaped) {
                builder.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                builder.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                builder.append(c);
                inString = !inString;
                continue;
            }

            if (inString) {
                builder.append(c);
                continue;
            }

            if (c == '{') {
                stack.push('}');
                builder.append(c);
                continue;
            }
            if (c == '[') {
                stack.push(']');
                builder.append(c);
                continue;
            }

            if (c == '}' || c == ']') {
                if (stack.isEmpty()) {
                    // 忽略多余的闭合符号
                    continue;
                }
                char expected = stack.peek();
                if (c == expected) {
                    stack.pop();
                    builder.append(c);
                } else {
                    // 将错误的闭合符号替换为期望的符号，并重新处理当前字符
                    builder.append(expected);
                    stack.pop();
                    i--;
                }
                continue;
            }

            builder.append(c);
        }

        // 如果字符串未闭合，自动补齐
        if (inString) {
            builder.append('"');
            inString = false;
        }

        while (!stack.isEmpty()) {
            builder.append(stack.pop());
        }

        return builder.toString();
    }

    /**
     * 从描述中提取注意事项
     * 查找包含"注意事项"、"注意"等关键词的内容
     */
    private String extractNotes(String description) {
        if (description == null || description.isEmpty()) {
            return null;
        }

        // 查找"注意事项"关键词
        String[] noteKeywords = {"注意事项", "注意", "温馨提示", "特别提醒"};
        for (String keyword : noteKeywords) {
            int index = description.indexOf(keyword);
            if (index >= 0) {
                // 找到关键词，提取后面的内容
                String afterKeyword = description.substring(index + keyword.length()).trim();

                // 移除可能的冒号、句号等标点
                if (afterKeyword.startsWith(":") || afterKeyword.startsWith("：")) {
                    afterKeyword = afterKeyword.substring(1).trim();
                }
                if (afterKeyword.startsWith(".") || afterKeyword.startsWith("。")) {
                    afterKeyword = afterKeyword.substring(1).trim();
                }

                // 提取到句号、换行或结尾
                int endIndex = afterKeyword.length();
                int periodIndex = afterKeyword.indexOf("。");
                int newlineIndex = afterKeyword.indexOf("\n");
                int periodIndex2 = afterKeyword.indexOf(".");

                if (periodIndex > 0 && periodIndex < endIndex) {
                    endIndex = periodIndex + 1;
                }
                if (newlineIndex > 0 && newlineIndex < endIndex) {
                    endIndex = newlineIndex;
                }
                if (periodIndex2 > 0 && periodIndex2 < endIndex) {
                    endIndex = periodIndex2 + 1;
                }

                String notes = afterKeyword.substring(0, endIndex).trim();
                if (!notes.isEmpty()) {
                    return notes;
                }
            }
        }

        // 如果没有找到明确的关键词，尝试查找包含"不要"、"禁止"等提示性词语的句子
        String[] warningKeywords = {"不要", "禁止", "请勿", "避免", "需注意"};
        for (String keyword : warningKeywords) {
            int index = description.indexOf(keyword);
            if (index >= 0) {
                // 向前查找句子开始（句号、换行或开头）
                int startIndex = 0;
                int prevPeriod = description.lastIndexOf("。", index);
                int prevNewline = description.lastIndexOf("\n", index);
                int prevPeriod2 = description.lastIndexOf(".", index);

                if (prevPeriod > startIndex) {
                    startIndex = prevPeriod + 1;
                }
                if (prevNewline > startIndex) {
                    startIndex = prevNewline + 1;
                }
                if (prevPeriod2 > startIndex) {
                    startIndex = prevPeriod2 + 1;
                }

                // 向后查找句子结束
                int endIndex = description.length();
                int nextPeriod = description.indexOf("。", index);
                int nextNewline = description.indexOf("\n", index);
                int nextPeriod2 = description.indexOf(".", index);

                if (nextPeriod > index && nextPeriod < endIndex) {
                    endIndex = nextPeriod + 1;
                }
                if (nextNewline > index && nextNewline < endIndex) {
                    endIndex = nextNewline;
                }
                if (nextPeriod2 > index && nextPeriod2 < endIndex) {
                    endIndex = nextPeriod2 + 1;
                }

                String notes = description.substring(startIndex, endIndex).trim();
                if (!notes.isEmpty() && notes.length() < 200) { // 限制长度，避免提取过多内容
                    return notes;
                }
            }
        }

        return null;
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
     * 当 AI JSON 解析失败时构建兜底路线，并附加提示信息
     */
    private AIRouteResponse buildFallbackRoute(GenerateRouteRequest request, String reason) {
        AIRouteResponse fallback = generateDefaultRoute(request);

        String desc = fallback.getDescription();
        if (!StringUtils.hasText(desc)) {
            desc = "根据系统默认策略生成的新疆线路建议。";
        }
        fallback.setDescription(desc + "（AI响应格式异常，已提供系统兜底路线，建议稍后重试以获取完整方案。）");

        List<String> tips = fallback.getTips() != null ? new ArrayList<>(fallback.getTips()) : new ArrayList<>();
        String message = "AI生成内容解析失败，系统已提供通用路线方案";
        if (StringUtils.hasText(reason)) {
            message += "（" + reason + "）";
        }
        message += "。如需更精细的规划，请稍后再次尝试。";
        tips.add(0, message);
        fallback.setTips(tips);

        return fallback;
    }

    /**
     * 生成默认路线（当AI服务不可用时）
     */
    private AIRouteResponse generateDefaultRoute(GenerateRouteRequest request) {
        // 提取起点和终点
        String startLocation = request.getStartLocation();
        String endLocation = request.getEndLocation();
        if (request.getDestinations() != null && !request.getDestinations().isEmpty()) {
            String[] cities = request.getDestinations().split("[,，→]|->");
            if (cities.length > 0) {
                startLocation = cities[0].trim();
                if (cities.length > 1) {
                    endLocation = cities[cities.length - 1].trim();
                } else {
                    endLocation = startLocation;
                }
            }
        }
        if (startLocation == null || startLocation.isEmpty()) {
            startLocation = "起点";
        }
        if (endLocation == null || endLocation.isEmpty()) {
            endLocation = "终点";
        }

        AIRouteResponse response = new AIRouteResponse();
        response.setTitle("从" + startLocation + "到" + endLocation + "的路线");
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
                dayTitle = "出发日 - 探索" + startLocation;
                dayDescription = "从" + startLocation + "出发，开始您的精彩旅程。";
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
                dayTitle = "返程日 - 抵达" + endLocation;
                dayDescription = "前往" + endLocation + "，结束愉快的旅程。";
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
                item.setAccommodation("建议在" + startLocation + "附近预订酒店或民宿");
            } else {
                item.setAccommodation("在" + endLocation + "住宿");
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

    /**
     * 校验 AI 返回的行程天数是否满足用户请求
     */
    private void validateItineraryCompleteness(AIRouteResponse response, GenerateRouteRequest request) {
        if (response == null) {
            throw new IncompleteItineraryException("AI响应为空");
        }
        Integer expectedDuration = request.getDuration();
        if (expectedDuration == null || expectedDuration <= 0) {
            return;
        }
        List<AIRouteResponse.ItineraryItem> itinerary = response.getItinerary();
        if (itinerary == null || itinerary.isEmpty()) {
            throw new IncompleteItineraryException("AI响应未包含行程数据");
        }

        Set<Integer> distinctDays = new HashSet<>();
        int maxDay = 0;
        for (AIRouteResponse.ItineraryItem item : itinerary) {
            if (item == null || item.getDay() == null) {
                continue;
            }
            distinctDays.add(item.getDay());
            if (item.getDay() > maxDay) {
                maxDay = item.getDay();
            }
        }

        if (distinctDays.isEmpty()) {
            throw new IncompleteItineraryException("AI响应缺少有效的行程天数信息");
        }

        if (distinctDays.size() < expectedDuration || maxDay < expectedDuration) {
            throw new IncompleteItineraryException(
                    "请求了" + expectedDuration + "天行程，AI仅返回" + distinctDays.size() + "天（最大第" + maxDay + "天）");
        }
    }

    private void safeRetryPause() {
        try {
            Thread.sleep(900L);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * AI文化讲解助手
     */
    public AiExplainResponse explainCulture(AiExplainRequest request) {
        String apiKey;
        String apiUrl;
        String model;
        String providerName;

        if ("deepseek".equalsIgnoreCase(provider)) {
            apiKey = deepseekApiKey;
            apiUrl = deepseekApiUrl;
            model = deepseekModel;
            providerName = "DeepSeek";
        } else {
            apiKey = openaiApiKey;
            apiUrl = openaiApiUrl;
            model = openaiModel;
            providerName = "OpenAI";
        }

        if (isApiKeyMissing(apiKey)) {
            log.warn("AI讲解使用默认逻辑，原因：{} API key 未配置", providerName);
            return buildLocalExplanation(request);
        }

        try {
            String prompt = buildExplainPrompt(request);

            OpenAIRequest aiRequest = new OpenAIRequest();
            aiRequest.setModel(model);
            OpenAIRequest.Message system = new OpenAIRequest.Message();
            system.setRole("system");
            system.setContent("你是新疆数字文化平台的高级讲解员，需要以学术而生动的语言讲述新疆文化。");
            OpenAIRequest.Message message = new OpenAIRequest.Message();
            message.setRole("user");
            message.setContent(prompt);
            aiRequest.setMessages(List.of(system, message));
            aiRequest.setTemperature(0.4);
            aiRequest.setMaxTokens(2000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ResponseEntity<OpenAIResponse> responseEntity = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(aiRequest, headers),
                    OpenAIResponse.class
            );

            OpenAIResponse body = responseEntity.getBody();
            if (body == null || body.getChoices() == null || body.getChoices().isEmpty()) {
                throw new RuntimeException("AI讲解返回空响应");
            }

            String content = body.getChoices().get(0).getMessage().getContent();
            return parseExplainResponse(content, request);
        } catch (Exception e) {
            log.error("AI讲解接口调用失败: {}", e.getMessage(), e);
            return buildLocalExplanation(request);
        }
    }

    private String buildExplainPrompt(AiExplainRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请以资深新疆文化讲解员身份，对以下内容进行深入介绍。");
        if (StringUtils.hasText(request.getQuery())) {
            prompt.append("\n主题：").append(request.getQuery());
        }
        if (StringUtils.hasText(request.getContext())) {
            prompt.append("\n补充背景：").append(request.getContext());
        }
        if (StringUtils.hasText(request.getImageUrl())) {
            prompt.append("\n用户提供了图像链接（可用于推测展品）：").append(request.getImageUrl());
        }
        if (StringUtils.hasText(request.getAudience())) {
            prompt.append("\n目标受众：").append(request.getAudience());
            prompt.append("。请根据该受众的知识结构与兴趣点调整讲解视角与案例。");
        }
        if (StringUtils.hasText(request.getTone())) {
            prompt.append("\n讲解风格偏好：").append(request.getTone());
        }
        if (StringUtils.hasText(request.getLength())) {
            switch (request.getLength()) {
                case "short" -> prompt.append("\n篇幅要求：浓缩为 200-300 字，突出核心史料与结论。");
                case "detailed" -> prompt.append("\n篇幅要求：不少于 500 字，需包含历史脉络、艺术特征、传承现状与互动提问。");
                default -> prompt.append("\n篇幅要求：约 350-400 字，兼顾信息密度与可读性。");
            }
        } else {
            prompt.append("\n篇幅要求：约 350-400 字，兼顾信息密度与可读性。");
        }
        if (request.getFocusPoints() != null && !request.getFocusPoints().isEmpty()) {
            prompt.append("\n重点请围绕以下维度展开：")
                    .append(String.join("、", request.getFocusPoints()))
                    .append("，并给出与这些维度相关的例证。");
        }
        prompt.append("\n请按照 JSON 输出：");
        prompt.append("{\"title\":\"简洁标题\",\"summary\":\"300字概述\",\"highlights\":[\"要点1\",\"要点2\"],");
        prompt.append("\"references\":[\"出处1\",\"出处2\"],\"mediaInsight\":\"如果有图像，描述图像细节及文化意义\"}");
        prompt.append("\n必须使用中文输出，兼顾学术性与可读性。");
        return prompt.toString();
    }

    private AiExplainResponse parseExplainResponse(String content, AiExplainRequest request) {
        try {
            String jsonContent = extractJsonBlock(content);
            String cleaned = cleanJsonContent(jsonContent);
            JsonNode rootNode = objectMapper.readTree(cleaned);
            return AiExplainResponse.builder()
                    .title(rootNode.hasNonNull("title") ? rootNode.get("title").asText() : fallbackTitle(request))
                    .summary(rootNode.hasNonNull("summary") ? rootNode.get("summary").asText() : defaultSummary(request))
                    .highlights(rootNode.has("highlights") && rootNode.get("highlights").isArray()
                            ? objectMapper.convertValue(rootNode.get("highlights"), objectMapper.getTypeFactory().constructCollectionType(List.class, String.class))
                            : List.of("新疆文化底蕴深厚，建议结合多媒体内容进行沉浸式体验。"))
                    .references(rootNode.has("references") && rootNode.get("references").isArray()
                            ? objectMapper.convertValue(rootNode.get("references"), objectMapper.getTypeFactory().constructCollectionType(List.class, String.class))
                            : List.of("新疆数字文化平台·AI助手"))
                    .mediaInsight(rootNode.hasNonNull("mediaInsight") ? rootNode.get("mediaInsight").asText()
                            : "如果有相关展品图像，可从纹理、工艺、色彩等角度进行讲解。")
                    .build();
        } catch (Exception e) {
            log.warn("解析AI讲解响应失败，使用本地降级内容: {}", e.getMessage());
            return buildLocalExplanation(request);
        }
    }

    private String extractJsonBlock(String content) {
        String trimmed = content != null ? content.trim() : "";
        if (trimmed.contains("```json")) {
            int start = trimmed.indexOf("```json") + 7;
            int end = trimmed.indexOf("```", start);
            if (end > start) {
                return trimmed.substring(start, end);
            }
        } else if (trimmed.startsWith("{")) {
            return trimmed;
        }
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }
        return "{\"title\":\"新疆文化讲解\",\"summary\":\"" + trimmed + "\"}";
    }

    private AiExplainResponse buildLocalExplanation(AiExplainRequest request) {
        return AiExplainResponse.builder()
                .title(fallbackTitle(request))
                .summary(defaultSummary(request))
                .highlights(List.of(
                        "新疆拥有多民族交融的文化形态，可结合音乐、舞蹈、手工艺进行展示。",
                        "建议关联平台内的非遗专题与地图地标，形成沉浸式体验。"))
                .references(List.of("新疆数字文化平台内容库"))
                .mediaInsight(StringUtils.hasText(request.getImageUrl())
                        ? "图像可重点描述纹理、颜色与工艺特征，引导用户放大细节观察。"
                        : "可配合权威图片或3D模型，增强理解体验。")
                .build();
    }

    private String fallbackTitle(AiExplainRequest request) {
        if (StringUtils.hasText(request.getQuery())) {
            return request.getQuery() + " · 新疆文化解读";
        }
        return "新疆文化讲解";
    }

    private String defaultSummary(AiExplainRequest request) {
        if (StringUtils.hasText(request.getQuery())) {
            return "围绕“" + request.getQuery() + "”的核心元素，介绍其历史背景、艺术价值与在新疆文化体系中的地位。";
        }
        return "通过AI助手介绍新疆代表性文化内容，涵盖历史沿革、地域特色及现代传承路径。";
    }

    private boolean isApiKeyMissing(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            return true;
        }
        String normalized = apiKey.trim();
        return "your-deepseek-api-key".equalsIgnoreCase(normalized)
                || "your-openai-api-key".equalsIgnoreCase(normalized);
    }

    private static class IncompleteItineraryException extends RuntimeException {
        public IncompleteItineraryException(String message) {
            super(message);
        }
    }

    private static class JsonFormatException extends RuntimeException {
        public JsonFormatException(String message, Throwable cause) {
            super(message, cause);
        }

        public JsonFormatException(String message) {
            super(message);
        }
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
            private String transportation;
            private String timeSchedule;
            private String dailyBudget;
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

