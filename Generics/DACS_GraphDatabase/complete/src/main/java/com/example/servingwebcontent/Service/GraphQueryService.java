package com.example.servingwebcontent.Service;

import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service quản lý các truy vấn graph và phân tích phát hiện gian lận.
 * 
 * Service này cung cấp các chức năng chính:
 * - Kiểm tra kết nối và health check với Neo4j database
 * - Tạo và quản lý các mối quan hệ giữa Email, URL và IPAddress
 * - Phân tích rủi ro và tính điểm cho từng node trong graph
 * - Truy vấn toàn bộ graph để hiển thị visualization
 * 
 * Thuật toán phát hiện gian lận:
 * - Đánh giá dựa trên số lượng kết nối (degree centrality)
 * - Phát hiện các pattern đáng ngờ (shared IP/URL)
 * - Tính điểm rủi ro tổng hợp từ nhiều yếu tố
 * 
 * @author DACS Graph Database Team
 */
@Service
public class GraphQueryService {

    private final Neo4jClient neo4jClient;

    public GraphQueryService(Neo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    /**
     * Kiểm tra sức khỏe kết nối với Neo4j database.
     * 
     * Thống kê:
     * - Số lượng Email nodes
     * - Số lượng URL nodes
     * - Số lượng IPAddress nodes
     * - Tổng số relationships trong graph
     * 
     * @return Map chứa status và counts của các nodes/relationships
     */
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = neo4jClient.query("""
                MATCH (e:Email)
                WITH count(e) AS emailCount
                MATCH (u:URL)
                WITH emailCount, count(u) AS urlCount
                MATCH (i:IPAddress)
                WITH emailCount, urlCount, count(i) AS ipCount
                MATCH ()-[r]->()
                RETURN 1 AS ok, emailCount, urlCount, ipCount, count(r) AS relationshipCount
                """)
                .fetch()
                .one()
                .orElse(Map.of(
                    "ok", 0,
                    "emailCount", 0,
                    "urlCount", 0,
                    "ipCount", 0,
                    "relationshipCount", 0
                ));

            long emailCount = ((Number) result.getOrDefault("emailCount", 0)).longValue();
            long urlCount = ((Number) result.getOrDefault("urlCount", 0)).longValue();
            long ipCount = ((Number) result.getOrDefault("ipCount", 0)).longValue();
            long relCount = ((Number) result.getOrDefault("relationshipCount", 0)).longValue();

            response.put("status", "connected");
            response.put("ping", Map.of("ok", result.getOrDefault("ok", 1)));
            response.put("counts", Map.of(
                    "Email", emailCount,
                    "URL", urlCount,
                    "IPAddress", ipCount,
                    "relationships", relCount
            ));
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
        }
        return response;
    }

    /**
     * Tạo mối quan hệ giữa Email, URL và IPAddress.
     * 
     * Relationships được tạo:
     * - Email -[:CONTAINS_LINK]-> URL (email chứa liên kết)
     * - Email -[:SENT_FROM]-> IPAddress (email được gửi từ IP)
     * - URL -[:HOSTED_BY]-> IPAddress (URL được host trên IP)
     * 
     * @param email Địa chỉ email
     * @param url URL trong email
     * @param ip Địa chỉ IP gửi email
     */
    public void linkEmailUrlIp(String email, String url, String ip) {
        neo4jClient.query("""
            MERGE (e:Email {id: 'Email:' + $email, value: $email})
            MERGE (u:URL {id: 'URL:' + $url, value: $url})
            MERGE (i:IPAddress {id: 'IPAddress:' + $ip, value: $ip})

            MERGE (e)-[:CONTAINS_LINK]->(u)
            MERGE (e)-[:SENT_FROM]->(i)
            MERGE (u)-[:HOSTED_BY]->(i)
        """)
        .bind(email).to("email")
        .bind(url).to("url")
        .bind(ip).to("ip")
        .run();
    }

    /**
     * Tạo node/relationship theo dữ liệu người dùng nhập (không bắt buộc đủ cả 3).
     *
     * Quy tắc:
     * - Có Email -> tạo/merge node Email
     * - Có URL -> tạo/merge node URL
     * - Có IP -> tạo/merge node IPAddress
     * - Có (Email + URL) -> tạo relationship Email -[:CONTAINS_LINK]-> URL
     * - Có (Email + IP) -> tạo relationship Email -[:SENT_FROM]-> IPAddress
     * - Có (URL + IP) -> tạo relationship URL -[:HOSTED_BY]-> IPAddress
     *
     * @param email email (có thể null/blank)
     * @param url url (có thể null/blank)
     * @param ip ip (có thể null/blank)
     * @return Map tóm tắt những gì đã tạo (nodes/relationships)
     */
    public Map<String, Object> linkPartial(String email, String url, String ip) {
        String safeEmail = email == null ? null : email.trim();
        String safeUrl = url == null ? null : url.trim();
        String safeIp = ip == null ? null : ip.trim();

        boolean hasEmail = safeEmail != null && !safeEmail.isBlank();
        boolean hasUrl = safeUrl != null && !safeUrl.isBlank();
        boolean hasIp = safeIp != null && !safeIp.isBlank();

        if (!hasEmail && !hasUrl && !hasIp) {
            return Map.of(
                    "status", "no_input",
                    "message", "Nhập ít nhất 1 trong 3: email/url/ip"
            );
        }

        if (hasEmail && hasUrl && hasIp) {
            linkEmailUrlIp(safeEmail, safeUrl, safeIp);
            return Map.of(
                "status", "ok",
                "nodes", List.of("Email", "URL", "IPAddress"),
                "relationships", List.of("CONTAINS_LINK", "SENT_FROM", "HOSTED_BY")
            );
        }

        List<String> nodes = new ArrayList<>();
        List<String> relationships = new ArrayList<>();

        if (hasEmail) nodes.add("Email");
        if (hasUrl) nodes.add("URL");
        if (hasIp) nodes.add("IPAddress");

        if (hasEmail && hasUrl) relationships.add("CONTAINS_LINK");
        if (hasEmail && hasIp) relationships.add("SENT_FROM");
        if (hasUrl && hasIp) relationships.add("HOSTED_BY");

        neo4jClient.query("""
            WITH 1 AS _
            FOREACH (ignore IN CASE WHEN $hasEmail THEN [1] ELSE [] END |
                MERGE (:Email {id: 'Email:' + $email, value: $email})
            )
            FOREACH (ignore IN CASE WHEN $hasUrl THEN [1] ELSE [] END |
                MERGE (:URL {id: 'URL:' + $url, value: $url})
            )
            FOREACH (ignore IN CASE WHEN $hasIp THEN [1] ELSE [] END |
                MERGE (:IPAddress {id: 'IPAddress:' + $ip, value: $ip})
            )

            FOREACH (ignore IN CASE WHEN ($hasEmail AND $hasUrl) THEN [1] ELSE [] END |
                MATCH (e:Email {id: 'Email:' + $email})
                MATCH (u:URL {id: 'URL:' + $url})
                MERGE (e)-[:CONTAINS_LINK]->(u)
            )
            FOREACH (ignore IN CASE WHEN ($hasEmail AND $hasIp) THEN [1] ELSE [] END |
                MATCH (e:Email {id: 'Email:' + $email})
                MATCH (i:IPAddress {id: 'IPAddress:' + $ip})
                MERGE (e)-[:SENT_FROM]->(i)
            )
            FOREACH (ignore IN CASE WHEN ($hasUrl AND $hasIp) THEN [1] ELSE [] END |
                MATCH (u:URL {id: 'URL:' + $url})
                MATCH (i:IPAddress {id: 'IPAddress:' + $ip})
                MERGE (u)-[:HOSTED_BY]->(i)
            )
        """)
                .bind(hasEmail).to("hasEmail")
                .bind(hasUrl).to("hasUrl")
                .bind(hasIp).to("hasIp")
                .bind(hasEmail ? safeEmail : "").to("email")
                .bind(hasUrl ? safeUrl : "").to("url")
                .bind(hasIp ? safeIp : "").to("ip")
                .run();

        return Map.of(
                "status", "ok_partial",
                "message", "Đã tạo node/relationship theo dữ liệu nhập. Lưu ý: chấm điểm chính (verdict/riskScore) chỉ rõ khi node tham gia đủ bộ 3 Email+URL+IP.",
                "nodes", nodes,
                "relationships", relationships
        );
    }

    /**
     * Phân tích và tính điểm rủi ro cho tất cả các nodes trong graph.
     * 
     * Thuật toán chấm điểm Email:
     * - +30 điểm: Nếu email chứa link
     * - +20 điểm: Nếu email có IP gửi
     * - +30 điểm: Nếu IP được dùng bởi nhiều email (shared IP - dấu hiệu spam/phishing)
     * - +30 điểm: Nếu URL xuất hiện trong nhiều email (shared URL - dấu hiệu phishing campaign)
     * 
      * Điều kiện ngữ cảnh (triad): chỉ chấm điểm khi Email tham gia đủ bộ 3 Email-URL-IP (tam giác).
      * Nếu chưa đủ triad: score = 0 và verdict = UNKNOWN.
      *
      * Phân loại verdict (đúng theo code):
      * - UNKNOWN: chưa đủ triad
      * - FRAUD (GIAN LẬN): score >= 60
      * - SUSPICIOUS (ĐÁNG NGHI): 0 < score < 60
      * - SAFE (AN TOÀN): score = 0
     * 
     * @return Danh sách kết quả phân tích email với điểm rủi ro và verdict
     */
        public List<Map<String, Object>> analyzeRisk() {
                // 1) Phân tích Email: tính riskScore và verdict dựa trên các mối quan hệ
                List<Map<String, Object>> emailResults = new ArrayList<>(neo4jClient.query("""
                        MATCH (e:Email)
                        OPTIONAL MATCH (e)-[:CONTAINS_LINK]->(u:URL)
                        OPTIONAL MATCH (e)-[:SENT_FROM]->(i:IPAddress)
                        WITH e, collect(DISTINCT u) AS urls, collect(DISTINCT i) AS ips
                        WITH e, urls, ips,
                            EXISTS {
                                MATCH (e)-[:CONTAINS_LINK]->(u2:URL)-[:HOSTED_BY]->(i2:IPAddress)
                                MATCH (e)-[:SENT_FROM]->(i2)
                            } AS hasTriad
                        WITH e, urls, ips, hasTriad,
                            CASE WHEN hasTriad AND size(urls) > 0 THEN 30 ELSE 0 END AS scoreHasLink,
                            CASE WHEN hasTriad AND size(ips) > 0 THEN 20 ELSE 0 END AS scoreHasIp,
                            CASE WHEN hasTriad AND any(ip IN ips WHERE COUNT { (ip)<-[:SENT_FROM]-(:Email) } > 1) THEN 30 ELSE 0 END AS scoreIpShared,
                            CASE WHEN hasTriad AND any(url IN urls WHERE COUNT { (url)<-[:CONTAINS_LINK]-(:Email) } > 1) THEN 30 ELSE 0 END AS scoreUrlShared
                        WITH e, hasTriad, (scoreHasLink + scoreHasIp + scoreIpShared + scoreUrlShared) AS scoreRaw
                        WITH e, hasTriad,
                             CASE WHEN scoreRaw > 100 THEN 100 ELSE scoreRaw END AS score
                        SET e.riskScore = score
                        SET e.verdict =
                            CASE
                                WHEN NOT hasTriad THEN 'UNKNOWN'
                                WHEN score >= 60 THEN 'FRAUD'
                                WHEN score > 0 THEN 'SUSPICIOUS'
                                ELSE 'SAFE'
                            END
                        RETURN e.id AS email, e.riskScore AS score, e.verdict AS verdict, hasTriad AS hasTriad
                        ORDER BY score DESC, email ASC
                """)
                .fetch()
                .all());

                // 2) Phân tích IPAddress: IP được nhiều email sử dụng = đáng ngờ
                neo4jClient.query("""
                        MATCH (i:IPAddress)
                        OPTIONAL MATCH (i)<-[:SENT_FROM]-(e:Email)
                        WITH i, count(DISTINCT e) AS emailCount
                        WITH i, emailCount,
                            EXISTS {
                                MATCH (e2:Email)-[:SENT_FROM]->(i)
                                MATCH (e2)-[:CONTAINS_LINK]->(u2:URL)-[:HOSTED_BY]->(i)
                            } AS hasTriad
                        WITH i, hasTriad,
                            CASE WHEN hasTriad AND emailCount > 1 THEN 30 ELSE 0 END AS score
                        SET i.riskScore = score
                        SET i.verdict =
                            CASE
                                WHEN NOT hasTriad THEN 'UNKNOWN'
                                WHEN score >= 60 THEN 'FRAUD'
                                WHEN score > 0 THEN 'SUSPICIOUS'
                                ELSE 'SAFE'
                            END
                        RETURN count(i) AS updated
                """)
                .fetch()
                .one();

                // 3) Phân tích URL: URL được nhiều email trỏ tới = phishing campaign
                neo4jClient.query("""
                        MATCH (u:URL)
                        OPTIONAL MATCH (u)<-[:CONTAINS_LINK]-(e:Email)
                        WITH u, count(DISTINCT e) AS emailCount
                        WITH u, emailCount,
                            EXISTS {
                                MATCH (e2:Email)-[:CONTAINS_LINK]->(u)
                                MATCH (e2)-[:SENT_FROM]->(i2:IPAddress)
                                MATCH (u)-[:HOSTED_BY]->(i2)
                            } AS hasTriad
                        WITH u, hasTriad,
                            CASE WHEN hasTriad AND emailCount > 1 THEN 30 ELSE 0 END AS score
                        SET u.riskScore = score
                        SET u.verdict =
                            CASE
                                WHEN NOT hasTriad THEN 'UNKNOWN'
                                WHEN score >= 60 THEN 'FRAUD'
                                WHEN score > 0 THEN 'SUSPICIOUS'
                                ELSE 'SAFE'
                            END
                        RETURN count(u) AS updated
                """)
                .fetch()
                .one();

                return emailResults;
        }

    /**
     * Lấy toàn bộ graph data để hiển thị visualization.
     * 
     * Trả về cấu trúc dữ liệu gồm:
     * - nodes: Danh sách các nodes (Email, URL, IPAddress) với thông tin rủi ro
     * - links: Danh sách các relationships giữa các nodes
     * 
     * @return Map chứa "nodes" và "links" cho D3.js visualization
     */
    public Map<String, Object> getFullGraph() {

    List<Map<String, Object>> nodes = new ArrayList<>();
    List<Map<String, Object>> links = new ArrayList<>();
    Set<String> seen = new HashSet<>();

    var rows = neo4jClient.query("""
        MATCH (a)-[r]->(b)
        RETURN a, r, b
    """).fetch().all();

    for (var row : rows) {

        Node a = (Node) row.get("a");
        Node b = (Node) row.get("b");
        Relationship r = (Relationship) row.get("r");

        String aId = a.get("id").asString();
        String bId = b.get("id").asString();

        // Thêm node A nếu chưa có
        if (seen.add(aId)) {
            Map<String, Object> n = new HashMap<>();
            n.put("id", aId);
            n.put("label", a.containsKey("value") ? a.get("value").asString() : aId);
            n.put("type", labelsAsList(a));
            n.put("riskScore", a.containsKey("riskScore") ? a.get("riskScore").asLong() : 0);
            n.put("verdict", a.containsKey("verdict") ? a.get("verdict").asString() : "UNKNOWN");
            if (a.containsKey("layoutX")) n.put("layoutX", a.get("layoutX").asDouble());
            if (a.containsKey("layoutY")) n.put("layoutY", a.get("layoutY").asDouble());
            nodes.add(n);
        }

        // Thêm node B nếu chưa có
        if (seen.add(bId)) {
            Map<String, Object> n = new HashMap<>();
            n.put("id", bId);
            n.put("label", b.containsKey("value") ? b.get("value").asString() : bId);
            n.put("type", labelsAsList(b));
            n.put("riskScore", b.containsKey("riskScore") ? b.get("riskScore").asLong() : 0);
            n.put("verdict", b.containsKey("verdict") ? b.get("verdict").asString() : "UNKNOWN");
            if (b.containsKey("layoutX")) n.put("layoutX", b.get("layoutX").asDouble());
            if (b.containsKey("layoutY")) n.put("layoutY", b.get("layoutY").asDouble());
            nodes.add(n);
        }

        // Thêm relationship
        Map<String, Object> l = new HashMap<>();
        l.put("source", aId);
        l.put("target", bId);
        l.put("type", r.type());
        links.add(l);
    }

    // Bổ sung các node đơn lẻ (không có relationship) để UI vẫn hiển thị
    var lonelyNodes = neo4jClient.query("""
        MATCH (n)
        RETURN n
    """).fetch().all();

    for (var row : lonelyNodes) {
        Node n0 = (Node) row.get("n");
        String nId = n0.get("id").asString();
        if (!seen.add(nId)) continue;

        Map<String, Object> n = new HashMap<>();
        n.put("id", nId);
        n.put("label", n0.containsKey("value") ? n0.get("value").asString() : nId);
        n.put("type", labelsAsList(n0));
        n.put("riskScore", n0.containsKey("riskScore") ? n0.get("riskScore").asLong() : 0);
        n.put("verdict", n0.containsKey("verdict") ? n0.get("verdict").asString() : "UNKNOWN");
        if (n0.containsKey("layoutX")) n.put("layoutX", n0.get("layoutX").asDouble());
        if (n0.containsKey("layoutY")) n.put("layoutY", n0.get("layoutY").asDouble());
        nodes.add(n);
    }

    Map<String, Object> result = new HashMap<>();
    result.put("nodes", nodes);
    result.put("links", links);
    return result;
}

    public Map<String, Object> saveLayout(List<Map<String, Object>> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            return Map.of(
                    "status", "no_input",
                    "saved", 0,
                    "message", "Không có node để lưu layout"
            );
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> n : nodes) {
            if (n == null) continue;
            Object idObj = n.get("id");
            if (idObj == null) continue;
            String id = String.valueOf(idObj).trim();
            if (id.isBlank()) continue;

            Double x = toDoubleOrNull(n.get("x"));
            Double y = toDoubleOrNull(n.get("y"));
            if (x == null || y == null) continue;

            rows.add(Map.of(
                    "id", id,
                    "x", x,
                    "y", y
            ));
        }

        if (rows.isEmpty()) {
            return Map.of(
                    "status", "no_valid_rows",
                    "saved", 0,
                    "message", "Không có tọa độ hợp lệ để lưu"
            );
        }

        Map<String, Object> result = neo4jClient.query("""
                UNWIND $rows AS row
                MATCH (n {id: row.id})
                SET n.layoutX = row.x,
                    n.layoutY = row.y
                RETURN count(n) AS saved
        """)
                .bind(rows).to("rows")
                .fetch()
                .one()
                .orElse(Map.of("saved", 0));

        long saved = ((Number) result.getOrDefault("saved", 0)).longValue();
        return Map.of(
                "status", "ok",
                "saved", saved
        );
    }

    private static Double toDoubleOrNull(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) {
            double d = n.doubleValue();
            return Double.isFinite(d) ? d : null;
        }
        try {
            double d = Double.parseDouble(String.valueOf(v));
            return Double.isFinite(d) ? d : null;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Xóa một node theo id (DETACH DELETE) để xóa kèm toàn bộ relationships.
     *
     * @param id định danh node theo format đang dùng, ví dụ: Email:<value>, URL:<value>, IPAddress:<value>
     * @return Map gồm status và số node đã xóa
     */
    public Map<String, Object> deleteNodeById(String id) {
        String safeId = id == null ? "" : id.trim();
        if (safeId.isBlank()) {
            return Map.of(
                    "status", "no_input",
                    "message", "Thiếu tham số id"
            );
        }

        Map<String, Object> row = neo4jClient.query("""
                        MATCH (n {id: $id})
                        WITH collect(n) AS nodes
                        FOREACH (x IN nodes | DETACH DELETE x)
                        RETURN size(nodes) AS deleted
                        """)
                .bind(safeId).to("id")
                .fetch()
                .one()
                .orElse(Map.of("deleted", 0));

        long deleted = ((Number) row.getOrDefault("deleted", 0)).longValue();
        return Map.of(
                "status", "ok",
                "deleted", deleted,
                "id", safeId
        );
    }


    /**
     * Chuyển đổi labels của Neo4j node thành List.
     * 
     * @param node Neo4j node
     * @return Danh sách labels
     */
    private static List<String> labelsAsList(Node node) {
        List<String> labels = new ArrayList<>();
        for (String label : node.labels()) {
            labels.add(label);
        }
        return labels;
    }

    /**
     * Phân tích rủi ro cho các IPAddress.
     * 
     * Indicators (chỉ số nghi ngờ):
     * - IP_DUNG_BOI_NHIEU_EMAIL: IP được sử dụng bởi >= 2 email khác nhau
     * - IP_HOST_NHIEU_URL: IP host >= 2 URL khác nhau
     * - MUC_DO_LIEN_KET_CAO: Tổng số kết nối >= 4
     * 
     * @return Danh sách IPAddress với thông tin phân tích rủi ro
     */
        public List<Map<String, Object>> analyzeIPRisk() {
                return new ArrayList<>(neo4jClient.query("""
                        MATCH (i:IPAddress)<-[:SENT_FROM]-(e:Email)
                        OPTIONAL MATCH (i)<-[:HOSTED_BY]-(u:URL)
                        WITH
                            i.value AS ip,
                            count(DISTINCT e) AS emailCount,
                            count(DISTINCT u) AS urlCount,
                            (count(DISTINCT e) + count(DISTINCT u)) AS riskScore
                        WITH ip, emailCount, urlCount, riskScore,
                            CASE
                                WHEN riskScore >= 4 THEN 'GIAN LẬN'
                                WHEN riskScore >= 2 THEN 'ĐÁNG NGHI NGỜ'
                                ELSE 'AN TOÀN'
                            END AS verdict,
                            CASE
                                WHEN riskScore >= 4 THEN 'HIGH'
                                WHEN riskScore >= 2 THEN 'MEDIUM'
                                ELSE 'LOW'
                            END AS riskLevel,
                            (CASE WHEN emailCount >= 2 THEN ['IP_DUNG_BOI_NHIEU_EMAIL'] ELSE [] END) +
                            (CASE WHEN urlCount >= 2 THEN ['IP_HOST_NHIEU_URL'] ELSE [] END) +
                            (CASE WHEN riskScore >= 4 THEN ['MUC_DO_LIEN_KET_CAO'] ELSE [] END) AS indicators
                        RETURN ip, emailCount, urlCount, riskScore, riskLevel, verdict, indicators
                        ORDER BY riskScore DESC, emailCount DESC, urlCount DESC
                """)
                                .fetch()
                                .all());
        }

    /**
     * Phân tích rủi ro cho các URL.
     * 
     * Indicators (chỉ số nghi ngờ):
     * - URL_DUOC_NHIEU_EMAIL_TRO_TOI: URL xuất hiện trong >= 2 email (phishing campaign)
     * - MUC_DO_LIEN_KET_CAO: Tổng số kết nối >= 4
     * 
     * @return Danh sách URL với thông tin phân tích rủi ro
     */
        public List<Map<String, Object>> analyzeUrlRisk() {
                return new ArrayList<>(neo4jClient.query("""
                        MATCH (u:URL)<-[:CONTAINS_LINK]-(e:Email)
                        WITH
                            u.value AS url,
                            count(DISTINCT e) AS emailCount,
                            count(DISTINCT e) AS riskScore
                        WITH url, emailCount, riskScore,
                            CASE
                                WHEN riskScore >= 4 THEN 'GIAN LẬN'
                                WHEN riskScore >= 2 THEN 'ĐÁNG NGHI NGỜ'
                                ELSE 'AN TOÀN'
                            END AS verdict,
                            CASE
                                WHEN riskScore >= 4 THEN 'HIGH'
                                WHEN riskScore >= 2 THEN 'MEDIUM'
                                ELSE 'LOW'
                            END AS riskLevel,
                            (CASE WHEN emailCount >= 2 THEN ['URL_DUOC_NHIEU_EMAIL_TRO_TOI'] ELSE [] END) +
                            (CASE WHEN riskScore >= 4 THEN ['MUC_DO_LIEN_KET_CAO'] ELSE [] END) AS indicators
                        RETURN url, emailCount, riskScore, riskLevel, verdict, indicators
                        ORDER BY riskScore DESC, emailCount DESC
                """)
                                .fetch()
                                .all());
        }

    /**
     * Phân tích rủi ro cho các Email.
     * 
     * Indicators (chỉ số nghi ngờ):
     * - EMAIL_TRO_TOI_NHIEU_URL: Email chứa >= 2 URL khác nhau
     * - EMAIL_DUNG_NHIEU_IP: Email được gửi từ >= 2 IP khác nhau
     * - MUC_DO_LIEN_KET_CAO: Tổng số kết nối >= 4
     * 
     * @return Danh sách Email với thông tin phân tích rủi ro
     */
        public List<Map<String, Object>> analyzeEmailRisk() {
                return new ArrayList<>(neo4jClient.query("""
                        MATCH (e:Email)
                        OPTIONAL MATCH (e)-[:CONTAINS_LINK]->(u:URL)
                        OPTIONAL MATCH (e)-[:SENT_FROM]->(i:IPAddress)
                        WITH
                            e.value AS email,
                            count(DISTINCT u) AS urlCount,
                            count(DISTINCT i) AS ipCount,
                            (count(DISTINCT u) + count(DISTINCT i)) AS riskScore
                        WITH email, urlCount, ipCount, riskScore,
                            CASE
                                WHEN riskScore >= 4 THEN 'GIAN LẬN'
                                WHEN riskScore >= 2 THEN 'ĐÁNG NGHI NGỜ'
                                ELSE 'AN TOÀN'
                            END AS verdict,
                            CASE
                                WHEN riskScore >= 4 THEN 'HIGH'
                                WHEN riskScore >= 2 THEN 'MEDIUM'
                                ELSE 'LOW'
                            END AS riskLevel,
                            (CASE WHEN urlCount >= 2 THEN ['EMAIL_TRO_TOI_NHIEU_URL'] ELSE [] END) +
                            (CASE WHEN ipCount >= 2 THEN ['EMAIL_DUNG_NHIEU_IP'] ELSE [] END) +
                            (CASE WHEN riskScore >= 4 THEN ['MUC_DO_LIEN_KET_CAO'] ELSE [] END) AS indicators
                        RETURN email, urlCount, ipCount, riskScore, riskLevel, verdict, indicators
                        ORDER BY riskScore DESC, urlCount DESC, ipCount DESC
                """)
                                .fetch()
                                .all());
        }
}
