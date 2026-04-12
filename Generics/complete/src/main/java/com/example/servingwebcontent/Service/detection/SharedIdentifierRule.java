package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import com.example.servingwebcontent.Model.dto.PatternMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rule 1: Nhan dien URL duoc nhieu Email cung tro toi.
 */
@Component
public class SharedIdentifierRule implements PatternRule<Map<String, Object>> {

    private final AnalysisProfileSupport profileSupport;

    public SharedIdentifierRule(AnalysisProfileSupport profileSupport) {
        this.profileSupport = profileSupport;
    }

    @Override
    public String code() {
        return "SHARED_URL";
    }

    @Override
    public List<PatternMatch<Map<String, Object>>> detect(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph) {
        Map<Object, String> nodeTypeById = new HashMap<>();
        for (NodeDTO<Map<String, Object>> node : graph.nodes()) {
            nodeTypeById.put(node.id(), node.type());
        }

        Map<Object, Set<Object>> urlToEmails = new HashMap<>();
        for (EdgeDTO<Map<String, Object>> edge : graph.edges()) {
            if (!profileSupport.isRelation(edge.relation(), "sourceToResource")) {
                continue;
            }

            String fromType = nodeTypeById.get(edge.from());
            String toType = nodeTypeById.get(edge.to());
            if (!profileSupport.isNodeType(fromType, "source")) {
                continue;
            }
            if (!profileSupport.isNodeType(toType, "resource")) {
                continue;
            }

            urlToEmails.computeIfAbsent(edge.to(), ignored -> new HashSet<>()).add(edge.from());
        }

        List<PatternMatch<Map<String, Object>>> out = new ArrayList<>();
        for (Map.Entry<Object, Set<Object>> entry : urlToEmails.entrySet()) {
            Object urlId = entry.getKey();
            Set<Object> emails = entry.getValue();
            if (emails.size() < 2) {
                continue;
            }

            List<Object> relatedNodeIds = new ArrayList<>();
            relatedNodeIds.add(urlId);
            relatedNodeIds.addAll(emails);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("urlType", nodeTypeById.get(urlId));
            metadata.put("emailCount", emails.size());
            metadata.put("emails", new ArrayList<>(emails));

            out.add(new PatternMatch<>(
                    code(),
                    "HIGH",
                    "Nhieu Email cung tro toi mot URL (dau hieu campaign).",
                    relatedNodeIds,
                    metadata
            ));
        }

        return out;
    }
}
