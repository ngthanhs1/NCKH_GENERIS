package com.example.servingwebcontent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cau hinh profile phan tich de doi domain ma khong sua core detection/scoring.
 */
@Component
@ConfigurationProperties(prefix = "analysis.profile")
public class AnalysisProfileProperties {

    private String name = "EMAIL_URL_IP";
    private final Map<String, String> nodeTypes = new LinkedHashMap<>();
    private final Map<String, String> relations = new LinkedHashMap<>();
    private final Map<String, Integer> scoreWeights = new LinkedHashMap<>();
    private final Map<String, Integer> thresholds = new LinkedHashMap<>();

    public AnalysisProfileProperties() {
        nodeTypes.put("source", "Email");
        nodeTypes.put("resource", "URL");
        nodeTypes.put("infrastructure", "IPAddress");

        relations.put("sourceToResource", "CONTAINS_LINK");
        relations.put("sourceToInfrastructure", "SENT_FROM");
        relations.put("resourceToInfrastructure", "HOSTED_BY");

        scoreWeights.put("sourceHasResource", 30);
        scoreWeights.put("sourceHasInfrastructure", 20);
        scoreWeights.put("sharedResource", 30);
        scoreWeights.put("sharedInfrastructure", 30);
        scoreWeights.put("resourceSharedByManySources", 40);
        scoreWeights.put("infrastructureUsedByManySources", 40);
        scoreWeights.put("infrastructureHostsManyResources", 30);

        thresholds.put("fraud", 60);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getNodeTypes() {
        return nodeTypes;
    }

    public Map<String, String> getRelations() {
        return relations;
    }

    public Map<String, Integer> getScoreWeights() {
        return scoreWeights;
    }

    public Map<String, Integer> getThresholds() {
        return thresholds;
    }

    public String nodeType(String key) {
        return nodeTypes.get(key);
    }

    public String relation(String key) {
        return relations.get(key);
    }

    public int scoreWeight(String key) {
        return scoreWeights.getOrDefault(key, 0);
    }

    public int threshold(String key, int defaultValue) {
        return thresholds.getOrDefault(key, defaultValue);
    }
}
