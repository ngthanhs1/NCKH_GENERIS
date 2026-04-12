package com.example.servingwebcontent.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Model đại diện cho địa chỉ IP trong hệ thống phát hiện gian lận.
 * 
 * IPAddress thường là node trung tâm (hub) kết nối nhiều Email và URL.
 * Đây là yếu tố quan trọng để phát hiện các mạng lưới gian lận:
 * - Một IP gửi nhiều email khác nhau
 * - Một IP hosting nhiều URL khả nghi
 * - IP xuất hiện trong nhiều giao dịch đáng ngờ
 * 
 * @author DACS Graph Database Team
 */
@Node("IPAddress")
public class IPAddress {

    @Id
    private String id;           // Format: IPAddress:192.168.1.1

    @Property("value")
    private String value;        // Địa chỉ IP thực tế (IPv4 hoặc IPv6)

    private String status;       // Trạng thái: valid | suspicious | invalid
    private String riskLevel;    // Mức độ rủi ro: low | medium | high
    private int riskScore;       // Điểm rủi ro: 0-100
    private String verdict;      // Kết luận: AN TOÀN | ĐÁNG NGHI NGỜ | GIAN LẬN

    private List<String> indicators;  // Các chỉ số nghi ngờ gian lận

    private boolean deleted = false;  // Đánh dấu xóa mềm
    private LocalDateTime lastModified;  // Thời gian cập nhật cuối

    // ===== Constructors =====
    
    /**
     * Constructor mặc định cho Spring Data Neo4j
     */
    public IPAddress() {}
    
    /**
     * Tạo IPAddress mới với giá trị mặc định
     * 
     * @param value Địa chỉ IP (IPv4 hoặc IPv6)
     */
    public IPAddress(String value) {
        this.id = "IPAddress:" + value;
        this.value = value;
        this.status = "unknown";
        this.riskLevel = "low";
        this.riskScore = 0;
        this.verdict = "UNKNOWN";
        this.lastModified = LocalDateTime.now();
    }

    // ===== Getters & Setters =====
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getRiskLevel() {
        return riskLevel;
    }
    
    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
    
    public int getRiskScore() {
        return riskScore;
    }
    
    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }
    
    public String getVerdict() {
        return verdict;
    }
    
    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }
    
    public List<String> getIndicators() {
        return indicators;
    }
    
    public void setIndicators(List<String> indicators) {
        this.indicators = indicators;
    }
    
    public boolean isDeleted() {
        return deleted;
    }
    
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
}
