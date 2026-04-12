package com.example.servingwebcontent.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Model đại diện cho URL trong hệ thống phát hiện gian lận.
 * 
 * URL thường xuất hiện trong email phishing hoặc các trang web lừa đảo.
 * Các dấu hiệu gian lận:
 * - URL xuất hiện trong nhiều email khác nhau
 * - URL hosted trên IP khả nghi
 * - Domain giả mạo các trang web chính thức
 * - URL rút gọn che giấu đích thực
 * 
 * @author DACS Graph Database Team
 */
@Node("URL")
public class Url {

    @Id
    private String id;     // Format: URL:https://example.com

    private String value;  // URL đầy đủ

    private String status;     // Trạng thái: valid | suspicious | invalid
    private String riskLevel;  // Mức độ rủi ro: low | medium | high
    private int riskScore;     // Điểm rủi ro: 0-100
    private String verdict;    // Kết luận: AN TOÀN | ĐÁNG NGHI | GIAN LẬN

    private List<String> indicators;  // Các chỉ số nghi ngờ gian lận

    private boolean deleted = false;  // Đánh dấu xóa mềm
    private LocalDateTime lastModified;  // Thời gian cập nhật cuối

    // ===== Constructors =====
    
    /**
     * Constructor mặc định cho Spring Data Neo4j
     */
    public Url() {}

    /**
     * Tạo URL mới với giá trị mặc định an toàn
     * 
     * @param value URL đầy đủ (bao gồm protocol)
     */
    public Url(String value) {
        this.id = "URL:" + value;
        this.value = value;
        this.status = "valid";
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
