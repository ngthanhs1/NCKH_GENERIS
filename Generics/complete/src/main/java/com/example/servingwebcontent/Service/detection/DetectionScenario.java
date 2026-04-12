package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.Model.dto.DetectionResult;
import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import com.example.servingwebcontent.Model.dto.PatternMatch;
import com.example.servingwebcontent.Model.dto.RiskResult;
import com.example.servingwebcontent.Model.dto.RiskScoreItem;

import java.util.Map;

/**
 * Moi kich ban phan tich duoc mo ta bang mot scenario rieng.
 */
public interface DetectionScenario {
    String key();

    String displayName();

    DetectionResult<PatternMatch<Map<String, Object>>> detect(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph);

    RiskResult<RiskScoreItem<Map<String, Object>>> evaluate(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph);

    Map<String, Object> describe();
}