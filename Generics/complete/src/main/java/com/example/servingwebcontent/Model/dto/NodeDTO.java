package com.example.servingwebcontent.Model.dto;

/**
 * DTO tổng quát cho node.
 *
 * @param <A> Kiểu dữ liệu attributes (có thể là Map, record/class riêng, v.v.)
 */
public record NodeDTO<A>(Object id, String type, A attributes) {
}
