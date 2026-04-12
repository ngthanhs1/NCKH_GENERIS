package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import com.example.servingwebcontent.Model.dto.PatternMatch;

import java.util.List;
import java.util.Map;

/**
 * Rule interface tong quat de phat hien pattern tren graph.
 * @param <M> Kieu metadata cua pattern rule.
 */
public interface PatternRule<M> {
    String code();

    List<PatternMatch<M>> detect(GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph);
}
