package com.example.servingwebcontent;

import com.example.servingwebcontent.Model.dto.*;
import com.example.servingwebcontent.Service.detection.GraphAiAnomalyDetectionScenario;
import com.example.servingwebcontent.Service.detection.GraphEmbeddingService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GraphAiAnomalyDetectionScenarioTest {

    @Test
    void testGraphAiAnomalyScenarioExecution() {
        GraphEmbeddingService embeddingService = new GraphEmbeddingService();
        GraphAiAnomalyDetectionScenario scenario = new GraphAiAnomalyDetectionScenario(embeddingService);

        assertEquals("GRAPH_AI_ANOMALY", scenario.key());
        assertEquals("Graph AI Embedding & Anomaly Detection Scenario", scenario.displayName());

        List<NodeDTO<Map<String, Object>>> nodes = List.of(
                new NodeDTO<>("A", "Person", Map.of("name", "Alice")),
                new NodeDTO<>("B", "Person", Map.of("name", "Bob")),
                new NodeDTO<>("C", "Person", Map.of("name", "Charlie")),
                new NodeDTO<>("D", "Device", Map.of("brand", "Samsung"))
        );

        List<EdgeDTO<Map<String, Object>>> edges = List.of(
                new EdgeDTO<>("A", "B", "KNOWS", Map.of()),
                new EdgeDTO<>("B", "C", "KNOWS", Map.of()),
                new EdgeDTO<>("C", "A", "KNOWS", Map.of()),
                new EdgeDTO<>("C", "D", "USES", Map.of())
        );

        GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph = new GraphData<>(nodes, edges);

        DetectionResult<PatternMatch<Map<String, Object>>> detectionResult = scenario.detect(graph);
        assertNotNull(detectionResult);
        assertTrue(detectionResult.totalPatterns() >= 0);

        RiskResult<RiskScoreItem<Map<String, Object>>> riskResult = scenario.evaluate(graph);
        assertNotNull(riskResult);
        assertEquals(4, riskResult.totalItems());
    }
}
