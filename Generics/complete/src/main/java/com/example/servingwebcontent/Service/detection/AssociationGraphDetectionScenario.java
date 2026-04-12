package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.Model.dto.DetectionResult;
import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import com.example.servingwebcontent.Model.dto.PatternMatch;
import com.example.servingwebcontent.Model.dto.RiskResult;
import com.example.servingwebcontent.Model.dto.RiskScoreItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Kich ban phan tich lien ket tong quat cho cac bo du lieu association/network.
 * Phu hop hon voi du lieu ACCOUNT, PERSON, DEVICE, TRANSACTION... thay vi Email-URL-IP.
 */
@Component
public class AssociationGraphDetectionScenario implements DetectionScenario {

    private static final int HIGH_DEGREE_THRESHOLD = 4;
    private static final int SHARED_TARGET_THRESHOLD = 2;
    private static final int BRIDGE_TYPE_THRESHOLD = 3;

    @Override
    public String key() {
        return "ASSOCIATION_GRAPH";
    }

    @Override
    public String displayName() {
        return "Association Graph Scenario";
    }

    @Override
    public DetectionResult<PatternMatch<Map<String, Object>>> detect(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph) {
        Map<Object, NodeDTO<Map<String, Object>>> nodeById = new LinkedHashMap<>();
        for (NodeDTO<Map<String, Object>> node : graph.nodes()) {
            nodeById.put(node.id(), node);
        }

        Map<Object, Integer> degreeByNode = new LinkedHashMap<>();
        Map<Object, Set<String>> neighborTypesByNode = new LinkedHashMap<>();
        Map<String, Map<Object, Set<Object>>> sharedTargetsByRelation = new LinkedHashMap<>();

        for (EdgeDTO<Map<String, Object>> edge : graph.edges()) {
            degreeByNode.put(edge.from(), degreeByNode.getOrDefault(edge.from(), 0) + 1);
            degreeByNode.put(edge.to(), degreeByNode.getOrDefault(edge.to(), 0) + 1);

            NodeDTO<Map<String, Object>> fromNode = nodeById.get(edge.from());
            NodeDTO<Map<String, Object>> toNode = nodeById.get(edge.to());
            if (fromNode != null && toNode != null) {
                neighborTypesByNode.computeIfAbsent(edge.from(), ignored -> new HashSet<>()).add(toNode.type());
                neighborTypesByNode.computeIfAbsent(edge.to(), ignored -> new HashSet<>()).add(fromNode.type());
            }

            sharedTargetsByRelation
                    .computeIfAbsent(edge.relation(), ignored -> new LinkedHashMap<>())
                    .computeIfAbsent(edge.to(), ignored -> new LinkedHashSet<>())
                    .add(edge.from());
        }

        List<PatternMatch<Map<String, Object>>> patterns = new ArrayList<>();

        for (Map.Entry<Object, Integer> entry : degreeByNode.entrySet()) {
            if (entry.getValue() < HIGH_DEGREE_THRESHOLD) {
                continue;
            }
            NodeDTO<Map<String, Object>> node = nodeById.get(entry.getKey());
            patterns.add(new PatternMatch<>(
                    "HIGH_DEGREE_NODE",
                    "HIGH",
                    "Node co so lien ket lon, co the la diem trung tam trong mang quan he.",
                    List.of(entry.getKey()),
                    Map.of(
                            "degree", entry.getValue(),
                            "nodeType", node != null ? node.type() : "UNKNOWN"
                    )
            ));
        }

        for (Map.Entry<String, Map<Object, Set<Object>>> relationEntry : sharedTargetsByRelation.entrySet()) {
            String relation = relationEntry.getKey();
            for (Map.Entry<Object, Set<Object>> targetEntry : relationEntry.getValue().entrySet()) {
                if (targetEntry.getValue().size() < SHARED_TARGET_THRESHOLD) {
                    continue;
                }
                List<Object> relatedNodeIds = new ArrayList<>();
                relatedNodeIds.add(targetEntry.getKey());
                relatedNodeIds.addAll(targetEntry.getValue());
                patterns.add(new PatternMatch<>(
                        "SHARED_RELATION_TARGET",
                        "MEDIUM",
                        "Nhieu node cung tro toi mot node thong qua cung mot relation.",
                        relatedNodeIds,
                        Map.of(
                                "relation", relation,
                                "sourceCount", targetEntry.getValue().size()
                        )
                ));
            }
        }

        for (Map.Entry<Object, Set<String>> entry : neighborTypesByNode.entrySet()) {
            if (entry.getValue().size() < BRIDGE_TYPE_THRESHOLD) {
                continue;
            }
            patterns.add(new PatternMatch<>(
                    "MULTI_TYPE_BRIDGE",
                    "MEDIUM",
                    "Node lien ket toi nhieu nhom doi tuong khac nhau, dong vai tro cau noi trong do thi.",
                    List.of(entry.getKey()),
                    Map.of(
                            "neighborTypeCount", entry.getValue().size(),
                            "neighborTypes", new ArrayList<>(entry.getValue())
                    )
            ));
        }

        Map<String, Long> countsByRule = new LinkedHashMap<>();
        for (PatternMatch<Map<String, Object>> pattern : patterns) {
            countsByRule.put(pattern.ruleCode(), countsByRule.getOrDefault(pattern.ruleCode(), 0L) + 1L);
        }

        return new DetectionResult<>(patterns, countsByRule, patterns.size());
    }

