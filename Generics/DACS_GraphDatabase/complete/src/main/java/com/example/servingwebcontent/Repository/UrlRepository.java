package com.example.servingwebcontent.Repository;

import com.example.servingwebcontent.model.Url;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface UrlRepository extends Neo4jRepository<Url, String> {

	Optional<Url> findByValue(String value);
}
