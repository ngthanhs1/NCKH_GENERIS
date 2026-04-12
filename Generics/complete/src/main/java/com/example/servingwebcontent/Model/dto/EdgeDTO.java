package com.example.servingwebcontent.Model.dto;

/**
 * DTO tổng quát cho edge (relationship).
 *
 * @param <A> Kiểu dữ liệu attributes (có thể là Map, record/class riêng, v.v.)
 */
public record EdgeDTO<A>(Object from, Object to, String relation, A attributes) {
}
