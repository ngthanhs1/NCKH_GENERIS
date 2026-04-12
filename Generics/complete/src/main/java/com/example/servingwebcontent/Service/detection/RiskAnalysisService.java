package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import com.example.servingwebcontent.Model.dto.RiskResult;
import com.example.servingwebcontent.Model.dto.RiskScoreItem;
import com.example.servingwebcontent.Service.GraphQueryService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service tong hop scoring rui ro dua tren strategy generic.
 */
@Service
public class RiskAnalysisService {

    private final GraphQueryService graphQueryService;
    private final DetectionScenarioRegistry detectionScenarioRegistry;

    public RiskAnalysisService(
            GraphQueryService graphQueryService,
            DetectionScenarioRegistry detectionScenarioRegistry) {
        this.graphQueryService = graphQueryService;
        this.detectionScenarioRegistry = detectionScenarioRegistry;
    }

    public RiskResult<RiskScoreItem<Map<String, Object>>> evaluateAll() {
        GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph = graphQueryService.fetchGraph();
        return detectionScenarioRegistry.activeScenario().evaluate(graph);
    }
}
