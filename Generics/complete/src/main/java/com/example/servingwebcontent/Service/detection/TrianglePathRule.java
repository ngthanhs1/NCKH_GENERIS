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
 * Rule 4: Nhan dien chu trinh lien ket day du Email -> URL -> IP va Email -> IP.
 */
@Component
public class TrianglePathRule implements PatternRule<Map<String, Object>> {

    private final AnalysisProfileSupport profileSupport;

    public TrianglePathRule(AnalysisProfileSupport profileSupport) {
        this.profileSupport = profileSupport;
    }

    @Override
    public String code() {
        return "TRIANGLE_PATH";
    }

    @Override
    public List<PatternMatch<Map<String, Object>>> detect(
            GraphData<NodeDTO<Map<String, Object>>, EdgeDTO<Map<String, Object>>> graph) {
        Map<Object, String> nodeTypeById = new HashMap<>();
        for (NodeDTO<Map<String, Object>> node : graph.nodes()) {
            nodeTypeById.put(node.id(), node.type());
        }

        Map<Object, Set<Object>> emailToUrls = new HashMap<>();
        Map<Object, Set<Object>> emailToIps = new HashMap<>();
        Map<Object, Set<Object>> urlToIps = new HashMap<>();

        for (EdgeDTO<Map<String, Object>> edge : graph.edges()) {
            String fromType = nodeTypeById.get(edge.from());
            String toType = nodeTypeById.get(edge.to());

            if (profileSupport.isRelation(edge.relation(), "sourceToResource")
                    && profileSupport.isNodeType(fromType, "source")
                    && profileSupport.isNodeType(toType, "resource")) {
                emailToUrls.computeIfAbsent(edge.from(), ignored -> new HashSet<>()).add(edge.to());
            }

            if (profileSupport.isRelation(edge.relation(), "sourceToInfrastructure")
                    && profileSupport.isNodeType(fromType, "source")
                    && profileSupport.isNodeType(toType, "infrastructure")) {
                emailToIps.computeIfAbsent(edge.from(), ignored -> new HashSet<>()).add(edge.to());
            }

            if (profileSupport.isRelation(edge.relation(), "resourceToInfrastructure")
                    && profileSupport.isNodeType(fromType, "resource")
                    && profileSupport.isNodeType(toType, "infrastructure")) {
                urlToIps.computeIfAbsent(edge.from(), ignored -> new HashSet<>()).add(edge.to());
            }
        }

        List<PatternMatch<Map<String, Object>>> out = new ArrayList<>();
        for (Map.Entry<Object, Set<Object>> emailUrls : emailToUrls.entrySet()) {
            Object emailId = emailUrls.getKey();
            Set<Object> ipsFromEmail = emailToIps.getOrDefault(emailId, Set.of());
            if (ipsFromEmail.isEmpty()) {
                continue;
            }

            for (Object urlId : emailUrls.getValue()) {
                Set<Object> ipsFromUrl = urlToIps.getOrDefault(urlId, Set.of());
                for (Object ipId : ipsFromEmail) {
                    if (!ipsFromUrl.contains(ipId)) {
                        continue;
                    }

                    List<Object> relatedNodeIds = List.of(emailId, urlId, ipId);
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("email", emailId);
                    metadata.put("url", urlId);
                    metadata.put("ip", ipId);

                    out.add(new PatternMatch<>(
                            code(),
                            "MEDIUM",
                            "Phat hien day du tam giac lien ket Email-URL-IP.",
                            relatedNodeIds,
                            metadata
                    ));
                }
            }
        }

        return out;
    }
}
