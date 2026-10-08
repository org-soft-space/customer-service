package org.softspace.customer.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.softspace.customer.dto.customer.request.CreateCustomerRequest;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.dto.customer.response.CustomerListResponse;
import org.softspace.customer.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createNewCustomer(
            @RequestBody @Valid CreateCustomerRequest createCustomerRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.createNewCustomer(createCustomerRequest));
    }

    @GetMapping("/{guid}")
    public ResponseEntity<CustomerResponse> getCustomer(
            @PathVariable final UUID guid
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(customerService.getCustomer(guid));
    }

    @GetMapping
    public ResponseEntity<CustomerListResponse> getAllCustomers() {
        return ResponseEntity.status(HttpStatus.OK).body(customerService.getAllCustomers());
    }
}
