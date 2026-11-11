package com.example.medical.controller;

import com.example.medical.dto.EnvironmentReport;
import com.example.medical.service.EnvironmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EnvironmentController {
    private final EnvironmentService service;

    public EnvironmentController(EnvironmentService service) {
        this.service = service;
    }

    @GetMapping("/api/environment")
    public ResponseEntity<EnvironmentReport> inspect() {
        EnvironmentReport report = service.inspect();
        return ResponseEntity.status("connected".equals(report.databaseStatus()) ? 200 : 503).body(report);
    }
}
