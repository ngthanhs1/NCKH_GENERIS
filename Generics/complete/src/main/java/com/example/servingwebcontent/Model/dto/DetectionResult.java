package com.example.servingwebcontent.Model.dto;

import java.util.List;
import java.util.Map;

/**
 * Tong hop ket qua nhan dien mau nghi ngo.
 * @param <P> Kieu pattern item.
 */
public record DetectionResult<P>(
        List<P> patterns,
        Map<String, Long> countsByRule,
        long totalPatterns
) {
}
