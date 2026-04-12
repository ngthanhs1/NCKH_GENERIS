package com.example.servingwebcontent.Model.dto;

/**
 * Ket qua xoa node theo id.
 */
public record NodeDeleteResult(
        String nodeId,
        long deletedNodes
) {
}