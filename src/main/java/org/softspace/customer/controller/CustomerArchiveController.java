package org.softspace.customer.controller;


import lombok.RequiredArgsConstructor;
import org.softspace.customer.dto.customer.response.ArchivedCustomerListResponse;
import org.softspace.customer.dto.customer.response.ArchivedCustomerResponse;
import org.softspace.customer.service.CustomerArchiveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/v1/customers/archive")
@RequiredArgsConstructor
public class CustomerArchiveController {

    private final CustomerArchiveService customerArchiveService;

    @GetMapping
    ResponseEntity<ArchivedCustomerListResponse> getAllArchivedCustomers() {
        return ResponseEntity.ok(customerArchiveService.getAllArchivedCustomers());
    }

    @GetMapping("/{guid}")
    ResponseEntity<ArchivedCustomerResponse> getArchivedCustomer(
            @PathVariable UUID guid
    ) {
        return ResponseEntity.ok(customerArchiveService.getArchivedCustomer(guid));
    }

    @PostMapping("/{guid}/restore")
    ResponseEntity<ArchivedCustomerResponse> restoreArchivedCustomer(
            @PathVariable UUID guid
    ) {
        return ResponseEntity.ok(customerArchiveService.restoreArchivedCustomer(guid));
    }

    @DeleteMapping("/{guid}")
    ResponseEntity<Void> deleteArchivedCustomer(
            @PathVariable UUID guid
    ) {
        customerArchiveService.permanentlyDeleteArchivedCustomer(guid);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
