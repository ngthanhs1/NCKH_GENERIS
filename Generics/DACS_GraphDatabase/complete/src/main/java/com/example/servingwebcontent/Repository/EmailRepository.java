package com.example.servingwebcontent.Repository;

import com.example.servingwebcontent.model.Email;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface EmailRepository extends Neo4jRepository<Email, String> {

	Optional<Email> findByValue(String value);

}
