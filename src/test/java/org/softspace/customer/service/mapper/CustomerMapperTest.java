package org.softspace.customer.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.softspace.customer.dto.customer.request.CreateCustomerRequest;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.entity.CustomerEntity;
import org.softspace.customer.enums.CustomerType;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomerMapperTest {

    private final CustomerMapper customerMapper = Mappers.getMapper(CustomerMapper.class);

    @DisplayName("Mapping CustomerEntity to CustomerResponse. Positive test")
    @Test
    void customerEntityToCustomerResponseTest() {
        // Given
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setGuid(UUID.randomUUID());
        customerEntity.setName("Name");
        customerEntity.setSurname("Surname");
        customerEntity.setMiddleName("MiddleName");
        customerEntity.setPhone("+7987654321");
        customerEntity.setEmail("client@softspace.org");
        customerEntity.setCustomerType(CustomerType.CUSTOMER);
        customerEntity.setResponsibleManagerGuid(UUID.randomUUID());
        customerEntity.setUserProfileGuid(UUID.randomUUID());
        customerEntity.setCreatedAt(Instant.now());
        customerEntity.setUpdatedAt(Instant.now());

        // Execute
        CustomerResponse response = customerMapper.customerEntityToCustomerResponse(customerEntity);

        // Then
        assertEquals(customerEntity.getGuid(), response.guid());
        assertEquals(customerEntity.getName(), response.name());
        assertEquals(customerEntity.getSurname(), response.surname());
        assertEquals(customerEntity.getMiddleName(), response.middleName());
        assertEquals(customerEntity.getPhone(), response.phone());
        assertEquals(customerEntity.getEmail(), response.email());
        assertEquals(customerEntity.getCustomerType(), response.customerType());
        assertEquals(customerEntity.getResponsibleManagerGuid(), response.responsibleManagerGuid());
        assertEquals(customerEntity.getUserProfileGuid(), response.userProfileGuid());
        assertEquals(customerEntity.getCreatedAt(), response.createdAt());
        assertEquals(customerEntity.getUpdatedAt(), response.updatedAt());
    }

    @DisplayName("Mapping CreateCustomerRequest to CustomerEntity. Positive test")
    @Test
    void createCustomerRequestToCustomerEntityTest() {
        // Given
        Instant newTime = Instant.now();
        UUID newGuid = UUID.randomUUID();
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                "Name",
                "Surname",
                "MiddleName",
                "client@softspace.org",
                "+7987654321",
                CustomerType.CUSTOMER,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        // Execute
        CustomerEntity customerEntityResult = customerMapper.createCustomerRequestToCustomerEntity(createCustomerRequest, newTime, newGuid);

        // Then
        assertEquals(newGuid, customerEntityResult.getGuid());
        assertEquals(createCustomerRequest.name(), customerEntityResult.getName());
        assertEquals(createCustomerRequest.surname(), customerEntityResult.getSurname());
        assertEquals(createCustomerRequest.middleName(), customerEntityResult.getMiddleName());
        assertEquals(createCustomerRequest.email(), customerEntityResult.getEmail());
        assertEquals(createCustomerRequest.phone(), customerEntityResult.getPhone());
        assertEquals(createCustomerRequest.customerType(), customerEntityResult.getCustomerType());
        assertEquals(createCustomerRequest.responsibleManagerGuid(), customerEntityResult.getResponsibleManagerGuid());
        assertEquals(createCustomerRequest.userProfileGuid(), customerEntityResult.getUserProfileGuid());
        assertEquals(newTime, customerEntityResult.getCreatedAt());
        assertEquals(newTime, customerEntityResult.getUpdatedAt());
    }
}
