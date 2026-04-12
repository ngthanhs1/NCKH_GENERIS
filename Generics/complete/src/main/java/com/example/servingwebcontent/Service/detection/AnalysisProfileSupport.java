package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.config.AnalysisProfileProperties;
import org.springframework.stereotype.Component;

/**
 * Helper dung chung de so khop node/relation theo profile cau hinh.
 */
@Component
public class AnalysisProfileSupport {

    private final AnalysisProfileProperties properties;

    public AnalysisProfileSupport(AnalysisProfileProperties properties) {
        this.properties = properties;
    }

    public AnalysisProfileProperties properties() {
        return properties;
    }

    public boolean isNodeType(String actualType, String key) {
        String expected = properties.nodeType(key);
        return expected != null && expected.equalsIgnoreCase(actualType);
    }

    public boolean isRelation(String actualRelation, String key) {
        String expected = properties.relation(key);
        return expected != null && expected.equalsIgnoreCase(actualRelation);
    }
}