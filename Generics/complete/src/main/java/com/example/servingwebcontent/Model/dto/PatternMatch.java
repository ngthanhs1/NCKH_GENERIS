package com.example.servingwebcontent.Model.dto;

import java.util.List;

/**
 * Ket qua phat hien mot mau nghi ngo.
 * @param <M> Metadata chi tiet cua pattern (rule-specific).
 */
public record PatternMatch<M>(
        String ruleCode,
        String severity,
        String description,
        List<Object> relatedNodeIds,
        M metadata
) {
}
