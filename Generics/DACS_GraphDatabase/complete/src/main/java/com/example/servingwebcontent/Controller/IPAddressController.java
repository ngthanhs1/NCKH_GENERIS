package com.example.servingwebcontent.Controller;

import com.example.servingwebcontent.Service.GraphQueryService;
import com.example.servingwebcontent.Service.IPAddressService;
import com.example.servingwebcontent.model.IPAddress;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ip")
public class IPAddressController {

    private final IPAddressService ipAddressService;
    private final GraphQueryService graphQueryService;

    public IPAddressController(IPAddressService ipAddressService, GraphQueryService graphQueryService) {
        this.ipAddressService = ipAddressService;
        this.graphQueryService = graphQueryService;
    }

    @PostMapping
    public IPAddress create(@RequestParam String value) {
        return ipAddressService.getOrCreate(value);
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return graphQueryService.health();
    }

    @GetMapping
    public IPAddress getByValue(@RequestParam String value) {
        return ipAddressService.findByValue(value).orElse(null);
    }

    @GetMapping("/all")
    public List<IPAddress> all() {
        return ipAddressService.findAll();
    }

    @GetMapping("/risk")
    public List<Map<String, Object>> risk() {
        return graphQueryService.analyzeIPRisk();
    }
}