package com.example.servingwebcontent;

import com.example.servingwebcontent.Model.dto.*;
import com.example.servingwebcontent.Service.detection.GraphAnalyticsDetectionScenario;
import com.example.servingwebcontent.Service.detection.GraphAnalyticsService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GraphAnalyticsDetectionScenarioTest {

    @Test
    void testGraphAnalyticsScenarioExecution() {
        GraphAnalyticsService analyticsService = new GraphAnalyticsService();
        GraphAnalyticsDetectionScenario scenario = new GraphAnalyticsDetectionScenario(analyticsService);

        assertEquals("GRAPH_ANALYTICS", scenario.key());
        assertEquals("Graph Analytics & Community Detection Scenario", scenario.displayName());

        List<NodeDTO<Map<String, Object>>> nodes = List.of(
                new NodeDTO<>("A", "Person", Map.of("name", "Alice")),
                new NodeDTO<>("B", "Person", Map.of("name", "Bob")),
                new NodeDTO<>("C", "Person", Map.of("name", "Charlie")),
                new NodeDTO<>("D", "Person", Map.of("name", "David"))
        );

        List<EdgeDTO<Map<String, Object>>> edges = List.of(
                new EdgeDTO<>("A", "B", "KNOWS", Map.of()),
                new EdgeDTO<>("B", "C", "KNOWS", Map.of()),
                new EdgeDTO<>("C", "A", "KNOWS", Map.of()),
                new EdgeDTO<>("C", "D", "KNOWS", Map.of())
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
