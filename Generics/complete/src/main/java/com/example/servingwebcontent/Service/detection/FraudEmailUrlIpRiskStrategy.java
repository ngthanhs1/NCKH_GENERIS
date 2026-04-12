package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import com.example.servingwebcontent.Model.dto.RiskScoreItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Profile scoring cho bai toan fraud Email-URL-IP.
 */
@Component
public class FraudEmailUrlIpRiskStrategy implements RiskScoreStrategy<
        GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>>,
        List<RiskScoreItem<Map<String, Object>>>> {

    private final AnalysisProfileSupport profileSupport;

    public FraudEmailUrlIpRiskStrategy(AnalysisProfileSupport profileSupport) {
        this.profileSupport = profileSupport;
    }

    @Override
    public List<RiskScoreItem<Map<String, Object>>> evaluate(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph) {
        Map<Object, String> nodeTypeById = new HashMap<>();
        for (NodeDTO<Map<String, Object>> node : graph.nodes()) {
            nodeTypeById.put(node.id(), node.type());
        }

        Map<Object, Set<Object>> emailToUrls = new HashMap<>();
        Map<Object, Set<Object>> emailToIps = new HashMap<>();
        Map<Object, Set<Object>> urlToEmails = new HashMap<>();
        Map<Object, Set<Object>> ipToEmails = new HashMap<>();
        Map<Object, Set<Object>> ipToUrls = new HashMap<>();

        for (EdgeDTO<Map<String, Object>> edge : graph.edges()) {
            String fromType = nodeTypeById.get(edge.from());
            String toType = nodeTypeById.get(edge.to());

            if (profileSupport.isRelation(edge.relation(), "sourceToResource")
                    && isSource(fromType)
                    && isResource(toType)) {
                emailToUrls.computeIfAbsent(edge.from(), ignored -> new HashSet<>()).add(edge.to());
                urlToEmails.computeIfAbsent(edge.to(), ignored -> new HashSet<>()).add(edge.from());
            }

            if (profileSupport.isRelation(edge.relation(), "sourceToInfrastructure")
                    && isSource(fromType)
                    && isInfrastructure(toType)) {
                emailToIps.computeIfAbsent(edge.from(), ignored -> new HashSet<>()).add(edge.to());
                ipToEmails.computeIfAbsent(edge.to(), ignored -> new HashSet<>()).add(edge.from());
            }

            if (profileSupport.isRelation(edge.relation(), "resourceToInfrastructure")
                    && isResource(fromType)
                    && isInfrastructure(toType)) {
                ipToUrls.computeIfAbsent(edge.to(), ignored -> new HashSet<>()).add(edge.from());
            }
        }

        List<RiskScoreItem<Map<String, Object>>> out = new ArrayList<>();
        for (NodeDTO<Map<String, Object>> node : graph.nodes()) {
            String type = node.type();
            Object id = node.id();
            int score = 0;
            List<String> reasons = new ArrayList<>();

            if (isSource(type)) {
                if (!emailToUrls.getOrDefault(id, Set.of()).isEmpty()) {
                    score += profileSupport.properties().scoreWeight("sourceHasResource");
                    reasons.add("Source node links to at least one resource node");
                }
                if (!emailToIps.getOrDefault(id, Set.of()).isEmpty()) {
                    score += profileSupport.properties().scoreWeight("sourceHasInfrastructure");
                    reasons.add("Source node links to at least one infrastructure node");
                }

                boolean sharedUrl = emailToUrls.getOrDefault(id, Set.of()).stream()
                        .anyMatch(urlId -> urlToEmails.getOrDefault(urlId, Set.of()).size() > 1);
                if (sharedUrl) {
                    score += profileSupport.properties().scoreWeight("sharedResource");
                    reasons.add("Source node shares a resource node with other sources");
                }

                boolean sharedIp = emailToIps.getOrDefault(id, Set.of()).stream()
                        .anyMatch(ipId -> ipToEmails.getOrDefault(ipId, Set.of()).size() > 1);
                if (sharedIp) {
                    score += profileSupport.properties().scoreWeight("sharedInfrastructure");
                    reasons.add("Source node shares an infrastructure node with other sources");
                }
            }

            if (isResource(type)) {
                int emailCount = urlToEmails.getOrDefault(id, Set.of()).size();
                if (emailCount > 1) {
                    score += profileSupport.properties().scoreWeight("resourceSharedByManySources");
                    reasons.add("Resource node is reused by multiple source nodes");
                }
            }

            if (isInfrastructure(type)) {
                int senderCount = ipToEmails.getOrDefault(id, Set.of()).size();
                int hostedUrlCount = ipToUrls.getOrDefault(id, Set.of()).size();
                if (senderCount > 1) {
                    score += profileSupport.properties().scoreWeight("infrastructureUsedByManySources");
                    reasons.add("Infrastructure node is used by multiple source nodes");
                }
                if (hostedUrlCount > 1) {
                    score += profileSupport.properties().scoreWeight("infrastructureHostsManyResources");
                    reasons.add("Infrastructure node hosts multiple resource nodes");
                }
            }

            score = Math.min(score, 100);
            String verdict = verdictFromScore(score);

            Map<String, Object> evidence = new HashMap<>();
            evidence.put("reasons", reasons);
            evidence.put("rawType", type);
            evidence.put("profile", profileSupport.properties().getName());

            out.add(new RiskScoreItem<>(id, type, score, verdict, evidence));
        }

        return out;
    }

    private boolean isSource(String type) {
        return profileSupport.isNodeType(type, "source");
    }

    private boolean isResource(String type) {
        return profileSupport.isNodeType(type, "resource");
    }

    private boolean isInfrastructure(String type) {
        return profileSupport.isNodeType(type, "infrastructure");
    }

    private String verdictFromScore(int score) {
        if (score == 0) {
            return "SAFE";
        }
        if (score >= profileSupport.properties().threshold("fraud", 60)) {
            return "FRAUD";
        }
        return "SUSPICIOUS";
    }
}
