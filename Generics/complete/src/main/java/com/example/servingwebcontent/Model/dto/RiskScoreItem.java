package com.example.servingwebcontent.Model.dto;

/**
 * Generic item cho ket qua cham diem rui ro cua 1 node.
 * @param <M> Kieu metadata giai thich diem.
 */
public record RiskScoreItem<M>(
        Object nodeId,
        String nodeType,
        int score,
        String verdict,
        M evidence
) {
}
