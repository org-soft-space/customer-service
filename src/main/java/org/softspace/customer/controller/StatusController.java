package org.softspace.customer.controller;

import org.softspace.customer.dto.StatusResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1")
public class StatusController {

    private final String serviceName;

    public StatusController(
            @Value("${spring.application.name}") String serviceName
    ) {
        this.serviceName = serviceName;
    }

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getStatus() {
        return ResponseEntity.ok(new StatusResponse(serviceName, "UP"));
    }
}
