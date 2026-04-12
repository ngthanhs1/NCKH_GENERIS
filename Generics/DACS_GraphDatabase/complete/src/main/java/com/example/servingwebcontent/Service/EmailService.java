package com.example.servingwebcontent.Service;

import com.example.servingwebcontent.model.Email;
import com.example.servingwebcontent.model.Url;
import com.example.servingwebcontent.Repository.EmailRepository;
import com.example.servingwebcontent.Repository.UrlRepository;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service quản lý các thao tác liên quan đến Email.
 * 
 * Chức năng chính:
 * - Tạo Email node trong Neo4j
 * - Tạo Email kèm theo URL và thiết lập relationship
 * - Kiểm tra kết nối Neo4j database
 * 
 * @author DACS Graph Database Team
 */
@Service
public class EmailService {

    private final EmailRepository emailRepository;
    private final UrlRepository urlRepository;
    private final Neo4jClient neo4jClient;

    public EmailService(
            EmailRepository emailRepository,
            UrlRepository urlRepository,
            Neo4jClient neo4jClient
    ) {
        this.emailRepository = emailRepository;
        this.urlRepository = urlRepository;
        this.neo4jClient = neo4jClient;
    }

    /**
     * Tạo Email mới kèm theo URL và thiết lập relationship CONTAINS_LINK.
     * 
     * @param emailValue Địa chỉ email
     * @param urlValue URL trong email
     * @return Email đã được tạo
     */
    public Email createEmailWithUrl(String emailValue, String urlValue) {
        Email email = getOrCreate(emailValue);
        Url url = urlRepository.findById("URL:" + urlValue)
            .orElseGet(() -> urlRepository.save(new Url(urlValue)));

        // Tạo relationship Email -[:CONTAINS_LINK]-> URL
        neo4jClient.query("""
            MATCH (e:Email {id: $emailId})
            MATCH (u:URL {id: $urlId})
            MERGE (e)-[:CONTAINS_LINK]->(u)
        """)
        .bind(email.getId()).to("emailId")
        .bind(url.getId()).to("urlId")
        .run();

        return email;
    }
    
    /**
     * Kiểm tra kết nối với Neo4j database.
     * 
     * @return Map chứa status của kết nối
     */
    public Map<String, Object> checkNeo4jConnection() {
        return neo4jClient
                .query("RETURN 'Neo4j OK' AS status")
                .fetch()
                .one()
                .orElse(Map.of("status", "Neo4j NOT CONNECTED"));
    }

    /**
     * Tạo Email đơn giản (chỉ có email, không có URL hay IP).
     * 
     * @param value Địa chỉ email
     * @return Email đã được tạo
     */
    public Email createEmail(String value) {
        return getOrCreate(value);
    }

    public Email getOrCreate(String value) {
        String id = "Email:" + value;
        return emailRepository.findById(id).orElseGet(() -> emailRepository.save(new Email(value)));
    }

    public Optional<Email> findByValue(String value) {
        return emailRepository.findByValue(value);
    }

    public List<Email> findAll() {
        return emailRepository.findAll();
    }

}