    @Override
    public RiskResult<RiskScoreItem<Map<String, Object>>> evaluate(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph) {
        Map<Object, NodeDTO<Map<String, Object>>> nodeById = new LinkedHashMap<>();
        Map<Object, Integer> degreeByNode = new LinkedHashMap<>();
        Map<Object, Set<String>> relationTypesByNode = new LinkedHashMap<>();
        Map<Object, Set<String>> neighborTypesByNode = new LinkedHashMap<>();
        Map<String, Map<Object, Set<Object>>> sharedTargetsByRelation = new HashMap<>();

        for (NodeDTO<Map<String, Object>> node : graph.nodes()) {
            nodeById.put(node.id(), node);
            degreeByNode.put(node.id(), 0);
        }

        for (EdgeDTO<Map<String, Object>> edge : graph.edges()) {
            degreeByNode.put(edge.from(), degreeByNode.getOrDefault(edge.from(), 0) + 1);
            degreeByNode.put(edge.to(), degreeByNode.getOrDefault(edge.to(), 0) + 1);
            relationTypesByNode.computeIfAbsent(edge.from(), ignored -> new HashSet<>()).add(edge.relation());
            relationTypesByNode.computeIfAbsent(edge.to(), ignored -> new HashSet<>()).add(edge.relation());

            NodeDTO<Map<String, Object>> fromNode = nodeById.get(edge.from());
            NodeDTO<Map<String, Object>> toNode = nodeById.get(edge.to());
            if (fromNode != null && toNode != null) {
                neighborTypesByNode.computeIfAbsent(edge.from(), ignored -> new HashSet<>()).add(toNode.type());
                neighborTypesByNode.computeIfAbsent(edge.to(), ignored -> new HashSet<>()).add(fromNode.type());
            }

            sharedTargetsByRelation
                    .computeIfAbsent(edge.relation(), ignored -> new LinkedHashMap<>())
                    .computeIfAbsent(edge.to(), ignored -> new LinkedHashSet<>())
                    .add(edge.from());
        }

        List<RiskScoreItem<Map<String, Object>>> items = new ArrayList<>();
        for (NodeDTO<Map<String, Object>> node : graph.nodes()) {
            int degree = degreeByNode.getOrDefault(node.id(), 0);
            int relationTypes = relationTypesByNode.getOrDefault(node.id(), Set.of()).size();
            int neighborTypes = neighborTypesByNode.getOrDefault(node.id(), Set.of()).size();

            boolean sharedTarget = sharedTargetsByRelation.values().stream()
                    .anyMatch(targets -> targets.getOrDefault(node.id(), Set.of()).size() >= SHARED_TARGET_THRESHOLD);

            int score = Math.min(100,
                    Math.min(degree * 10, 40)
                            + (relationTypes >= 3 ? 20 : 0)
                            + (neighborTypes >= BRIDGE_TYPE_THRESHOLD ? 20 : 0)
                            + (sharedTarget ? 20 : 0));

            String verdict = score >= 70 ? "HIGH_INTEREST"
                    : score >= 35 ? "SUSPICIOUS"
                    : "SAFE";

            items.add(new RiskScoreItem<>(
                    node.id(),
                    node.type(),
                    score,
                    verdict,
                    Map.of(
                            "degree", degree,
                            "relationTypeCount", relationTypes,
                            "neighborTypeCount", neighborTypes,
                            "sharedTarget", sharedTarget,
                            "scenario", key()
                    )
            ));
        }

        Map<String, Long> countsByVerdict = new LinkedHashMap<>();
        for (RiskScoreItem<Map<String, Object>> item : items) {
            countsByVerdict.put(item.verdict(), countsByVerdict.getOrDefault(item.verdict(), 0L) + 1L);
        }

        return new RiskResult<>(items, countsByVerdict, items.size());
    }

    @Override
    public Map<String, Object> describe() {
        return Map.of(
                "scenario", key(),
                "displayName", displayName(),
                "description", "Kich ban phan tich lien ket tong quat cho cac bo du lieu association/network.",
                "logic", List.of(
                        "HIGH_DEGREE_NODE",
                        "SHARED_RELATION_TARGET",
                        "MULTI_TYPE_BRIDGE"
                ),
                "thresholds", Map.of(
                        "highDegree", HIGH_DEGREE_THRESHOLD,
                        "sharedTarget", SHARED_TARGET_THRESHOLD,
                        "bridgeTypeCount", BRIDGE_TYPE_THRESHOLD
                )
        );
    }
}