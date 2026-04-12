package com.example.servingwebcontent;

import com.example.servingwebcontent.Controller.GraphApiController;
import com.example.servingwebcontent.Controller.RestExceptionHandler;
import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.GraphImportResult;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import com.example.servingwebcontent.Model.dto.NodeDeleteResult;
import com.example.servingwebcontent.Service.GraphQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GraphApiController.class)
@Import({RestExceptionHandler.class})
class GraphApiControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private GraphQueryService graphQueryService;

    @Test
    void getGraph_returnsApiResponseWithGenericGraphData() throws Exception {
        var nodes = List.of(
                new NodeDTO<Map<String, Object>>(
                        "n1",
                        "Person",
                        Map.of("name", "Alice")
                )
        );

        var edges = List.of(
                new EdgeDTO<Map<String, Object>>(
                        "n1",
                        "n2",
                        "KNOWS",
                        Map.of("since", 2020)
                )
        );

        when(graphQueryService.fetchGraph()).thenReturn(new GraphData<>(nodes, edges));

        mockMvc.perform(get("/api/graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.nodes[0].id").value("n1"))
                .andExpect(jsonPath("$.data.nodes[0].type").value("Person"))
                .andExpect(jsonPath("$.data.nodes[0].attributes.name").value("Alice"))
                .andExpect(jsonPath("$.data.edges[0].from").value("n1"))
                .andExpect(jsonPath("$.data.edges[0].to").value("n2"))
                .andExpect(jsonPath("$.data.edges[0].relation").value("KNOWS"))
                .andExpect(jsonPath("$.data.edges[0].attributes.since").value(2020));
    }

    @Test
    void importGraph_acceptsGenericGraphPayload() throws Exception {
        GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> payload = new GraphData<>(
                List.of(new NodeDTO<>("p1", "PERSON", Map.of("name", "Alice"))),
                List.of(new EdgeDTO<>("p1", "d1", "USES_DEVICE", Map.of("since", "2026-03-10")))
        );

        when(graphQueryService.importGraph(any(), eq(true)))
                .thenReturn(new GraphImportResult(1, 1, true));

        mockMvc.perform(post("/api/graph/import")
                        .queryParam("replace", "true")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(payload))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.data.importedNodes").value(1))
                .andExpect(jsonPath("$.data.importedEdges").value(1))
                .andExpect(jsonPath("$.data.replaceMode").value(true));
    }

        @Test
        void deleteNode_returnsDeletionResult() throws Exception {
                when(graphQueryService.deleteNodeById("p1"))
                                .thenReturn(new NodeDeleteResult("p1", 1));

                mockMvc.perform(delete("/api/graph/nodes/p1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("OK"))
                                .andExpect(jsonPath("$.data.nodeId").value("p1"))
                                .andExpect(jsonPath("$.data.deletedNodes").value(1));
        }
}
