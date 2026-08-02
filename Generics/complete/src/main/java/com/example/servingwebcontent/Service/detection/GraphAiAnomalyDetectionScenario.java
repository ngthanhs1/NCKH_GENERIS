package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.Model.dto.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Kich ban phan tich do thi AI/Machine Learning
 * (Node2Vec Random Walk Embedding & Mahalanobis Anomaly Detection Model).
 */
@Component
public class GraphAiAnomalyDetectionScenario implements DetectionScenario {

    private final GraphEmbeddingService embeddingService;

    public GraphAiAnomalyDetectionScenario(GraphEmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @Override
    public String key() {
        return "GRAPH_AI_ANOMALY";
    }

    @Override
    public String displayName() {
        return "Graph AI Embedding & Anomaly Detection Scenario";
    }

    @Override
    public DetectionResult<PatternMatch<Map<String, Object>>> detect(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph) {

        GraphEmbeddingService.EmbeddingResult result = embeddingService.computeGraphEmbeddingsAndAnomalies(graph);
        Map<Object, Double> anomalyScores = result.anomalyScores();
        Map<Object, double[]> embeddings = result.nodeEmbeddings();
        Map<Object, String> verdicts = result.aiVerdicts();

        List<PatternMatch<Map<String, Object>>> patterns = new ArrayList<>();

        // 1. Phat hien cac nut co vector dac trung bat thuong lon (Anomaly Score >= 60.0)
        for (Map.Entry<Object, Double> entry : anomalyScores.entrySet()) {
            if (entry.getValue() >= 60.0) {
                double[] vec = embeddings.getOrDefault(entry.getKey(), new double[0]);
                patterns.add(new PatternMatch<>(
                        "AI_EMBEDDING_ANOMALY_NODE",
                        "HIGH",
                        "Mo hinh Graph Machine Learning (Node2Vec) phat hien nut co cau truc va kieu kien truc bat thuong so voi toan do thi.",
                        List.of(entry.getKey()),
                        Map.of(
                                "aiAnomalyScore", entry.getValue(),
                                "aiVerdict", verdicts.getOrDefault(entry.getKey(), "UNKNOWN"),
                                "embeddingVectorDim", vec.length
                        )
                ));
            }
        }

        // 2. Phat hien cac Pattern Cụm Bất Thường (Anomalous Cluster Pattern)
        List<Object> anomalousNodeIds = new ArrayList<>();
        for (Map.Entry<Object, Double> entry : anomalyScores.entrySet()) {
            if (entry.getValue() >= 45.0) {
                anomalousNodeIds.add(entry.getKey());
            }
        }

        if (anomalousNodeIds.size() >= 2) {
            patterns.add(new PatternMatch<>(
                    "AI_ANOMALOUS_CLUSTER_PATTERN",
                    "MEDIUM",
                    "Mo hinh AI phat hien cum cac nut co hanh vi va vi tri ket noi bat thuong trong do thi.",
                    anomalousNodeIds,
                    Map.of(
                            "anomalousNodeCount", anomalousNodeIds.size(),
                            "aiModel", "Node2Vec + Distance Variance Model"
                    )
            ));
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

        GraphEmbeddingService.EmbeddingResult result = embeddingService.computeGraphEmbeddingsAndAnomalies(graph);
        Map<Object, Double> anomalyScores = result.anomalyScores();
        Map<Object, String> verdicts = result.aiVerdicts();

        List<RiskScoreItem<Map<String, Object>>> items = new ArrayList<>();
        List<NodeDTO<Map<String, Object>>> nodes = graph.nodes() == null ? List.of() : graph.nodes();

        for (NodeDTO<Map<String, Object>> node : nodes) {
            if (node == null || node.id() == null) continue;
            double score = anomalyScores.getOrDefault(node.id(), 0.0);
            String verdict = verdicts.getOrDefault(node.id(), "NORMAL_PATTERN");

            items.add(new RiskScoreItem<>(
                    node.id(),
                    node.type(),
                    (int) Math.round(score),
                    verdict,
                    Map.of(
                            "aiAnomalyScore", score,
                            "aiVerdict", verdict,
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
                "description", "Kịch bản phân tích đồ thị AI/Machine Learning sử dụng thuật toán nhúng vector cấu trúc đồ thị Node2Vec và mô hình phát hiện bất thường Anomaly Detection.",
                "aiPipeline", List.of(
                        "Second-Order Random Walk Generator",
                        "Co-occurrence Vector Space Embedding (Node2Vec)",
                        "Euclidean Variance Centroid Anomaly Model"
                )
        );
    }
}
