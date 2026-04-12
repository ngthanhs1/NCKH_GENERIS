package com.example.servingwebcontent.Service;

import com.example.servingwebcontent.Repository.UrlRepository;
import com.example.servingwebcontent.model.Url;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UrlService {

	private final UrlRepository urlRepository;

	public UrlService(UrlRepository urlRepository) {
		this.urlRepository = urlRepository;
	}

	public Url getOrCreate(String value) {
		String id = "URL:" + value;
		return urlRepository.findById(id).orElseGet(() -> urlRepository.save(new Url(value)));
	}

	public Optional<Url> findByValue(String value) {
		return urlRepository.findByValue(value);
	}

	public List<Url> findAll() {
		return urlRepository.findAll();
	}
}
