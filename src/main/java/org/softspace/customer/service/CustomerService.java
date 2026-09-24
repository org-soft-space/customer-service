package org.softspace.customer.service;

import lombok.RequiredArgsConstructor;
import org.softspace.customer.dto.customer.request.CreateCustomerRequest;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.dto.customer.response.SetCustomersResponse;
import org.softspace.customer.entity.CustomerEntity;
import org.softspace.customer.exception.CustomerNotFoundException;
import org.softspace.customer.exception.ValidationException;
import org.softspace.customer.repository.CustomerRepository;
import org.softspace.customer.service.mapper.CustomerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public CustomerResponse createNewCustomer(CreateCustomerRequest createCustomerRequest) {

        Instant newTime = Instant.now();
        UUID newCustomerGuid = UUID.randomUUID();

        if ((createCustomerRequest.email() == null || createCustomerRequest.email().isBlank())
                && (createCustomerRequest.phone() == null || createCustomerRequest.phone().isBlank())) {
            throw new ValidationException(
                    "Must have one or both field for connecting.",
                    Map.of("fieldName", "email or phone")
            );
        }

        CustomerEntity customerEntityMapped = customerMapper.createCustomerRequestToCustomerEntity(createCustomerRequest,
                newTime,
                newCustomerGuid);
        CustomerEntity newCustomer = customerRepository.save(customerEntityMapped);
        return customerMapper.customerEntityToCustomerResponse(newCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID customerGuid) {
        Optional<CustomerEntity> customerEntityOptional = customerRepository.findByGuid(customerGuid);

        CustomerEntity customer = customerEntityOptional.orElseThrow(() -> {
            return new CustomerNotFoundException(
                    "Customer not found",
                    Map.of("customer guid", customerGuid)
            );
        });
        return customerMapper.customerEntityToCustomerResponse(customer);
    }

    @Transactional(readOnly = true)
    public SetCustomersResponse getAllCustomers() {
        List<CustomerEntity> allCustomers = customerRepository.findAll();
        return customerMapper.customerEntityListToSetCustomerResponse(allCustomers);
    }
}
