package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service tinh toan cac thuat toan phan tich do thi nâng cao (Graph Analytics)
 * bao gom PageRank Centrality va Label Propagation Community Detection (LPA)
 * truc tiep tren cau truc generic GraphData<N, E>.
 */
@Service
public class GraphAnalyticsService {

    private static final double DAMPING_FACTOR = 0.85;
    private static final int PAGERANK_ITERATIONS = 20;
    private static final int LPA_MAX_ITERATIONS = 15;

    public record AnalyticsResult(
            Map<Object, Double> pageRankScores,
            Map<Object, String> communityIds,
            Map<String, List<Object>> communityMembers
    ) {}

    /**
     * Tinh toan PageRank va Community Detection cho mot GraphData tong quat.
     */
    public AnalyticsResult analyzeGraph(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph) {

        List<NodeDTO<Map<String, Object>>> nodes = graph.nodes() == null ? List.of() : graph.nodes();
        List<EdgeDTO<Map<String, Object>>> edges = graph.edges() == null ? List.of() : graph.edges();

        if (nodes.isEmpty()) {
            return new AnalyticsResult(Map.of(), Map.of(), Map.of());
        }

        Set<Object> nodeIds = new LinkedHashSet<>();
        Map<Object, Set<Object>> neighbors = new HashMap<>();
        Map<Object, Set<Object>> inNeighbors = new HashMap<>();
        Map<Object, Integer> outDegree = new HashMap<>();

        for (NodeDTO<Map<String, Object>> node : nodes) {
            if (node != null && node.id() != null) {
                nodeIds.add(node.id());
                neighbors.put(node.id(), new HashSet<>());
                inNeighbors.put(node.id(), new HashSet<>());
                outDegree.put(node.id(), 0);
            }
        }

        for (EdgeDTO<Map<String, Object>> edge : edges) {
            if (edge == null || edge.from() == null || edge.to() == null) continue;
            Object from = edge.from();
            Object to = edge.to();

            if (nodeIds.contains(from) && nodeIds.contains(to)) {
                neighbors.computeIfAbsent(from, k -> new HashSet<>()).add(to);
                neighbors.computeIfAbsent(to, k -> new HashSet<>()).add(from);

                inNeighbors.computeIfAbsent(to, k -> new HashSet<>()).add(from);
                outDegree.put(from, outDegree.getOrDefault(from, 0) + 1);
            }
        }

        // 1. Tinh PageRank Centrality
        Map<Object, Double> pageRank = calculatePageRank(nodeIds, inNeighbors, outDegree);

        // 2. Tinh Label Propagation Algorithm (LPA) cho Community Detection
        Map<Object, String> communityIds = calculateCommunities(nodeIds, neighbors);

        // 3. Gom nhom danh sach thanh vien theo Community
        Map<String, List<Object>> communityMembers = new LinkedHashMap<>();
        for (Map.Entry<Object, String> entry : communityIds.entrySet()) {
            communityMembers.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        return new AnalyticsResult(pageRank, communityIds, communityMembers);
    }

    private Map<Object, Double> calculatePageRank(
            Set<Object> nodeIds,
            Map<Object, Set<Object>> inNeighbors,
            Map<Object, Integer> outDegree) {

        int totalNodes = nodeIds.size();
        double initialRank = 1.0 / totalNodes;

        Map<Object, Double> currentRank = new HashMap<>();
        for (Object id : nodeIds) {
            currentRank.put(id, initialRank);
        }

        for (int iter = 0; iter < PAGERANK_ITERATIONS; iter++) {
            Map<Object, Double> nextRank = new HashMap<>();
            double randomJump = (1.0 - DAMPING_FACTOR) / totalNodes;

            for (Object node : nodeIds) {
                double rankSum = 0.0;
                Set<Object> incoming = inNeighbors.getOrDefault(node, Set.of());
                for (Object inNode : incoming) {
                    int outDeg = outDegree.getOrDefault(inNode, 0);
                    if (outDeg > 0) {
                        rankSum += currentRank.getOrDefault(inNode, initialRank) / outDeg;
                    }
                }
                nextRank.put(node, randomJump + DAMPING_FACTOR * rankSum);
            }
            currentRank = nextRank;
        }

        // Normalize PageRank values to [0..100]
        double maxRank = currentRank.values().stream().mapToDouble(Double::doubleValue).max().orElse(1.0);
        Map<Object, Double> normalizedRank = new LinkedHashMap<>();
        for (Map.Entry<Object, Double> entry : currentRank.entrySet()) {
            double score = maxRank > 0 ? (entry.getValue() / maxRank) * 100.0 : 0.0;
            normalizedRank.put(entry.getKey(), Math.round(score * 10.0) / 10.0);
        }

        return normalizedRank;
    }

    private Map<Object, String> calculateCommunities(Set<Object> nodeIds, Map<Object, Set<Object>> neighbors) {
        Map<Object, String> labels = new HashMap<>();
        for (Object id : nodeIds) {
            labels.put(id, "COMMUNITY_" + id);
        }

        List<Object> nodeList = new ArrayList<>(nodeIds);

        for (int iter = 0; iter < LPA_MAX_ITERATIONS; iter++) {
            Collections.shuffle(nodeList);
            boolean changed = false;

            for (Object node : nodeList) {
                Set<Object> neighborSet = neighbors.getOrDefault(node, Set.of());
                if (neighborSet.isEmpty()) continue;

                Map<String, Integer> labelCounts = new HashMap<>();
                for (Object neighbor : neighborSet) {
                    String label = labels.get(neighbor);
                    if (label != null) {
                        labelCounts.put(label, labelCounts.getOrDefault(label, 0) + 1);
                    }
                }

                if (labelCounts.isEmpty()) continue;

                int maxCount = Collections.max(labelCounts.values());
                List<String> bestLabels = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : labelCounts.entrySet()) {
                    if (entry.getValue() == maxCount) {
                        bestLabels.add(entry.getKey());
                    }
                }

                String currentLabel = labels.get(node);
                if (!bestLabels.contains(currentLabel)) {
                    String newLabel = bestLabels.get(0);
                    labels.put(node, newLabel);
                    changed = true;
                }
            }

            if (!changed) break;
        }

        return labels;
    }

