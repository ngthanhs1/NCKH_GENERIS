package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.Model.dto.DetectionResult;
import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import com.example.servingwebcontent.Model.dto.PatternMatch;
import com.example.servingwebcontent.Model.dto.RiskResult;
import com.example.servingwebcontent.Model.dto.RiskScoreItem;
import com.example.servingwebcontent.config.AnalysisProfileProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kich ban minh hoa cu theo bai toan Email-URL-IP.
 */
@Component
public class EmailUrlIpDetectionScenario implements DetectionScenario {

    private final AnalysisProfileProperties analysisProfileProperties;
    private final List<PatternRule<Map<String, Object>>> rules;
    private final FraudEmailUrlIpRiskStrategy riskStrategy;

    public EmailUrlIpDetectionScenario(
            AnalysisProfileProperties analysisProfileProperties,
            SharedIdentifierRule sharedIdentifierRule,
            SharedDeviceRule sharedDeviceRule,
            ChargebackTransactionRule chargebackTransactionRule,
            TrianglePathRule trianglePathRule,
            FraudEmailUrlIpRiskStrategy riskStrategy) {
        this.analysisProfileProperties = analysisProfileProperties;
        this.rules = List.of(sharedIdentifierRule, sharedDeviceRule, chargebackTransactionRule, trianglePathRule);
        this.riskStrategy = riskStrategy;
    }

    @Override
    public String key() {
        return "EMAIL_URL_IP";
    }

    @Override
    public String displayName() {
        return "Email - URL - IP Scenario";
    }

    @Override
    public DetectionResult<PatternMatch<Map<String, Object>>> detect(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph) {
        List<PatternMatch<Map<String, Object>>> all = new ArrayList<>();
        Map<String, Long> countsByRule = new LinkedHashMap<>();

        for (PatternRule<Map<String, Object>> rule : rules) {
            List<PatternMatch<Map<String, Object>>> matches = rule.detect(graph);
            all.addAll(matches);
            countsByRule.put(rule.code(), (long) matches.size());
        }

        return new DetectionResult<>(all, countsByRule, all.size());
    }

    @Override
    public RiskResult<RiskScoreItem<Map<String, Object>>> evaluate(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph) {
        List<RiskScoreItem<Map<String, Object>>> items = riskStrategy.evaluate(graph);
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
                "description", "Kich ban phan tich minh hoa theo profile Email - URL - IP.",
                "nodeTypes", analysisProfileProperties.getNodeTypes(),
                "relations", analysisProfileProperties.getRelations(),
                "scoreWeights", analysisProfileProperties.getScoreWeights(),
                "thresholds", analysisProfileProperties.getThresholds()
        );
    }
}