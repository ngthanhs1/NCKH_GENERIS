package com.example.servingwebcontent.Controller;

import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global Exception Handler cho tất cả REST Controllers
 * Mục đích: Xử lý exceptions và trả về response lỗi có cấu trúc thống nhất
 * @RestControllerAdvice - áp dụng cho tất cả @RestController trong application
 */
@RestControllerAdvice
public class RestExceptionHandler {

    /**
     * Xử lý lỗi xác thực Neo4j (sai username/password)
     * @param ex Exception khi Neo4j từ chối quyền truy cập
     * @return HTTP 401 Unauthorized với thông tin lỗi chi tiết
     */
    @ExceptionHandler(PermissionDeniedDataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleNeo4jPermissionDenied(PermissionDeniedDataAccessException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "NEO4J_UNAUTHORIZED",
                "message",
                "Neo4j Aura authentication failed. Please verify spring.neo4j.authentication.username/password.",
                "details",
                ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "INVALID_GRAPH_PAYLOAD",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "MALFORMED_JSON",
                "message", "JSON import không hợp lệ. Kiểm tra dấu phẩy, dấu ngoặc và cấu trúc nodes/edges.",
                "details", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()
        ));
    }
}
