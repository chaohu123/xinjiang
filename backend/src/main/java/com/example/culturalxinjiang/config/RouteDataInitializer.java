package com.example.culturalxinjiang.config;

import com.example.culturalxinjiang.entity.Route;
import com.example.culturalxinjiang.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 路线数据初始化组件
 * 在应用启动时自动创建示例路线数据
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RouteDataInitializer implements CommandLineRunner {

    private final RouteRepository routeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 检查是否已有路线数据
        if (routeRepository.count() > 0) {
            log.info("路线数据已存在，跳过初始化");
            return;
        }

        log.info("开始初始化示例路线数据...");

        List<Route> sampleRoutes = new ArrayList<>();

        // 示例路线1：丝绸之路文化之旅
        Route route1 = Route.builder()
                .title("丝绸之路文化之旅")
                .description("探索新疆丝绸之路的历史文化，感受千年文明的魅力。从乌鲁木齐出发，途经吐鲁番、库尔勒，最终到达喀什，体验多元文化的交融。")
                .cover("https://images.unsplash.com/photo-1508804185872-d7badad00f7d?w=800")
                .theme("silkRoad")
                .duration(7)
                .distance(1200.0)
                .startLocation("乌鲁木齐")
                .endLocation("喀什")
                .waypoints(15)
                .views(1250)
                .favorites(89)
                .user(null) // 示例路线没有创建者
                .createdAt(LocalDateTime.now().minusDays(30))
                .updatedAt(LocalDateTime.now().minusDays(30))
                .build();
        route1.setItinerary(new ArrayList<>());
        route1.setTips(new ArrayList<>());
        sampleRoutes.add(route1);

        // 示例路线2：天山天池自然风光
        Route route2 = Route.builder()
                .title("天山天池自然风光之旅")
                .description("领略天山天池的壮美景色，感受大自然的鬼斧神工。适合喜欢自然风光的旅行者，包含徒步、摄影等多种体验。")
                .cover("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800")
                .theme("nature")
                .duration(3)
                .distance(150.0)
                .startLocation("乌鲁木齐")
                .endLocation("天山天池")
                .waypoints(8)
                .views(980)
                .favorites(67)
                .user(null)
                .createdAt(LocalDateTime.now().minusDays(25))
                .updatedAt(LocalDateTime.now().minusDays(25))
                .build();
        route2.setItinerary(new ArrayList<>());
        route2.setTips(new ArrayList<>());
        sampleRoutes.add(route2);

        // 示例路线3：维吾尔文化体验
        Route route3 = Route.builder()
                .title("维吾尔文化深度体验")
                .description("深入了解维吾尔族的文化传统，参观博物馆、体验手工艺制作、品尝地道美食，感受浓郁的民族风情。")
                .cover("https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=800")
                .theme("culture")
                .duration(5)
                .distance(600.0)
                .startLocation("乌鲁木齐")
                .endLocation("吐鲁番")
                .waypoints(12)
                .views(1560)
                .favorites(112)
                .user(null)
                .createdAt(LocalDateTime.now().minusDays(20))
                .updatedAt(LocalDateTime.now().minusDays(20))
                .build();
        route3.setItinerary(new ArrayList<>());
        route3.setTips(new ArrayList<>());
        sampleRoutes.add(route3);

        // 示例路线4：新疆美食探索之旅
        Route route4 = Route.builder()
                .title("新疆美食探索之旅")
                .description("品尝新疆最地道的美食，从大盘鸡到烤羊肉串，从手抓饭到馕，体验舌尖上的新疆。")
                .cover("https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800")
                .theme("food")
                .duration(4)
                .distance(400.0)
                .startLocation("乌鲁木齐")
                .endLocation("伊犁")
                .waypoints(10)
                .views(890)
                .favorites(54)
                .user(null)
                .createdAt(LocalDateTime.now().minusDays(15))
                .updatedAt(LocalDateTime.now().minusDays(15))
                .build();
        route4.setItinerary(new ArrayList<>());
        route4.setTips(new ArrayList<>());
        sampleRoutes.add(route4);

        // 示例路线5：喀纳斯湖仙境之旅
        Route route5 = Route.builder()
                .title("喀纳斯湖仙境之旅")
                .description("探访神秘的喀纳斯湖，欣赏湖光山色，体验图瓦人的独特文化，感受大自然的宁静与美丽。")
                .cover("https://images.unsplash.com/photo-1501594907352-04cda38ebc29?w=800")
                .theme("nature")
                .duration(6)
                .distance(800.0)
                .startLocation("乌鲁木齐")
                .endLocation("喀纳斯")
                .waypoints(14)
                .views(2100)
                .favorites(145)
                .user(null)
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusDays(10))
                .build();
        route5.setItinerary(new ArrayList<>());
        route5.setTips(new ArrayList<>());
        sampleRoutes.add(route5);

        // 示例路线6：吐鲁番火焰山探险
        Route route6 = Route.builder()
                .title("吐鲁番火焰山探险")
                .description("挑战火焰山的高温，参观葡萄沟、坎儿井等著名景点，了解吐鲁番独特的地理环境和历史文化。")
                .cover("https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800")
                .theme("nature")
                .duration(2)
                .distance(200.0)
                .startLocation("吐鲁番")
                .endLocation("吐鲁番")
                .waypoints(6)
                .views(750)
                .favorites(48)
                .user(null)
                .createdAt(LocalDateTime.now().minusDays(5))
                .updatedAt(LocalDateTime.now().minusDays(5))
                .build();
        route6.setItinerary(new ArrayList<>());
        route6.setTips(new ArrayList<>());
        sampleRoutes.add(route6);

        routeRepository.saveAll(sampleRoutes);
        log.info("成功初始化 {} 条示例路线数据", sampleRoutes.size());
    }
}


