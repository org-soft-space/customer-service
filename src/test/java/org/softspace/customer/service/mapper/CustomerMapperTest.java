package org.softspace.customer.service.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.entity.CustomerEntity;
import org.softspace.customer.enums.CustomerType;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomerMapperTest {

    private final CustomerMapper customerMapper = Mappers.getMapper(CustomerMapper.class);

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
}
