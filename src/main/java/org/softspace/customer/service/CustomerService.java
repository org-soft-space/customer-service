package org.softspace.customer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openapitools.jackson.nullable.JsonNullable;
import org.softspace.customer.dto.customer.request.CreateCustomerRequest;
import org.softspace.customer.dto.customer.request.UpdateCustomerRequest;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.dto.customer.response.SetCustomersResponse;
import org.softspace.customer.entity.CustomerEntity;
import org.softspace.customer.enums.CustomerType;
import org.softspace.customer.exception.CustomerNotFoundException;
import org.softspace.customer.exception.ValidationException;
import org.softspace.customer.repository.CustomerRepository;
import org.softspace.customer.service.mapper.CustomerMapper;
import org.softspace.customer.service.validation.CustomerValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CustomerValidator customerValidator;

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
                    Map.of("GUID", customerGuid)
            );
        });
        return customerMapper.customerEntityToCustomerResponse(customer);
    }

    @Transactional(readOnly = true)
    public SetCustomersResponse getAllCustomers() {
        List<CustomerEntity> allCustomers = customerRepository.findAll();
        return customerMapper.customerEntityListToSetCustomerResponse(allCustomers);
    }

    @Transactional
    public CustomerResponse updateCustomer(UUID customerGuid, UpdateCustomerRequest updateCustomerRequest) {

        log.info("Update customer: {}", customerGuid);
        CustomerEntity customerEntity = customerRepository.findByGuid(customerGuid).orElseThrow(
                () -> new CustomerNotFoundException(
                        "Customer not found",
                        Map.of("GUID", customerGuid)
                )
        );
        customerValidator.validateUpdateCustomerRequest(
                updateCustomerRequest,
                customerEntity.getEmail(),
                customerEntity.getPhone()
        );
        Instant newUpdatedTime = Instant.now();
        applyChange(customerEntity, updateCustomerRequest, newUpdatedTime);
        return customerMapper.customerEntityToCustomerResponse(customerEntity);
    }

    private void applyChange(
            CustomerEntity customerEntity,
            UpdateCustomerRequest updateCustomerRequest,
            Instant newUpdatedTime
    ) {
        customerEntity.setUpdatedAt(newUpdatedTime);

        JsonNullable<String> name = updateCustomerRequest.name();
        if (name.isPresent()) {
            customerEntity.setName(name.get());
        }

        JsonNullable<String> surname = updateCustomerRequest.surname();
        if (surname.isPresent()) {
            customerEntity.setSurname(surname.get());
        }

        JsonNullable<String> middleName = updateCustomerRequest.middleName();
        if (middleName.isPresent()) {
            customerEntity.setMiddleName(middleName.get());
        }

        JsonNullable<String> email = updateCustomerRequest.email();
        if (email.isPresent()) {
            customerEntity.setEmail(email.get());
        }

        JsonNullable<String> phone = updateCustomerRequest.phone();
        if (phone.isPresent()) {
            customerEntity.setPhone(phone.get());
        }

        JsonNullable<CustomerType> customerType = updateCustomerRequest.customerType();
        if (customerType.isPresent()) {
            customerEntity.setCustomerType(customerType.get());
        }

        JsonNullable<UUID> userProfileGuid = updateCustomerRequest.userProfileGuid();
        if (userProfileGuid.isPresent()) {
            customerEntity.setUserProfileGuid(userProfileGuid.get());
        }

        JsonNullable<UUID> responsibleManagerGuid = updateCustomerRequest.responsibleManagerGuid();
        if (responsibleManagerGuid.isPresent()) {
            customerEntity.setResponsibleManagerGuid(responsibleManagerGuid.get());
        }
    }
}
