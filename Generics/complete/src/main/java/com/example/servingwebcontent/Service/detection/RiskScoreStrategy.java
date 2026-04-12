package com.example.servingwebcontent.Service.detection;

/**
 * Strategy generic cho scoring theo tung profile bai toan.
 * @param <C> Context dau vao.
 * @param <R> Kieu ket qua.
 */
public interface RiskScoreStrategy<C, R> {
    R evaluate(C context);
}
