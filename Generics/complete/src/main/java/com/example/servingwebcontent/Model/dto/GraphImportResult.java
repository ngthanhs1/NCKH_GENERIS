package com.example.servingwebcontent.Model.dto;

/**
 * Ket qua import graph tong quat tu UI/API.
 */
public record GraphImportResult(
        long importedNodes,
        long importedEdges,
        boolean replaceMode
) {
}