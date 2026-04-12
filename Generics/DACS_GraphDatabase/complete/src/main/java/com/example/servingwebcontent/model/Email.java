package com.example.servingwebcontent.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Model đại diện cho một địa chỉ Email trong hệ thống phát hiện gian lận.
 * 
 * Email được đánh giá rủi ro dựa trên các yếu tố:
 * - Số lượng URL chứa trong email
 * - Địa chỉ IP gửi email
 * - Liên kết với các đối tượng khả nghi khác trong graph
 * 
 * @author DACS Graph Database Team
 */
@Node("Email")
public class Email {

    @Id
    private String id;   // Format: Email:example@mail.com

    @Property("value")
    private String value;  // Địa chỉ email thực tế

    private String status;      // Trạng thái: valid | suspicious | invalid
    private String riskLevel;   // Mức độ rủi ro: low | medium | high
    private int riskScore;      // Điểm rủi ro: 0-100
    private String verdict;     // Kết luận: AN TOÀN | ĐÁNG NGHI | GIAN LẬN

    private List<String> indicators;  // Các chỉ số nghi ngờ gian lận

    private boolean deleted = false;  // Đánh dấu xóa mềm
    private LocalDateTime lastModified;  // Thời gian cập nhật cuối

    // ===== Constructors =====
    
    /**
     * Constructor mặc định cho Spring Data Neo4j
     */
    public Email() {}

    /**
     * Tạo Email mới với giá trị mặc định an toàn
     * 
     * @param value Địa chỉ email
     */
    public Email(String value) {
        this.id = "Email:" + value;
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
