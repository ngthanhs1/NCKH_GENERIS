package com.example.servingwebcontent.Controller;

import com.example.servingwebcontent.Model.dto.ApiResponse;
import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.GraphImportResult;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import com.example.servingwebcontent.Model.dto.NodeDeleteResult;
import com.example.servingwebcontent.Service.GraphQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller API để truy vấn và trả về dữ liệu đồ thị từ Neo4j
 * Mục đích: Cung cấp endpoint REST API để lấy toàn bộ graph (nodes + edges)
 * cho việc visualization trên frontend
 */
@RestController
@RequestMapping("/api/graph")
public class GraphApiController {

    private final GraphQueryService graphQueryService;

    /**
     * Constructor injection - Spring tự động inject Neo4jClient bean
     * @param neo4j Neo4j client để kết nối và query database
     */
    public GraphApiController(GraphQueryService graphQueryService) {
        this.graphQueryService = graphQueryService;
    }

    /**
     * GET /api/graph - Trả về toàn bộ đồ thị dưới dạng JSON
     * Response format: { "nodes": [...], "edges": [...] }
     */
    @GetMapping
    public ApiResponse<GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>>> getGraph() {
        return ApiResponse.ok(graphQueryService.fetchGraph());
    }

    @PostMapping("/import")
    public ApiResponse<GraphImportResult> importGraph(
            @RequestBody GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graphData,
            @RequestParam(name = "replace", defaultValue = "false") boolean replace) {
        return ApiResponse.ok(graphQueryService.importGraph(graphData, replace));
    }

    @DeleteMapping("/nodes/{nodeId}")
    public ApiResponse<NodeDeleteResult> deleteNode(@PathVariable String nodeId) {
        return ApiResponse.ok(graphQueryService.deleteNodeById(nodeId));
    }
}
