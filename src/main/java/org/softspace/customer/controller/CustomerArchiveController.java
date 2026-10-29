package org.softspace.customer.controller;


import lombok.RequiredArgsConstructor;
import org.softspace.customer.dto.customer.response.CustomerListResponse;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.service.CustomerArchiveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/api/admin/v1/customers/archive")
@RequiredArgsConstructor
public class CustomerArchiveController {

    private final CustomerArchiveService customerArchiveService;

    @GetMapping
    ResponseEntity<CustomerListResponse> getAllArchivedCustomers() {
        return ResponseEntity.ok(customerArchiveService.getAllArchivedCustomers());
    }

    @GetMapping("/{guid}")
    ResponseEntity<CustomerResponse> getArchivedCustomer(
            @PathVariable UUID guid
    ) {
        return ResponseEntity.ok(customerArchiveService.getArchivedCustomer(guid));
    }

    @PostMapping("/{guid}/restore")
    ResponseEntity<CustomerResponse> restoreArchivedCustomer(
            @PathVariable UUID guid
    ) {
        return ResponseEntity.ok(customerArchiveService.restoreArchivedCustomer(guid));
    }

    @DeleteMapping("/{guid}")
    ResponseEntity<CustomerResponse> deleteArchivedCustomer(
            @PathVariable UUID guid
    ) {
        customerArchiveService.deleteArchivedCustomer(guid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
