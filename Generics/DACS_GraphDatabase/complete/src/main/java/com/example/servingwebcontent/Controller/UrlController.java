package com.example.servingwebcontent.Controller;

import com.example.servingwebcontent.Service.GraphQueryService;
import com.example.servingwebcontent.Service.UrlService;
import com.example.servingwebcontent.model.Url;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/url")
public class UrlController {

	private final UrlService urlService;
	private final GraphQueryService graphQueryService;

	public UrlController(UrlService urlService, GraphQueryService graphQueryService) {
		this.urlService = urlService;
		this.graphQueryService = graphQueryService;
	}

	@PostMapping
	public Url create(@RequestParam String value) {
		return urlService.getOrCreate(value);
	}

	@GetMapping("/health")
	public Map<String, Object> health() {
		return graphQueryService.health();
	}

	@GetMapping
	public Url getByValue(@RequestParam String value) {
		return urlService.findByValue(value).orElse(null);
	}

	@GetMapping("/all")
	public List<Url> all() {
		return urlService.findAll();
	}

	@GetMapping("/risk")
	public List<Map<String, Object>> risk() {
		return graphQueryService.analyzeUrlRisk();
	}
}
