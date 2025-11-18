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
                .tips(new ArrayList<>())
                .build();
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
                .tips(new ArrayList<>())
                .build();
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
                .tips(new ArrayList<>())
                .build();
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
                .tips(new ArrayList<>())
                .build();
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
                .tips(new ArrayList<>())
                .build();
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
                .tips(new ArrayList<>())
                .build();
        sampleRoutes.add(route6);

        // ========== 丝绸之路主题 - 第二条 ==========
        // 示例路线7：古丝绸之路南道探秘
        Route route7 = Route.builder()
                .title("古丝绸之路南道探秘")
                .description("沿着古丝绸之路南道，从和田出发，穿越塔克拉玛干沙漠边缘，探访千年古城遗址，感受丝路商旅的传奇历史。途经和田、于田、民丰，体验沙漠绿洲的独特魅力。")
                .cover("https://images.unsplash.com/photo-1508804185872-d7badad00f7d?w=800")
                .theme("silkRoad")
                .duration(8)
                .distance(900.0)
                .startLocation("和田")
                .endLocation("库尔勒")
                .waypoints(18)
                .views(1680)
                .favorites(125)
                .user(null)
                .createdAt(LocalDateTime.now().minusDays(28))
                .updatedAt(LocalDateTime.now().minusDays(28))
                .tips(new ArrayList<>())
                .build();
        sampleRoutes.add(route7);

        // ========== 文化体验主题 - 第二条 ==========
        // 示例路线8：哈萨克族文化深度游
        Route route8 = Route.builder()
                .title("哈萨克族文化深度游")
                .description("深入体验哈萨克族的游牧文化，观看传统赛马、叼羊比赛，品尝马奶酒和手抓肉，入住哈萨克毡房，感受草原民族的豪迈与热情。")
                .cover("https://images.unsplash.com/photo-1559827260-dc66d52bef19?w=800")
                .theme("culture")
                .duration(4)
                .distance(500.0)
                .startLocation("伊犁")
                .endLocation("那拉提")
                .waypoints(10)
                .views(1320)
                .favorites(98)
                .user(null)
                .createdAt(LocalDateTime.now().minusDays(18))
                .updatedAt(LocalDateTime.now().minusDays(18))
                .tips(new ArrayList<>())
                .build();
        sampleRoutes.add(route8);

        // ========== 美食之旅主题 - 第二条 ==========
        // 示例路线9：南疆美食寻味之旅
        Route route9 = Route.builder()
                .title("南疆美食寻味之旅")
                .description("从喀什出发，一路品尝南疆最地道的美食。烤包子、拉条子、抓饭、烤全羊、馕坑肉...每一站都是味蕾的盛宴，体验新疆美食的丰富多样和独特风味。")
                .cover("https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800")
                .theme("food")
                .duration(5)
                .distance(650.0)
                .startLocation("喀什")
                .endLocation("阿克苏")
                .waypoints(12)
                .views(1450)
                .favorites(108)
                .user(null)
                .createdAt(LocalDateTime.now().minusDays(12))
                .updatedAt(LocalDateTime.now().minusDays(12))
                .tips(new ArrayList<>())
                .build();
        sampleRoutes.add(route9);

        // ========== 自然风光主题 - 第四条（已有3条，再增加1条特色路线）==========
        // 示例路线10：塔克拉玛干沙漠穿越
        Route route10 = Route.builder()
                .title("塔克拉玛干沙漠穿越")
                .description("挑战中国最大的沙漠，体验沙漠越野的刺激，观赏壮观的沙漠日出日落，探访沙漠中的绿洲，感受大漠孤烟直的壮美景象。")
                .cover("https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800")
                .theme("nature")
                .duration(5)
                .distance(1200.0)
                .startLocation("库尔勒")
                .endLocation("和田")
                .waypoints(16)
                .views(1890)
                .favorites(156)
                .user(null)
                .createdAt(LocalDateTime.now().minusDays(8))
                .updatedAt(LocalDateTime.now().minusDays(8))
                .tips(new ArrayList<>())
                .build();
        sampleRoutes.add(route10);

        routeRepository.saveAll(sampleRoutes);
        log.info("成功初始化 {} 条示例路线数据", sampleRoutes.size());
    }
}






