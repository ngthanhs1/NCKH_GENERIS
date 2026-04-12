package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.Model.dto.DetectionResult;
import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import com.example.servingwebcontent.Model.dto.PatternMatch;
import com.example.servingwebcontent.Service.GraphQueryService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service tong hop ket qua detection tu nhieu rule co generic metadata.
 */
@Service
public class PatternDetectionService {

    private final GraphQueryService graphQueryService;
    private final DetectionScenarioRegistry detectionScenarioRegistry;

    public PatternDetectionService(
            GraphQueryService graphQueryService,
            DetectionScenarioRegistry detectionScenarioRegistry) {
        this.graphQueryService = graphQueryService;
        this.detectionScenarioRegistry = detectionScenarioRegistry;
    }

    public DetectionResult<PatternMatch<Map<String, Object>>> detectAll() {
        GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph = graphQueryService.fetchGraph();
        return detectionScenarioRegistry.activeScenario().detect(graph);
    }
}
