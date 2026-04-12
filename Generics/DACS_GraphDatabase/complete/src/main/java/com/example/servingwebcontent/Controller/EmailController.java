package com.example.servingwebcontent.Controller;

import com.example.servingwebcontent.Service.GraphQueryService;
import com.example.servingwebcontent.model.Email;
import com.example.servingwebcontent.Service.EmailService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;
    private final GraphQueryService graphQueryService;

    public EmailController(EmailService emailService, GraphQueryService graphQueryService) {
        this.emailService = emailService;
        this.graphQueryService = graphQueryService;
    }

    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        return graphQueryService.health();
    }

    @PostMapping
    public Email create(@RequestParam String value) {
        return emailService.createEmail(value);
    }

    @GetMapping
    public Email getByValue(@RequestParam String value) {
        return emailService.findByValue(value).orElse(null);
    }

    @GetMapping("/all")
    public List<Email> all() {
        return emailService.findAll();
    }

    @GetMapping("/risk")
    public List<Map<String, Object>> risk() {
        return graphQueryService.analyzeEmailRisk();
    }

    @PostMapping("/with-url")
    public Email createWithUrl(@RequestParam String email, @RequestParam String url) {
        // Tạo Email + URL và relationship CONTAINS_LINK.
        // Lưu ý: chấm điểm chính (verdict/riskScore) vẫn cần đủ bộ 3 Email + URL + IP.
        graphQueryService.linkPartial(email, url, null);
        return emailService.getOrCreate(email);
    }

}
