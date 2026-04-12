package com.example.servingwebcontent.Controller;

import com.example.servingwebcontent.Service.GraphQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST Controller quản lý các API liên quan đến Graph Database.
 * 
 * Controller này cung cấp các endpoints cho:
 * - Kiểm tra health và thống kê graph
 * - Tạo dữ liệu và mối quan hệ trong graph
 * - Phân tích rủi ro cho Email, URL, IPAddress
 * - Lấy toàn bộ graph data cho visualization
 * 
 * Đây là điểm tiếp xúc chính giữa frontend và backend
 * trong hệ thống phát hiện gian lận sử dụng Graph Database.
 * 
 * @author DACS Graph Database Team
 */
@RestController
@RequestMapping("/api/graph")
public class GraphController {

    private final GraphQueryService graphService;

    public GraphController(GraphQueryService graphService) {
        this.graphService = graphService;
    }

    /**
     * Kiểm tra kết nối Neo4j và thống kê số lượng nodes/relationships.
     * 
     * @return Map chứa status và counts của Email, URL, IPAddress, relationships
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return graphService.health();
    }

    /**
     * Phân tích rủi ro cho tất cả các IPAddress.
     * 
     * Trả về danh sách IP với:
     * - Số email gửi từ IP
     * - Số URL host trên IP
     * - Risk score và verdict
     * - Các indicators nghi ngờ
     * 
     * @return Danh sách IP với thông tin phân tích rủi ro
     */
    @GetMapping("/analysis/ip-risk")
    public List<Map<String, Object>> ipRisk() {
        return graphService.analyzeIPRisk();
    }

    /**
     * Phân tích rủi ro cho tất cả các URL.
     * 
     * Trả về danh sách URL với:
     * - Số email chứa URL
     * - Risk score và verdict
     * - Các indicators (phishing campaign detection)
     * 
     * @return Danh sách URL với thông tin phân tích rủi ro
     */
    @GetMapping("/analysis/url-risk")
    public List<Map<String, Object>> urlRisk() {
        return graphService.analyzeUrlRisk();
    }

    /**
     * Phân tích rủi ro cho tất cả các Email.
     * 
     * Trả về danh sách Email với:
     * - Số URL trong email
     * - Số IP liên quan
     * - Risk score và verdict
     * - Các indicators nghi ngờ
     * 
     * @return Danh sách Email với thông tin phân tích rủi ro
     */
    @GetMapping("/analysis/email-risk")
    public List<Map<String, Object>> emailRisk() {
        return graphService.analyzeEmailRisk();
    }

    /**
     * Lấy toàn bộ graph data để hiển thị visualization.
     * 
     * Trả về cấu trúc:
     * - nodes: Danh sách tất cả nodes (Email, URL, IPAddress)
     * - links: Danh sách tất cả relationships
     * 
     * Data được format sẵn cho D3.js force-directed graph.
     * 
     * @return Map chứa "nodes" và "links"
     */
    @GetMapping("/full")
    public Map<String, Object> fullGraph() {
        return graphService.getFullGraph();
    }

    /**
     * Chạy phân tích rủi ro toàn bộ graph.
     * 
     * Áp dụng thuật toán chấm điểm:
     * - Email có URL: +30
     * - Email có IP: +20
     * - IP được dùng bởi nhiều email: +30
     * - URL xuất hiện trong nhiều email: +30
     * 
     * Verdict:
     * - UNKNOWN: chưa đủ triad (tam giác Email-URL-IP)
     * - SAFE: score = 0 (khi đã đủ triad)
     * - SUSPICIOUS: 0 < score < 60
     * - FRAUD: score >= 60
     * 
     * @return Danh sách kết quả phân tích với điểm và verdict
     */
    @GetMapping("/analyze")
    public List<Map<String, Object>> analyze() {
        return graphService.analyzeRisk();
    }

    /**
     * Tạo mối quan hệ Email-URL-IP trong graph (GET method).
     * 
     * Tạo 3 nodes và 3 relationships:
     * - Email -[:CONTAINS_LINK]-> URL
     * - Email -[:SENT_FROM]-> IPAddress
     * - URL -[:HOSTED_BY]-> IPAddress
     * 
     * @param email Địa chỉ email
     * @param url URL trong email
     * @param ip Địa chỉ IP
     * @return Message xác nhận
     */
    @GetMapping("/link")
    public String linkGet(
            @RequestParam String email,
            @RequestParam String url,
            @RequestParam String ip
    ) {
        graphService.linkEmailUrlIp(email, url, ip);
        return "Graph linked OK: " + email + " → " + url + " → " + ip;
    }

    /**
     * Tạo mối quan hệ Email-URL-IP trong graph (POST method).
     * 
     * Giống với GET /link, nhưng dùng POST method phù hợp hơn
     * cho thao tác tạo dữ liệu theo REST convention.
     * 
     * @param email Địa chỉ email
     * @param url URL trong email
     * @param ip Địa chỉ IP
     * @return Message xác nhận
     */
    @PostMapping("/link")
    public String link(
            @RequestParam String email,
            @RequestParam String url,
            @RequestParam String ip
    ) {
        graphService.linkEmailUrlIp(email, url, ip);
        return "Graph linked OK: " + email + " → " + url + " → " + ip;
    }

    /**
     * Tạo node/relationship tùy theo dữ liệu được nhập (không bắt buộc đủ cả 3).
     *
     * - Nhập 1 trong 3: tạo node tương ứng.
     * - Nhập 2 trong 3: tạo node + relationship tương ứng.
     * - Nhập đủ 3: tạo full chain Email → URL → IP.
     */
    @GetMapping("/link-partial")
    public Map<String, Object> linkPartial(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String ip
    ) {
        return graphService.linkPartial(email, url, ip);
    }

    /**
     * Xóa một node theo id (DETACH DELETE) để xóa kèm các relationships.
     *
     * Ví dụ: DELETE /api/graph/node?id=Email:scam@test.com
     */
    @DeleteMapping("/node")
    public Map<String, Object> deleteNode(@RequestParam String id) {
        return graphService.deleteNodeById(id);
    }

    /**
     * Lưu bố cục (layout) của graph sau khi kéo thả trên UI.
     *
     * Body: JSON array [{id: "Email:...", x: 123.4, y: 567.8}, ...]
     */
    @PostMapping("/layout/save")
    public Map<String, Object> saveLayout(@RequestBody(required = false) List<Map<String, Object>> nodes) {
        return graphService.saveLayout(nodes == null ? new ArrayList<>() : nodes);
    }
}
