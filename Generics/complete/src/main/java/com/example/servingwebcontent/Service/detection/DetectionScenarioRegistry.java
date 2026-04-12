package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.config.DetectionScenarioProperties;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Registry quan ly cac scenario detection va scenario dang duoc chon.
 */
@Service
public class DetectionScenarioRegistry {

    private final DetectionScenarioProperties properties;
    private final Map<String, DetectionScenario> scenariosByKey;

    public DetectionScenarioRegistry(
            DetectionScenarioProperties properties,
            List<DetectionScenario> scenarios) {
        this.properties = properties;
        this.scenariosByKey = new LinkedHashMap<>();
        for (DetectionScenario scenario : scenarios) {
            scenariosByKey.put(normalize(scenario.key()), scenario);
        }
    }

    public DetectionScenario activeScenario() {
        DetectionScenario scenario = scenariosByKey.get(normalize(properties.getActiveScenario()));
        if (scenario != null) {
            return scenario;
        }
        return scenariosByKey.values().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No detection scenario registered"));
    }

    public List<Map<String, Object>> availableScenarios() {
        return scenariosByKey.values().stream()
                .map(scenario -> Map.<String, Object>of(
                        "key", scenario.key(),
                        "displayName", scenario.displayName()
                ))
                .toList();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}