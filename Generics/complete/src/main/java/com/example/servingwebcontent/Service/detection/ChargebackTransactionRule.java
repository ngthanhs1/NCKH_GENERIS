package com.example.servingwebcontent.Service.detection;

import com.example.servingwebcontent.Model.dto.EdgeDTO;
import com.example.servingwebcontent.Model.dto.GraphData;
import com.example.servingwebcontent.Model.dto.NodeDTO;
import com.example.servingwebcontent.Model.dto.PatternMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rule 3: Nhan dien IP host nhieu URL (hosting cluster).
 */
@Component
public class ChargebackTransactionRule implements PatternRule<Map<String, Object>> {

    private final AnalysisProfileSupport profileSupport;

    public ChargebackTransactionRule(AnalysisProfileSupport profileSupport) {
        this.profileSupport = profileSupport;
    }

    @Override
    public String code() {
        return "HOSTING_CLUSTER";
    }

    @Override
    public List<PatternMatch<Map<String, Object>>> detect(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph) {
        Map<Object, String> nodeTypeById = new HashMap<>();
        for (NodeDTO<Map<String, Object>> node : graph.nodes()) {
            nodeTypeById.put(node.id(), node.type());
        }

        Map<Object, List<Object>> ipToUrls = new HashMap<>();
        for (EdgeDTO<Map<String, Object>> edge : graph.edges()) {
            if (!profileSupport.isRelation(edge.relation(), "resourceToInfrastructure")) {
                continue;
            }
            String fromType = nodeTypeById.get(edge.from());
            String toType = nodeTypeById.get(edge.to());
            if (profileSupport.isNodeType(fromType, "resource")
                    && profileSupport.isNodeType(toType, "infrastructure")) {
                ipToUrls.computeIfAbsent(edge.to(), ignored -> new ArrayList<>()).add(edge.from());
            }
        }

        List<PatternMatch<Map<String, Object>>> out = new ArrayList<>();
        for (Map.Entry<Object, List<Object>> entry : ipToUrls.entrySet()) {
            Object ipId = entry.getKey();
            List<Object> urls = entry.getValue();
            if (urls.size() < 2) {
                continue;
            }

            List<Object> relatedNodeIds = new ArrayList<>();
            relatedNodeIds.add(ipId);
            relatedNodeIds.addAll(urls);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("ipType", nodeTypeById.get(ipId));
            metadata.put("urlCount", urls.size());
            metadata.put("urls", urls);

            out.add(new PatternMatch<>(
                    code(),
                    "MEDIUM",
                    "Mot IP host nhieu URL (co the la cum hosting nghi ngo).",
                    relatedNodeIds,
                    metadata
            ));
        }

        return out;
    }
}
