package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.Model.dto.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Kich ban phan tich do thi nang cao bang thuat toan Graph Analytics
 * (PageRank Centrality & Label Propagation Community Detection).
 */
@Component
public class GraphAnalyticsDetectionScenario implements DetectionScenario {

    private final GraphAnalyticsService analyticsService;

    public GraphAnalyticsDetectionScenario(GraphAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @Override
    public String key() {
        return "GRAPH_ANALYTICS";
    }

    @Override
    public String displayName() {
        return "Graph Analytics & Community Detection Scenario";
    }

    @Override
    public DetectionResult<PatternMatch<Map<String, Object>>> detect(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph) {

        GraphAnalyticsService.AnalyticsResult analytics = analyticsService.analyzeGraph(graph);
        Map<Object, Double> pageRanks = analytics.pageRankScores();
        Map<Object, String> communities = analytics.communityIds();
        Map<String, List<Object>> communityMembers = analytics.communityMembers();

        List<PatternMatch<Map<String, Object>>> patterns = new ArrayList<>();

        // 1. Phat hien cac nut Hub co do trung tam PageRank cao (>= 50.0)
        for (Map.Entry<Object, Double> entry : pageRanks.entrySet()) {
            if (entry.getValue() >= 50.0) {
                patterns.add(new PatternMatch<>(
                        "PAGERANK_HIGH_CENTRALITY_HUB",
                        "HIGH",
                        "Nut co do trung tam PageRank cao, dong vai tro hat nhan dieu phoi trong do thi.",
                        List.of(entry.getKey()),
                        Map.of(
                                "pageRankScore", entry.getValue(),
                                "communityId", communities.getOrDefault(entry.getKey(), "UNKNOWN")
                        )
                ));
            }
        }

        // 2. Phat hien cac nut la Cầu nối liên cộng đồng (Cross-Community Bridge)
        Map<Object, Set<String>> neighborCommunities = new HashMap<>();
        for (EdgeDTO<Map<String, Object>> edge : graph.edges() == null ? List.<EdgeDTO<Map<String, Object>>>of() : graph.edges()) {
            String fromComm = communities.get(edge.from());
            String toComm = communities.get(edge.to());
            if (fromComm != null && toComm != null && !fromComm.equals(toComm)) {
                neighborCommunities.computeIfAbsent(edge.from(), k -> new HashSet<>()).add(toComm);
                neighborCommunities.computeIfAbsent(edge.to(), k -> new HashSet<>()).add(fromComm);
            }
        }

        for (Map.Entry<Object, Set<String>> entry : neighborCommunities.entrySet()) {
            if (entry.getValue().size() >= 2) {
                patterns.add(new PatternMatch<>(
                        "CROSS_COMMUNITY_BRIDGE",
                        "MEDIUM",
                        "Nut dong vai tro cau noi giua nhieu cong dong/cum khac nhau trong do thi.",
                        List.of(entry.getKey()),
                        Map.of(
                                "connectedCommunitiesCount", entry.getValue().size(),
                                "connectedCommunities", new ArrayList<>(entry.getValue())
                        )
                ));
            }
        }

        // 3. Phat hien cac Cong dong/Cum co mat do dong (>= 3 thanh vien)
        for (Map.Entry<String, List<Object>> entry : communityMembers.entrySet()) {
            if (entry.getValue().size() >= 3) {
                patterns.add(new PatternMatch<>(
                        "DENSE_COMMUNITY_CLUSTER",
                        "INFO",
                        "Cong dong/Cum duoc phat hien chua nhieu nut lien ket mat thiet.",
                        entry.getValue(),
                        Map.of(
                                "communityId", entry.getKey(),
                                "memberCount", entry.getValue().size()
                        )
                ));
            }
        }

        Map<String, Long> countsByRule = new LinkedHashMap<>();
        for (PatternMatch<Map<String, Object>> p : patterns) {
            countsByRule.put(p.ruleCode(), countsByRule.getOrDefault(p.ruleCode(), 0L) + 1L);
        }

        return new DetectionResult<>(patterns, countsByRule, patterns.size());
    }

    @Override
    public RiskResult<RiskScoreItem<Map<String, Object>>> evaluate(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph) {

        GraphAnalyticsService.AnalyticsResult analytics = analyticsService.analyzeGraph(graph);
        Map<Object, Double> pageRanks = analytics.pageRankScores();
        Map<Object, String> communities = analytics.communityIds();

        List<RiskScoreItem<Map<String, Object>>> items = new ArrayList<>();
        List<NodeDTO<Map<String, Object>>> nodes = graph.nodes() == null ? List.of() : graph.nodes();

        for (NodeDTO<Map<String, Object>> node : nodes) {
            if (node == null || node.id() == null) continue;
            double pr = pageRanks.getOrDefault(node.id(), 0.0);
            String comm = communities.getOrDefault(node.id(), "NONE");

            int score = (int) Math.min(100, Math.round(pr));
            String verdict = score >= 75 ? "CRITICAL_HUB"
                    : score >= 45 ? "HIGH_INFLUENCE"
                    : score >= 20 ? "MODERATE"
                    : "ISOLATED";

            items.add(new RiskScoreItem<>(
                    node.id(),
                    node.type(),
                    score,
                    verdict,
                    Map.of(
                            "pageRankScore", pr,
                            "communityId", comm,
                            "scenario", key()
                    )
            ));
        }

        Map<String, Long> countsByVerdict = new LinkedHashMap<>();
        for (RiskScoreItem<Map<String, Object>> item : items) {
            countsByVerdict.put(item.verdict(), countsByVerdict.getOrDefault(item.verdict(), 0L) + 1L);
        }

        return new RiskResult<>(items, countsByVerdict, items.size());
    }

    @Override
    public Map<String, Object> describe() {
        return Map.of(
                "scenario", key(),
                "displayName", displayName(),
                "description", "Kịch bản phân tích đồ thị nâng cao sử dụng các thuật toán Graph Analytics (PageRank Centrality & Label Propagation Community Detection).",
                "algorithms", List.of(
                        "PageRank Centrality",
                        "Label Propagation Algorithm (LPA) Community Detection",
                        "Cross-Community Bridge Detection"
                )
        );
    }
}
