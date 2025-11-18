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
            aiRequest.setMaxTokens(8000); // 增加最大 token 数，允许生成更详细的路线（从4000增加到8000）

            // 发送请求到 AI API
            log.debug("调用 {} API: {}", provider, apiUrl);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<OpenAIRequest> requestEntity = new HttpEntity<>(aiRequest, headers);

            // ========== 输出发送给 DeepSeek 的请求数据 ==========
            String requestJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(aiRequest);
            log.info("========== 发送给 DeepSeek API 的请求数据 ==========");
            log.info("API URL: {}", apiUrl);
            log.info("API Provider: {}", provider);
            log.info("请求体:\n{}", requestJson);
            log.info("================================================");

            // 同时输出到控制台
            System.out.println("\n========== 发送给 DeepSeek API 的请求数据 ==========");
            System.out.println("API URL: " + apiUrl);
            System.out.println("API Provider: " + provider);
            System.out.println("请求体:");
            System.out.println(requestJson);
            System.out.println("================================================\n");

            // 发送请求
            log.info("正在调用 {} API 生成路线，URL: {}", provider, apiUrl);

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

            // ========== 输出 DeepSeek 返回的完整原始数据 ==========
            log.info("========== DeepSeek API 返回的完整原始数据 ==========");
            log.info("响应状态码: {}", responseEntity.getStatusCode());
            log.info("完整响应内容:\n{}", content);
            log.info("响应内容长度: {} 字符", content.length());
            log.info("================================================");

            // 同时输出到控制台（System.out）以便查看
            System.out.println("\n========== DeepSeek API 返回的完整原始数据 ==========");
            System.out.println("响应状态码: " + responseEntity.getStatusCode());
            System.out.println("完整响应内容:");
            System.out.println(content);
            System.out.println("响应内容长度: " + content.length() + " 字符");
            System.out.println("================================================\n");

            // 解析AI返回的JSON
            AIRouteResponse aiRouteResponse = parseAIResponse(content, request);

            // ========== 输出解析后的结构化数据 ==========
            log.info("========== 解析后的结构化数据 ==========");
            log.info("路线标题: {}", aiRouteResponse.getTitle());
            log.info("路线描述: {}", aiRouteResponse.getDescription());
            log.info("行程天数: {}", aiRouteResponse.getItinerary() != null ? aiRouteResponse.getItinerary().size() : 0);
            log.info("提示数量: {}", aiRouteResponse.getTips() != null ? aiRouteResponse.getTips().size() : 0);

            if (aiRouteResponse.getItinerary() != null) {
                for (AIRouteResponse.ItineraryItem item : aiRouteResponse.getItinerary()) {
                    log.info("第{}天 - 标题: {}", item.getDay(), item.getTitle());
                    log.info("  描述: {}", item.getDescription());
                    log.info("  景点数量: {}", item.getLocations() != null ? item.getLocations().size() : 0);
                    if (item.getLocations() != null) {
                        for (AIRouteResponse.Location loc : item.getLocations()) {
                            log.info("    景点: {} (lat: {}, lng: {})", loc.getName(), loc.getLat(), loc.getLng());
                            if (loc.getDescription() != null) {
                                log.info("      描述: {}", loc.getDescription());
                            }
                        }
                    }
                    log.info("  住宿: {}", item.getAccommodation());
                    log.info("  餐饮: {}", item.getMeals());
                }
            }

            if (aiRouteResponse.getTips() != null) {
                log.info("提示信息:");
                for (String tip : aiRouteResponse.getTips()) {
                    log.info("  - {}", tip);
                }
            }

            // 输出解析后的JSON格式数据到控制台
            try {
                String jsonOutput = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(aiRouteResponse);
                System.out.println("\n========== 解析后的结构化数据 (JSON格式) ==========");
                System.out.println(jsonOutput);
                System.out.println("================================================\n");
            } catch (Exception e) {
                log.warn("无法将解析后的数据转换为JSON: {}", e.getMessage());
            }

            log.info("==========================================");
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

        prompt.append("你是专业旅行规划师。请为以下信息生成一个【详尽且可执行】的行程方案，要求详列每天活动、建议时间、交通方式、预估费用（分门别类）、推荐住宿区并给 3 个不同价位的酒店/民宿建议、以及每项活动的优先等级和替代方案。\n\n");

        prompt.append("输出应包含：\n");
        prompt.append("1) 每日行程表（时间轴）\n");
        prompt.append("2) 每日预算汇总\n");
        prompt.append("3) 必备行前准备与 packing list\n");
        prompt.append("4) 预订建议（是否需要提前买票/预约）\n");
        prompt.append("5) 可导入 Google Maps 的地标坐标或链接\n\n");

        prompt.append("必须包含以下完整信息：\n");
        prompt.append("1. 路线标题（简洁吸引人，突出特色，体现路线主题，必须包含起点和终点城市名称）\n");
        prompt.append("   示例：\"乌鲁木齐到喀什的丝路文化之旅\"、\"探索新疆：从乌鲁木齐到伊犁的7日深度游\"\n");
        prompt.append("2. 路线描述（300-500字，必须包含：路线亮点、适合人群、最佳旅游季节、路线特色、文化背景、预期体验）\n");
        prompt.append("3. 每日行程安排（按天详细列出，每天必须包含以下完整信息）：\n");
        prompt.append("   - 每日标题（突出当日主题和核心体验，如：\"第1天 - 探索乌鲁木齐，感受丝路起点\"）\n");
        prompt.append("   - 每日详细描述（200-300字，必须包含：当日行程概述、主要活动、文化体验、亮点推荐、注意事项）\n");
        prompt.append("   - 具体时间安排（必须详细到小时，例如：\n");
        prompt.append("     * 08:00-09:00 酒店早餐，准备出发\n");
        prompt.append("     * 09:00-10:30 前往XX景点（交通时间30分钟，游览时间1小时）\n");
        prompt.append("     * 10:30-12:00 游览XX景点（详细说明游览内容和体验）\n");
        prompt.append("     * 12:00-13:30 午餐时间（推荐餐厅和菜品）\n");
        prompt.append("     * 13:30-17:00 继续游览其他景点\n");
        prompt.append("     * 17:00-18:00 休息或自由活动\n");
        prompt.append("     * 18:00-19:30 晚餐时间\n");
        prompt.append("     * 19:30-21:00 夜游或文化体验活动\n");
        prompt.append("     * 21:00 返回酒店休息）\n");
        prompt.append("   - 景点信息（每个景点必须包含完整信息）：\n");
        prompt.append("     * 景点名称（必须是新疆真实存在的景点）\n");
        prompt.append("     * 详细描述（100-150字，包含：景点历史背景、文化意义、自然特色、游览亮点、拍照打卡点、最佳观赏角度）\n");
        prompt.append("     * 开放时间（具体时间段，如：夏季08:00-20:00，冬季09:00-18:00）\n");
        prompt.append("     * 门票价格（详细价格信息：成人票、学生票、儿童票、优惠政策）\n");
        prompt.append("     * 最佳游览时间（具体时间段和季节，如：上午9-11点光线最佳，秋季色彩最美）\n");
        prompt.append("     * 建议游览时长（如：深度游览3-4小时，快速游览1-2小时）\n");
        prompt.append("     * 游览路线建议（推荐游览顺序和路线）\n");
        prompt.append("     * 特色活动（如：骑马、拍照、体验民族风情等）\n");
        prompt.append("     * 注意事项（如：需要携带物品、禁止事项等）\n");
        prompt.append("     * 优先等级（必去/推荐/可选）\n");
        prompt.append("     * 替代方案（如果该景点不可行，推荐的替代景点）\n");
        prompt.append("     * 精确的经纬度坐标（lat和lng，必须是新疆境内的真实坐标）\n");
        prompt.append("     * Google Maps 链接（格式：https://maps.google.com/?q=lat,lng）\n");
        prompt.append("   - 交通信息（必须详细说明）：\n");
        prompt.append("     * 景点之间的交通方式（自驾/包车/公共交通/步行）\n");
        prompt.append("     * 具体距离（公里数）\n");
        prompt.append("     * 预计时间（包含堵车、休息等因素）\n");
        prompt.append("     * 费用（如：包车500元/天，公共交通20元/人）\n");
        prompt.append("     * 路况说明（如：山路较多、注意安全）\n");
        prompt.append("     * 停车信息（如：景区停车场位置和费用）\n");
        prompt.append("   - 餐饮安排（每餐必须详细说明）：\n");
        prompt.append("     * 具体餐厅名称、地址或推荐餐厅类型（如：XX餐厅位于XX路，XX美食街）\n");
        prompt.append("     * 推荐菜品（3-5道特色菜，说明菜品特色和口味）\n");
        prompt.append("     * 人均消费范围（如：早餐30-50元/人，午餐80-120元/人，晚餐100-150元/人）\n");
        prompt.append("     * 餐厅特色说明（如：当地老字号、民族特色、环境优雅、服务周到）\n");
        prompt.append("     * 用餐时间建议（如：避开用餐高峰期）\n");
        prompt.append("     * 特殊饮食需求（如：清真餐厅、素食选择）\n");
        prompt.append("   - 住宿安排（必须详细说明，提供3个不同价位的选择）：\n");
        prompt.append("     * 推荐住宿区域（如：市中心、景区附近、交通便利区）\n");
        prompt.append("     * 经济型选择：酒店名称、地址、价格、特色、预订建议\n");
        prompt.append("     * 舒适型选择：酒店名称、地址、价格、特色、预订建议\n");
        prompt.append("     * 豪华型选择：酒店名称、地址、价格、特色、预订建议\n");
        prompt.append("     * 酒店位置优势（如：距离XX景点X公里，步行X分钟，交通便利，周边设施齐全）\n");
        prompt.append("   - 每日活动建议（除了景点游览外的活动）：\n");
        prompt.append("     * 文化体验活动（如：观看民族歌舞、体验手工艺制作）\n");
        prompt.append("     * 休闲活动（如：购物、SPA、夜游）\n");
        prompt.append("     * 拍照打卡点（如：最佳拍照位置和时间）\n");
        prompt.append("   - 每日预算分配（详细列出各项费用）：\n");
        prompt.append("     * 交通费用（具体金额）\n");
        prompt.append("     * 餐饮费用（早餐、午餐、晚餐分别列出）\n");
        prompt.append("     * 住宿费用（具体金额）\n");
        prompt.append("     * 门票费用（各景点门票合计）\n");
        prompt.append("     * 其他费用（购物、娱乐等）\n");
        prompt.append("     * 每日总预算\n");
        prompt.append("4. 总体预算分配建议（按类别详细说明）：\n");
        prompt.append("   - 交通费用（往返交通、当地交通、详细说明）\n");
        prompt.append("   - 住宿费用（按天数计算，不同档次选择）\n");
        prompt.append("   - 餐饮费用（按天数计算，不同档次选择）\n");
        prompt.append("   - 门票费用（各景点门票合计）\n");
        prompt.append("   - 其他费用（购物、娱乐、保险等）\n");
        prompt.append("   - 总预算范围（最低预算和舒适预算）\n");
        prompt.append("5. 必备行前准备与 Packing List（详细列出）：\n");
        prompt.append("   - 证件类（身份证、边防证、学生证等）\n");
        prompt.append("   - 衣物类（根据季节和天气，列出具体衣物清单）\n");
        prompt.append("   - 用品类（防晒用品、药品、电子设备等）\n");
        prompt.append("   - 其他必需品\n");
        prompt.append("6. 预订建议（详细说明）：\n");
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
        prompt.append("          \"description\": \"景点详细描述（100-150字，包含：历史背景、文化意义、自然特色、游览亮点、拍照打卡点、最佳观赏角度、开放时间、门票价格、最佳游览时间、建议游览时长、游览路线建议、特色活动、注意事项）\",\n");
        prompt.append("          \"priority\": \"必去/推荐/可选\",\n");
        prompt.append("          \"alternative\": \"替代景点名称（如果该景点不可行）\",\n");
        prompt.append("          \"googleMapsLink\": \"https://maps.google.com/?q=43.8833,88.1333\"\n");
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
                            if (locNode.has("description")) {
                                locDescBuilder.append(locNode.get("description").asText());
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

                            // 解析Google Maps链接
                            if (locNode.has("googleMapsLink") && !locNode.get("googleMapsLink").isNull()) {
                                String mapsLink = locNode.get("googleMapsLink").asText();
                                if (!mapsLink.isEmpty()) {
                                    locDescBuilder.append("\n【地图链接】").append(mapsLink);
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

