package com.example.servingwebcontent.Controller;

import com.example.servingwebcontent.Model.dto.ApiResponse;
import com.example.servingwebcontent.Model.dto.DetectionResult;
import com.example.servingwebcontent.Model.dto.PatternMatch;
import com.example.servingwebcontent.Model.dto.RiskResult;
import com.example.servingwebcontent.Model.dto.RiskScoreItem;
import com.example.servingwebcontent.Service.detection.DetectionScenarioRegistry;
import com.example.servingwebcontent.Service.detection.PatternDetectionService;
import com.example.servingwebcontent.Service.detection.RiskAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API phat hien mau nghi ngo va ket noi an.
 */
@RestController
@RequestMapping("/api/detection")
public class DetectionApiController {

    private final PatternDetectionService patternDetectionService;
    private final RiskAnalysisService riskAnalysisService;
    private final DetectionScenarioRegistry detectionScenarioRegistry;

    public DetectionApiController(
            PatternDetectionService patternDetectionService,
            RiskAnalysisService riskAnalysisService,
            DetectionScenarioRegistry detectionScenarioRegistry) {
        this.patternDetectionService = patternDetectionService;
        this.riskAnalysisService = riskAnalysisService;
        this.detectionScenarioRegistry = detectionScenarioRegistry;
    }

    @GetMapping("/patterns")
    public ApiResponse<DetectionResult<PatternMatch<Map<String, Object>>>> detectPatterns() {
        return ApiResponse.ok(patternDetectionService.detectAll());
    }

    @GetMapping("/risk")
    public ApiResponse<RiskResult<RiskScoreItem<Map<String, Object>>>> analyzeRisk() {
        return ApiResponse.ok(riskAnalysisService.evaluateAll());
    }

    @GetMapping("/profile")
    public ApiResponse<Map<String, Object>> getProfile() {
        return ApiResponse.ok(
                Map.of(
                "activeScenario", detectionScenarioRegistry.activeScenario().key(),
                "details", detectionScenarioRegistry.activeScenario().describe(),
                "availableScenarios", detectionScenarioRegistry.availableScenarios()
                )
        );
    }
}
