package com.apexobserve.queryservice.controller;

import com.apexobserve.queryservice.repository.DependencyRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/dependencies")
public class DependencyController {
    
    private final DependencyRepository dependencyRepository;

    public DependencyController(DependencyRepository dependencyRepository) {
        this.dependencyRepository = dependencyRepository;
    }

    @GetMapping({"", "/graph"})
    public Map<String, Object> getDependencyGraph(@RequestHeader(value = "X-Tenant-ID", defaultValue = "default") String tenantId) {
        List<Object[]> deps = dependencyRepository.findServiceDependencies(tenantId);
        
        List<Map<String, Object>> edges = new ArrayList<>();
        for (Object[] row : deps) {
            Map<String, Object> edge = new HashMap<>();
            edge.put("source", row[0]);
            edge.put("target", row[1]);
            edge.put("callCount", ((Number) row[2]).longValue());
            edges.add(edge);
        }
        
        Map<String, Object> graph = new HashMap<>();
        graph.put("edges", edges);
        return graph;
    }
}
