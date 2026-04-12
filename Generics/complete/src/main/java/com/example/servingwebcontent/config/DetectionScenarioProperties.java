package com.example.servingwebcontent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cau hinh scenario detection dang hoat dong.
 * Theo dung y tuong: UI va graph core giu nguyen, chi doi kich ban phan tich.
 */
@Component
@ConfigurationProperties(prefix = "detection")
public class DetectionScenarioProperties {

    private String activeScenario = "ASSOCIATION_GRAPH";

    public String getActiveScenario() {
        return activeScenario;
    }

    public void setActiveScenario(String activeScenario) {
        this.activeScenario = activeScenario;
    }
}