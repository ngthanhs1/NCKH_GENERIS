package com.example.servingwebcontent.Repository;

import com.example.servingwebcontent.model.IPAddress;
import org.springframework.data.neo4j.repository.Neo4jRepository;

import java.util.Optional;

public interface IPAddressRepository extends Neo4jRepository<IPAddress, String> {

	Optional<IPAddress> findByValue(String value);
}
