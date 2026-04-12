package com.example.servingwebcontent.Service;

import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.GraphImportResult;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import com.example.servingwebcontent.Model.dto.NodeDeleteResult;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Truy van graph theo schema tong quat (:Node)-[:RELATION]->(:Node).
 * Service nay duoc dung lai cho API visualization va detection.
 */
@Service
public class GraphQueryService {

    private final Neo4jClient neo4j;

    public GraphQueryService(Neo4jClient neo4j) {
        this.neo4j = neo4j;
    }

    private static Map<String, Object> toAttributes(Object rawProps, String... removeKeys) {
        Map<String, Object> attributes = new HashMap<>();
        if (rawProps instanceof Map<?, ?> props) {
            for (Map.Entry<?, ?> entry : props.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    if (key.startsWith("attr_")) {
                        attributes.put(key.substring("attr_".length()), entry.getValue());
                    } else {
                        attributes.put(key, entry.getValue());
                    }
                }
            }
        }

        for (String key : removeKeys) {
            attributes.remove(key);
        }
        return attributes;
    }

    private static Map<String, Object> toStoredProperties(Map<String, Object> attributes) {
        Map<String, Object> properties = new HashMap<>();
        if (attributes == null) {
            return properties;
        }

        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isBlank()) {
                continue;
            }
            if (key.startsWith("attr_")) {
                properties.put(key, entry.getValue());
            } else {
                properties.put("attr_" + key, entry.getValue());
            }
        }
        return properties;
    }

    public GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> fetchGraph() {
        Collection<Map<String, Object>> rawNodes = neo4j.query("""
                MATCH (n:Node)
                RETURN n.id AS id,
                       n.type AS type,
                       properties(n) AS props
                ORDER BY id
                """).fetch().all();

        Collection<Map<String, Object>> rawEdges = neo4j.query("""
                MATCH (a:Node)-[r:RELATION]->(b:Node)
                RETURN a.id AS `from`,
                       b.id AS `to`,
                       r.type AS relation,
                       properties(r) AS props
                """).fetch().all();

        List<NodeDTO<Map<String, Object>>> nodes = new ArrayList<>(rawNodes.size());
        for (Map<String, Object> row : rawNodes) {
            Map<String, Object> attributes = toAttributes(row.get("props"), "id", "type");
            nodes.add(new NodeDTO<>(
                    row.get("id"),
                    (String) row.get("type"),
                    attributes
            ));
        }

        List<EdgeDTO<Map<String, Object>>> edges = new ArrayList<>(rawEdges.size());
        for (Map<String, Object> row : rawEdges) {
            Map<String, Object> attributes = toAttributes(row.get("props"), "type");
            edges.add(new EdgeDTO<>(
                    row.get("from"),
                    row.get("to"),
                    (String) row.get("relation"),
                    attributes
            ));
        }

        return new GraphData<>(nodes, edges);
    }

    private Set<String> fetchExistingNodeIds() {
        Collection<String> ids = neo4j.query("""
                MATCH (n:Node)
                RETURN n.id AS id
                """)
                .fetchAs(String.class)
                .all();
        return new HashSet<>(ids);
    }

    private static String normalizeId(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private void validateGraphImport(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graphData,
            boolean replaceMode) {
        if (graphData == null) {
            throw new IllegalArgumentException("Payload import không hợp lệ: thiếu đối tượng graphData.");
        }

        List<NodeDTO<Map<String, Object>>> nodes = graphData.nodes() == null ? List.of() : graphData.nodes();
        List<EdgeDTO<Map<String, Object>>> edges = graphData.edges() == null ? List.of() : graphData.edges();

        Set<String> knownNodeIds = replaceMode ? new HashSet<>() : fetchExistingNodeIds();
        Set<String> payloadNodeIds = new HashSet<>();

        for (int index = 0; index < nodes.size(); index++) {
            NodeDTO<Map<String, Object>> node = nodes.get(index);
            if (node == null) {
                throw new IllegalArgumentException("Payload import không hợp lệ: nodes[" + index + "] đang bị null.");
            }

            String nodeId = normalizeId(node.id());
            if (nodeId == null) {
                throw new IllegalArgumentException("Payload import không hợp lệ: nodes[" + index + "] thiếu id.");
            }

            if (node.type() == null || node.type().isBlank()) {
                throw new IllegalArgumentException("Payload import không hợp lệ: node '" + nodeId + "' thiếu type.");
            }

            if (!payloadNodeIds.add(nodeId)) {
                throw new IllegalArgumentException("Payload import không hợp lệ: id node bị trùng '" + nodeId + "'.");
            }

            knownNodeIds.add(nodeId);
        }

        for (int index = 0; index < edges.size(); index++) {
            EdgeDTO<Map<String, Object>> edge = edges.get(index);
            if (edge == null) {
                throw new IllegalArgumentException("Payload import không hợp lệ: edges[" + index + "] đang bị null.");
            }

            String fromId = normalizeId(edge.from());
            String toId = normalizeId(edge.to());

            if (fromId == null) {
                throw new IllegalArgumentException("Payload import không hợp lệ: edges[" + index + "] thiếu from.");
            }

            if (toId == null) {
                throw new IllegalArgumentException("Payload import không hợp lệ: edges[" + index + "] thiếu to.");
            }

            if (edge.relation() == null || edge.relation().isBlank()) {
                throw new IllegalArgumentException("Payload import không hợp lệ: edge '" + fromId + " -> " + toId + "' thiếu relation.");
            }

            if (!knownNodeIds.contains(fromId)) {
                throw new IllegalArgumentException("Payload import không hợp lệ: edge tham chiếu node nguồn không tồn tại '" + fromId + "'.");
            }

            if (!knownNodeIds.contains(toId)) {
                throw new IllegalArgumentException("Payload import không hợp lệ: edge tham chiếu node đích không tồn tại '" + toId + "'.");
            }
        }
    }

    @Transactional("transactionManager")
    public GraphImportResult importGraph(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graphData,
            boolean replaceMode) {
        validateGraphImport(graphData, replaceMode);

        List<NodeDTO<Map<String, Object>>> nodes = graphData == null || graphData.nodes() == null
                ? List.of()
                : graphData.nodes();
        List<EdgeDTO<Map<String, Object>>> edges = graphData == null || graphData.edges() == null
                ? List.of()
                : graphData.edges();

        if (replaceMode) {
            neo4j.query("MATCH (n:Node) DETACH DELETE n").run();
        }

        List<Map<String, Object>> nodeRows = new ArrayList<>(nodes.size());
        for (NodeDTO<Map<String, Object>> node : nodes) {
            if (node == null || node.id() == null || node.type() == null || node.type().isBlank()) {
                continue;
            }
            Map<String, Object> props = new HashMap<>();
            props.put("id", node.id());
            props.put("type", node.type());
            props.putAll(toStoredProperties(node.attributes()));

            nodeRows.add(Map.of(
                    "id", node.id(),
                    "props", props
            ));
        }

        if (!nodeRows.isEmpty()) {
            neo4j.query("""
                    UNWIND $nodes AS node
                    MERGE (n:Node {id: node.id})
                    SET n = node.props
                    """)
                    .bind(nodeRows).to("nodes")
                    .run();
        }

        List<Map<String, Object>> edgeRows = new ArrayList<>(edges.size());
        for (EdgeDTO<Map<String, Object>> edge : edges) {
            if (edge == null || edge.from() == null || edge.to() == null || edge.relation() == null || edge.relation().isBlank()) {
                continue;
            }
            Map<String, Object> props = new HashMap<>();
            props.put("type", edge.relation());
            props.putAll(toStoredProperties(edge.attributes()));

            edgeRows.add(Map.of(
                    "from", edge.from(),
                    "to", edge.to(),
                    "relation", edge.relation(),
                    "props", props
            ));
        }

        if (!edgeRows.isEmpty()) {
            neo4j.query("""
                    UNWIND $edges AS edge
                    MATCH (from:Node {id: edge.from})
                    MATCH (to:Node {id: edge.to})
                    MERGE (from)-[r:RELATION {type: edge.relation}]->(to)
                    SET r = edge.props
                    """)
                    .bind(edgeRows).to("edges")
                    .run();
        }

        return new GraphImportResult(nodeRows.size(), edgeRows.size(), replaceMode);
    }

    @Transactional("transactionManager")
    public NodeDeleteResult deleteNodeById(Object nodeId) {
        String normalizedId = normalizeId(nodeId);
        if (normalizedId == null) {
            throw new IllegalArgumentException("Không thể xóa node: thiếu id node.");
        }

        Long deletedNodes = neo4j.query("""
                OPTIONAL MATCH (n:Node {id: $id})
                WITH collect(n) AS nodes
                FOREACH (node IN nodes | DETACH DELETE node)
                RETURN size(nodes) AS deletedNodes
                """)
                .bind(normalizedId).to("id")
                .fetchAs(Long.class)
                .one()
                .orElse(0L);

        return new NodeDeleteResult(normalizedId, deletedNodes);
    }
}
