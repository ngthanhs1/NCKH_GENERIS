package com.example.servingwebcontent;

import com.example.servingwebcontent.Controller.DetectionApiController;
import com.example.servingwebcontent.Controller.RestExceptionHandler;
import com.example.servingwebcontent.Model.dto.DetectionResult;
import com.example.servingwebcontent.Model.dto.PatternMatch;
import com.example.servingwebcontent.Model.dto.RiskResult;
import com.example.servingwebcontent.Model.dto.RiskScoreItem;
import com.example.servingwebcontent.Service.detection.DetectionScenario;
import com.example.servingwebcontent.Service.detection.DetectionScenarioRegistry;
import com.example.servingwebcontent.Service.detection.PatternDetectionService;
import com.example.servingwebcontent.Service.detection.RiskAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DetectionApiController.class)
@Import({RestExceptionHandler.class})
class DetectionApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatternDetectionService patternDetectionService;

        @MockBean
        private RiskAnalysisService riskAnalysisService;

        @MockBean
        private DetectionScenarioRegistry detectionScenarioRegistry;

        @MockBean
        private DetectionScenario detectionScenario;

    @Test
    void detectPatterns_returnsAggregatedDetectionResult() throws Exception {
        List<PatternMatch<Map<String, Object>>> patterns = List.of(
                new PatternMatch<>(
                        "SHARED_URL",
                        "HIGH",
                        "Nhieu Email cung tro toi mot URL (dau hieu campaign).",
                        List.of("FD_E_SHARED", "FD_P_ALICE", "FD_P_BOB"),
                        Map.of("emailCount", (Object) 2)
                )
        );

        DetectionResult<PatternMatch<Map<String, Object>>> result = new DetectionResult<>(
                patterns,
                Map.of("SHARED_URL", 1L),
                1L
        );

        when(patternDetectionService.detectAll()).thenReturn(
                result
        );

        mockMvc.perform(get("/api/detection/patterns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.totalPatterns").value(1))
                .andExpect(jsonPath("$.data.countsByRule.SHARED_URL").value(1))
                .andExpect(jsonPath("$.data.patterns[0].ruleCode").value("SHARED_URL"))
                .andExpect(jsonPath("$.data.patterns[0].severity").value("HIGH"));
    }

    @Test
    void analyzeRisk_returnsGenericRiskResult() throws Exception {
        List<RiskScoreItem<Map<String, Object>>> items = List.of(
                new RiskScoreItem<>(
                        "Email:alice@example.com",
                        "Email",
                        80,
                        "FRAUD",
                        Map.of("profile", "EMAIL_URL_IP")
                )
        );

        RiskResult<RiskScoreItem<Map<String, Object>>> result = new RiskResult<>(
                items,
                Map.of("FRAUD", 1L),
                1L
        );

        when(riskAnalysisService.evaluateAll()).thenReturn(result);

        mockMvc.perform(get("/api/detection/risk"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.totalItems").value(1))
                .andExpect(jsonPath("$.data.countsByVerdict.FRAUD").value(1))
                .andExpect(jsonPath("$.data.items[0].verdict").value("FRAUD"))
                .andExpect(jsonPath("$.data.items[0].score").value(80));
    }

    @Test
    void getProfile_returnsAnalysisProfileForFrontend() throws Exception {
        doReturn("ASSOCIATION_GRAPH").when(detectionScenario).key();
        doReturn(Map.of(
                "scenario", "ASSOCIATION_GRAPH",
                "displayName", "Association Graph Scenario"
        )).when(detectionScenario).describe();
        doReturn(detectionScenario).when(detectionScenarioRegistry).activeScenario();
        doReturn(List.of(
                Map.of("key", "ASSOCIATION_GRAPH", "displayName", "Association Graph Scenario"),
                Map.of("key", "EMAIL_URL_IP", "displayName", "Email - URL - IP Scenario")
        )).when(detectionScenarioRegistry).availableScenarios();

        mockMvc.perform(get("/api/detection/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.activeScenario").value("ASSOCIATION_GRAPH"))
                .andExpect(jsonPath("$.data.details.displayName").value("Association Graph Scenario"))
                .andExpect(jsonPath("$.data.availableScenarios[0].key").value("ASSOCIATION_GRAPH"));
    }
}
