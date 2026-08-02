package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service thuc hien hoc may tren do thi (Graph Machine Learning)
 * bao gom Node2Vec Random Walk Embedding va Anomaly Detection
 * truc tiep tren cau truc GraphData<N, E> tong quat.
 */
@Service
public class GraphEmbeddingService {

    private static final int EMBEDDING_DIMENSIONS = 8;
    private static final int WALKS_PER_NODE = 10;
    private static final int WALK_LENGTH = 10;
    private static final int WINDOW_SIZE = 3;

    public record EmbeddingResult(
            Map<Object, double[]> nodeEmbeddings,
            Map<Object, Double> anomalyScores,
            Map<Object, String> aiVerdicts
    ) {}

    public EmbeddingResult computeGraphEmbeddingsAndAnomalies(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph) {

        List<NodeDTO<Map<String, Object>>> nodes = graph.nodes() == null ? List.of() : graph.nodes();
        List<EdgeDTO<Map<String, Object>>> edges = graph.edges() == null ? List.of() : graph.edges();

        if (nodes.isEmpty()) {
            return new EmbeddingResult(Map.of(), Map.of(), Map.of());
        }

        Set<Object> nodeIds = new LinkedHashSet<>();
        Map<Object, List<Object>> adjacencyList = new HashMap<>();

        for (NodeDTO<Map<String, Object>> node : nodes) {
            if (node != null && node.id() != null) {
                nodeIds.add(node.id());
                adjacencyList.put(node.id(), new ArrayList<>());
            }
        }

        for (EdgeDTO<Map<String, Object>> edge : edges) {
            if (edge == null || edge.from() == null || edge.to() == null) continue;
            Object u = edge.from();
            Object v = edge.to();
            if (nodeIds.contains(u) && nodeIds.contains(v)) {
                adjacencyList.computeIfAbsent(u, k -> new ArrayList<>()).add(v);
                adjacencyList.computeIfAbsent(v, k -> new ArrayList<>()).add(u);
            }
        }

        // 1. Sinh Random Walks (Node2Vec Walk Generator)
        List<List<Object>> walks = generateRandomWalks(nodeIds, adjacencyList);

        // 2. Co-occurrence Matrix & Feature Vector Embedding Generation
        Map<Object, double[]> embeddings = generateEmbeddingsFromWalks(nodeIds, walks);

        // 3. Mahalanobis / Distance Anomaly Detection Model
        Map<Object, Double> anomalyScores = calculateAnomalyScores(nodeIds, embeddings);

        // 4. Compute AI Verdicts
        Map<Object, String> aiVerdicts = new LinkedHashMap<>();
        for (Map.Entry<Object, Double> entry : anomalyScores.entrySet()) {
            double score = entry.getValue();
            String verdict = score >= 75.0 ? "CRITICAL_AI_ANOMALY"
                    : score >= 50.0 ? "HIGH_AI_RISK"
                    : score >= 25.0 ? "MODERATE_RISK"
                    : "NORMAL_PATTERN";
            aiVerdicts.put(entry.getKey(), verdict);
        }

        return new EmbeddingResult(embeddings, anomalyScores, aiVerdicts);
    }

    private List<List<Object>> generateRandomWalks(Set<Object> nodeIds, Map<Object, List<Object>> adj) {
        List<List<Object>> walks = new ArrayList<>();
        Random random = new Random(42);

        for (Object startNode : nodeIds) {
            for (int w = 0; w < WALKS_PER_NODE; w++) {
                List<Object> walk = new ArrayList<>();
                Object current = startNode;
                walk.add(current);

                for (int step = 0; step < WALK_LENGTH - 1; step++) {
                    List<Object> neighbors = adj.getOrDefault(current, List.of());
                    if (neighbors.isEmpty()) break;
                    current = neighbors.get(random.nextInt(neighbors.size()));
                    walk.add(current);
                }
                walks.add(walk);
            }
        }
        return walks;
    }

    private Map<Object, double[]> generateEmbeddingsFromWalks(Set<Object> nodeIds, List<List<Object>> walks) {
        Map<Object, Map<Object, Integer>> coMatrix = new HashMap<>();
        for (Object id : nodeIds) {
            coMatrix.put(id, new HashMap<>());
        }

        for (List<Object> walk : walks) {
            for (int i = 0; i < walk.size(); i++) {
                Object target = walk.get(i);
                int start = Math.max(0, i - WINDOW_SIZE);
                int end = Math.min(walk.size() - 1, i + WINDOW_SIZE);

                for (int j = start; j <= end; j++) {
                    if (i != j) {
                        Object context = walk.get(j);
                        Map<Object, Integer> targetCounts = coMatrix.get(target);
                        if (targetCounts != null) {
                            targetCounts.put(context, targetCounts.getOrDefault(context, 0) + 1);
                        }
                    }
                }
            }
        }

        List<Object> idList = new ArrayList<>(nodeIds);
        Map<Object, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < idList.size(); i++) {
            indexMap.put(idList.get(i), i);
        }

        Map<Object, double[]> embeddings = new LinkedHashMap<>();
        int N = idList.size();

        for (int i = 0; i < N; i++) {
            Object id = idList.get(i);
            double[] vector = new double[EMBEDDING_DIMENSIONS];
            Map<Object, Integer> counts = coMatrix.getOrDefault(id, Map.of());

            for (Map.Entry<Object, Integer> entry : counts.entrySet()) {
                Integer colIdx = indexMap.get(entry.getKey());
                if (colIdx != null) {
                    double weight = Math.log(1.0 + entry.getValue());
                    int dim = colIdx % EMBEDDING_DIMENSIONS;
                    vector[dim] += weight;
                }
            }

            // Normalize vector to unit length
            double norm = 0.0;
            for (double val : vector) norm += val * val;
            norm = Math.sqrt(norm);
            if (norm > 0) {
                for (int d = 0; d < EMBEDDING_DIMENSIONS; d++) {
                    vector[d] /= norm;
                }
            }

            embeddings.put(id, vector);
        }

        return embeddings;
    }

    private Map<Object, Double> calculateAnomalyScores(Set<Object> nodeIds, Map<Object, double[]> embeddings) {
        if (nodeIds.isEmpty()) return Map.of();

        double[] centroid = new double[EMBEDDING_DIMENSIONS];
        for (double[] vec : embeddings.values()) {
            for (int d = 0; d < EMBEDDING_DIMENSIONS; d++) {
                centroid[d] += vec[d];
            }
        }
        int N = nodeIds.size();
        for (int d = 0; d < EMBEDDING_DIMENSIONS; d++) {
            centroid[d] /= N;
        }

        Map<Object, Double> distances = new HashMap<>();
        double maxDist = 0.0;

        for (Object id : nodeIds) {
            double[] vec = embeddings.get(id);
            double dist = 0.0;
            if (vec != null) {
                for (int d = 0; d < EMBEDDING_DIMENSIONS; d++) {
                    double diff = vec[d] - centroid[d];
                    dist += diff * diff;
                }
                dist = Math.sqrt(dist);
            }
            distances.put(id, dist);
            if (dist > maxDist) maxDist = dist;
        }

        Map<Object, Double> anomalyScores = new LinkedHashMap<>();
        for (Object id : nodeIds) {
            double dist = distances.getOrDefault(id, 0.0);
            double rawScore = maxDist > 0 ? (dist / maxDist) * 100.0 : 0.0;
            anomalyScores.put(id, Math.round(rawScore * 10.0) / 10.0);
        }

        return anomalyScores;
    }
}
