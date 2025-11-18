package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.response.MapPoiResponse;
import com.example.culturalxinjiang.entity.CultureResource;
import com.example.culturalxinjiang.repository.CultureResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapService {

    private static final double DEFAULT_CLUSTER_SIZE = 0.25D;
    private static final double MIN_CLUSTER_SIZE = 0.05D;
    private static final double MAX_CLUSTER_SIZE = 1.2D;

    private final CultureResourceRepository cultureResourceRepository;

    @Transactional(readOnly = true)
    public MapPoiResponse getPois(String keyword,
                                  String category,
                                  Bounds bounds,
                                  boolean cluster,
                                  Integer zoom,
                                  Integer limit) {
        List<MapPoiResponse.MapPoi> allPois = cultureResourceRepository.findAllWithLocation().stream()
                .map(this::mapToPoi)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, Long> stats = allPois.stream()
                .collect(Collectors.groupingBy(MapPoiResponse.MapPoi::getCategory, Collectors.counting()));
        stats.put("total", (long) allPois.size());

        List<MapPoiResponse.MapPoi> filteredAll = allPois.stream()
                .filter(poi -> matchesKeyword(poi, keyword))
                .filter(poi -> matchesCategory(poi, category))
                .filter(poi -> matchesBounds(poi, bounds))
                .collect(Collectors.toList());

        long filteredTotal = filteredAll.size();
        List<MapPoiResponse.MapPoi> filtered = filteredAll.stream()
                .limit(limit != null && limit > 0 ? limit : Long.MAX_VALUE)
                .collect(Collectors.toList());

        List<MapPoiResponse.MapCluster> clusters = cluster
                ? buildClusters(filteredAll, zoom != null ? zoom : 5)
                : List.of();

        return MapPoiResponse.builder()
                .pois(filtered)
                .clusters(clusters)
                .stats(stats)
                .total((long) allPois.size())
                .filtered(filteredTotal)
                .clustered(cluster)
                .build();
    }

    private boolean matchesKeyword(MapPoiResponse.MapPoi poi, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return poi.getTitle().toLowerCase(Locale.ROOT).contains(lowerKeyword)
                || (poi.getSummary() != null && poi.getSummary().toLowerCase(Locale.ROOT).contains(lowerKeyword))
                || (poi.getTags() != null && poi.getTags().stream()
                .anyMatch(tag -> tag != null && tag.toLowerCase(Locale.ROOT).contains(lowerKeyword)));
    }

    private boolean matchesCategory(MapPoiResponse.MapPoi poi, String category) {
        if (!StringUtils.hasText(category) || "all".equalsIgnoreCase(category)) {
            return true;
        }
        return category.equalsIgnoreCase(poi.getCategory());
    }

    private boolean matchesBounds(MapPoiResponse.MapPoi poi, Bounds bounds) {
        if (bounds == null) {
            return true;
        }
        return poi.getLat() >= bounds.getSouth()
                && poi.getLat() <= bounds.getNorth()
                && poi.getLng() >= bounds.getWest()
                && poi.getLng() <= bounds.getEast();
    }

    private List<MapPoiResponse.MapCluster> buildClusters(List<MapPoiResponse.MapPoi> pois, Integer zoom) {
        if (pois.isEmpty()) {
            return List.of();
        }
        double bucketSize = deriveBucketSize(zoom);
        Map<String, List<MapPoiResponse.MapPoi>> buckets = new ConcurrentHashMap<>();

        for (MapPoiResponse.MapPoi poi : pois) {
            String key = bucketKey(poi.getLat(), poi.getLng(), bucketSize);
            buckets.computeIfAbsent(key, ignored -> new ArrayList<>()).add(poi);
        }

        return buckets.values().stream()
                .filter(bucket -> bucket.size() > 1)
                .map(bucket -> {
                    DoubleSummaryStatistics latStats = bucket.stream()
                            .collect(Collectors.summarizingDouble(MapPoiResponse.MapPoi::getLat));
                    DoubleSummaryStatistics lngStats = bucket.stream()
                            .collect(Collectors.summarizingDouble(MapPoiResponse.MapPoi::getLng));
                    Map<String, Long> breakdown = bucket.stream()
                            .collect(Collectors.groupingBy(MapPoiResponse.MapPoi::getCategory, Collectors.counting()));
                    return MapPoiResponse.MapCluster.builder()
                            .lat((latStats.getAverage()))
                            .lng((lngStats.getAverage()))
                            .count((long) bucket.size())
                            .categories(breakdown)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private double deriveBucketSize(Integer zoom) {
        if (zoom == null) {
            return DEFAULT_CLUSTER_SIZE;
        }
        double normalized = Math.max(3, Math.min(zoom, 18));
        double size = (18 - normalized) * 0.05;
        size = Math.max(size, MIN_CLUSTER_SIZE);
        size = Math.min(size, MAX_CLUSTER_SIZE);
        return size;
    }

    private String bucketKey(Double lat, Double lng, double bucketSize) {
        long latIndex = Math.round(lat / bucketSize);
        long lngIndex = Math.round(lng / bucketSize);
        return latIndex + ":" + lngIndex;
    }

    private MapPoiResponse.MapPoi mapToPoi(CultureResource resource) {
        if (resource.getLocation() == null
                || resource.getLocation().getLat() == null
                || resource.getLocation().getLng() == null) {
            return null;
        }
        String category = resolveCategory(resource);
        return MapPoiResponse.MapPoi.builder()
                .id(resource.getId())
                .originRefId(resource.getId())
                .originType("CULTURE")
                .contentType(resource.getType() != null ? resource.getType().name().toLowerCase(Locale.ROOT) : "culture")
                .category(category)
                .title(resource.getTitle())
                .lat(resource.getLocation().getLat())
                .lng(resource.getLocation().getLng())
                .region(resource.getRegion())
                .cover(resource.getCover())
                .tags(resource.getTags() != null ? new ArrayList<>(resource.getTags()) : List.of())
                .summary(resource.getDescription())
                .views(resource.getViews())
                .favorites(resource.getFavorites())
                .build();
    }

    private String resolveCategory(CultureResource resource) {
        if (resource.getType() != null) {
            switch (resource.getType()) {
                case EXHIBIT -> {
                    return "museum";
                }
                case VIDEO -> {
                    if (containsHeritageKeyword(safeLower(resource.getTitle()), safeLower(resource.getDescription()))) {
                        return "heritage";
                    }
                }
                default -> {
                    // fallthrough to keyword rules
                }
            }
        }

        String tags = resource.getTags() != null
                ? String.join(",", resource.getTags()).toLowerCase(Locale.ROOT)
                : "";
        String text = (safeLower(resource.getTitle()) + safeLower(resource.getDescription()));
        if (containsMuseumKeyword(tags, text)) {
            return "museum";
        }
        if (containsRelicKeyword(tags, text)) {
            return "relic";
        }
        if (containsHeritageKeyword(tags, text)) {
            return "heritage";
        }
        return "scenic";
    }

    private boolean containsMuseumKeyword(String... sources) {
        return containsAny(List.of("博物馆", "museum", "纪念馆"), sources);
    }

    private boolean containsRelicKeyword(String... sources) {
        return containsAny(List.of("遗址", "古城", "故城", "遗迹", "旧址"), sources);
    }

    private boolean containsHeritageKeyword(String... sources) {
        return containsAny(List.of("非遗", "非物质", "heritage"), sources);
    }

    private boolean containsAny(List<String> keywords, String... sources) {
        for (String source : sources) {
            if (source == null || source.isBlank()) {
                continue;
            }
            for (String keyword : keywords) {
                if (source.contains(keyword)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String safeLower(String input) {
        return input != null ? input.toLowerCase(Locale.ROOT) : "";
    }

    public record Bounds(Double north, Double south, Double east, Double west) {
        public double getNorth() {
            return north != null ? north : 90D;
        }

        public double getSouth() {
            return south != null ? south : -90D;
        }

        public double getEast() {
            return east != null ? east : 180D;
        }

        public double getWest() {
            return west != null ? west : -180D;
        }
    }
}


