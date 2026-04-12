package com.example.servingwebcontent.Model.dto;

import java.util.List;
import java.util.Map;

/**
 * Generic container tong hop ket qua scoring.
 * @param <R> Kieu item diem rui ro.
 */
public record RiskResult<R>(
        List<R> items,
        Map<String, Long> countsByVerdict,
        long totalItems
) {
}