    /**
     * Tinh do tuong dong Jaccard Similarity giua 2 nut dua tren tap hang xom chung.
     */
    public double calculateJaccardSimilarity(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph,
            Object nodeA,
            Object nodeB) {

        Set<Object> neighborsA = new HashSet<>();
        Set<Object> neighborsB = new HashSet<>();

        for (EdgeDTO<Map<String, Object>> edge : graph.edges() == null ? List.<EdgeDTO<Map<String, Object>>>of() : graph.edges()) {
            if (edge == null) continue;
            if (Objects.equals(edge.from(), nodeA)) neighborsA.add(edge.to());
            if (Objects.equals(edge.to(), nodeA)) neighborsA.add(edge.from());

            if (Objects.equals(edge.from(), nodeB)) neighborsB.add(edge.to());
            if (Objects.equals(edge.to(), nodeB)) neighborsB.add(edge.from());
        }

        if (neighborsA.isEmpty() && neighborsB.isEmpty()) return 0.0;

        Set<Object> intersection = new HashSet<>(neighborsA);
        intersection.retainAll(neighborsB);

        Set<Object> union = new HashSet<>(neighborsA);
        union.addAll(neighborsB);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * Tim duong di ngan nhat giua 2 nut su dung thuat toan Dijkstra.
     */
    public List<Object> findShortestPathDijkstra(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph,
            Object startNode,
            Object targetNode) {

        Map<Object, List<Object>> adj = new HashMap<>();
        for (EdgeDTO<Map<String, Object>> edge : graph.edges() == null ? List.<EdgeDTO<Map<String, Object>>>of() : graph.edges()) {
            if (edge == null || edge.from() == null || edge.to() == null) continue;
            adj.computeIfAbsent(edge.from(), k -> new ArrayList<>()).add(edge.to());
            adj.computeIfAbsent(edge.to(), k -> new ArrayList<>()).add(edge.from());
        }

        if (!adj.containsKey(startNode) || !adj.containsKey(targetNode)) {
            return List.of();
        }

        Map<Object, Integer> dist = new HashMap<>();
        Map<Object, Object> prev = new HashMap<>();
        Queue<Object> queue = new LinkedList<>();

        dist.put(startNode, 0);
        queue.add(startNode);

        while (!queue.isEmpty()) {
            Object current = queue.poll();
            if (current.equals(targetNode)) break;

            int currentDist = dist.get(current);
            for (Object neighbor : adj.getOrDefault(current, List.of())) {
                if (!dist.containsKey(neighbor)) {
                    dist.put(neighbor, currentDist + 1);
                    prev.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        if (!dist.containsKey(targetNode)) {
            return List.of();
        }

        List<Object> path = new LinkedList<>();
        Object curr = targetNode;
        while (curr != null) {
            path.add(0, curr);
            curr = prev.get(curr);
        }

        return path;
    }
}
