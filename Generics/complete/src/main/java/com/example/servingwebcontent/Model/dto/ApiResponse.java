package com.example.servingwebcontent.Model.dto;

/**
 * Generic API response wrapper.
 *
 * Ý nghĩa theo đề tài:
 * - Chuẩn hoá response cho mọi API mà không lặp code.
 * - data mang kiểu T giúp type-safe và tái sử dụng.
 */
public record ApiResponse<T>(T data, String message) {

	public static <R> ApiResponse<R> ok(R data) {
		return new ApiResponse<>(data, "OK");
	}
}
