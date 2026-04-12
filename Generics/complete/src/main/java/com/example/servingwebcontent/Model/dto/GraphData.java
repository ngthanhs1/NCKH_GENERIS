package com.example.servingwebcontent.Model.dto;

import java.util.List;

/**
 * Generic container biểu diễn dữ liệu đồ thị (graph) theo cách tổng quát.
 *
 * Ý nghĩa theo đề tài:
 * - Không phụ thuộc domain cụ thể (User/City/Course...).
 * - Có thể tái sử dụng cho nhiều bài toán chỉ bằng cách thay đổi N và E.
 */
public record GraphData<N, E>(List<N> nodes, List<E> edges) {
}
