package org.softspace.customer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.softspace.customer.dto.customer.response.SetCustomersResponse;
import org.softspace.customer.dtotest.DtoCustomerTestBuilder;
import org.softspace.customer.dto.customer.request.CreateCustomerRequest;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.entity.CustomerEntity;
import org.softspace.customer.enums.CustomerType;
import org.softspace.customer.repository.CustomerRepository;
import org.softspace.customer.service.mapper.CustomerMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    private CustomerEntity customerEntity;

    @BeforeEach
    void setUp() {
        customerEntity = new CustomerEntity(
                1L,
                DtoCustomerTestBuilder.GUID,
                DtoCustomerTestBuilder.NAME,
                DtoCustomerTestBuilder.SURNAME,
                DtoCustomerTestBuilder.MIDDLE_NAME,
                DtoCustomerTestBuilder.EMAIL,
                DtoCustomerTestBuilder.PHONE,
                DtoCustomerTestBuilder.CUSTOMER_TYPE,
                DtoCustomerTestBuilder.USER_PROFILE_GUID,
                DtoCustomerTestBuilder.RESPONSIBLE_MANAGER_GUID,
                DtoCustomerTestBuilder.TIME,
                DtoCustomerTestBuilder.TIME
        );

    }

    @DisplayName("Create new customer successfully test.")
    @Test
    void createCustomerSuccessfullyTest() {
        // Given
        CreateCustomerRequest createCustomerRequest = DtoCustomerTestBuilder.getCreateCustomerRequest();
        CustomerResponse customerResponse = DtoCustomerTestBuilder.getCustomerResponse();

        // When
        when(customerMapper.createCustomerRequestToCustomerEntity(eq(createCustomerRequest), any(Instant.class), any(UUID.class)))
                .thenReturn(customerEntity);
        when(customerRepository.save(customerEntity))
                .thenReturn(customerEntity);
        when(customerMapper.customerEntityToCustomerResponse(customerEntity))
                .thenReturn(customerResponse);

        // Execute
        CustomerResponse newCustomer = customerService.createNewCustomer(createCustomerRequest);

        // Then
        assertNotNull(newCustomer);
        assertEquals(customerResponse.guid(), newCustomer.guid());
        assertEquals(customerResponse.name(), newCustomer.name());
        assertEquals(customerResponse.surname(), newCustomer.surname());
        assertEquals(customerResponse.middleName(), newCustomer.middleName());
        assertEquals(customerResponse.email(), newCustomer.email());
        assertEquals(customerResponse.phone(), newCustomer.phone());
        assertEquals(customerResponse.customerType(), newCustomer.customerType());
        assertEquals(customerResponse.userProfileGuid(), newCustomer.userProfileGuid());
        assertEquals(customerResponse.responsibleManagerGuid(), newCustomer.responsibleManagerGuid());
        assertEquals(customerResponse.createdAt(), newCustomer.createdAt());
        assertEquals(customerResponse.updatedAt(), newCustomer.updatedAt());

        verify(customerMapper).createCustomerRequestToCustomerEntity(eq(createCustomerRequest), any(Instant.class), any(UUID.class));
        verify(customerRepository).save(any(CustomerEntity.class));
        verify(customerMapper).customerEntityToCustomerResponse(any(CustomerEntity.class));
    }

    @DisplayName("Create new customer with email contact only successfully test.")
    @Test
    void createCustomerWithEmailContactSuccessfullyTest() {
        // Given
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                DtoCustomerTestBuilder.NAME,
                null,
                null,
                DtoCustomerTestBuilder.EMAIL,
                null,
                CustomerType.LEAD,
                null,
                null
        );
        CustomerResponse customerResponse = new CustomerResponse(
                DtoCustomerTestBuilder.GUID,
                DtoCustomerTestBuilder.NAME,
                null,
                null,
                DtoCustomerTestBuilder.EMAIL,
                null,
                CustomerType.LEAD,
                null,
                null,
                DtoCustomerTestBuilder.TIME,
                DtoCustomerTestBuilder.TIME
        );

        // When
        when(customerMapper.createCustomerRequestToCustomerEntity(eq(createCustomerRequest), any(Instant.class), any(UUID.class)))
                .thenReturn(customerEntity);
        when(customerRepository.save(customerEntity))
                .thenReturn(customerEntity);
        when(customerMapper.customerEntityToCustomerResponse(customerEntity))
                .thenReturn(customerResponse);

        // Execute
        CustomerResponse newCustomer = customerService.createNewCustomer(createCustomerRequest);

        // Then
        assertNotNull(newCustomer);
        assertEquals(customerResponse.name(), newCustomer.name());
        assertEquals(customerResponse.email(), newCustomer.email());
        assertNull(newCustomer.phone());
        assertEquals(customerResponse.customerType(), newCustomer.customerType());
        assertEquals(customerResponse.createdAt(), newCustomer.createdAt());
        assertEquals(customerResponse.updatedAt(), newCustomer.updatedAt());

        verify(customerMapper).createCustomerRequestToCustomerEntity(eq(createCustomerRequest), any(Instant.class), any(UUID.class));
        verify(customerRepository).save(any(CustomerEntity.class));
        verify(customerMapper).customerEntityToCustomerResponse(any(CustomerEntity.class));
    }

    @DisplayName("Create new customer with phone contact only successfully test.")
    @Test
    void createCustomerWithPhoneContactSuccessfullyTest() {
        // Given
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                DtoCustomerTestBuilder.NAME,
                null,
                null,
                null,
                DtoCustomerTestBuilder.PHONE,
                CustomerType.LEAD,
                null,
                null
        );
        CustomerResponse customerResponse = new CustomerResponse(
                DtoCustomerTestBuilder.GUID,
                DtoCustomerTestBuilder.NAME,
                null,
                null,
                null,
                DtoCustomerTestBuilder.PHONE,
                CustomerType.LEAD,
                null,
                null,
                DtoCustomerTestBuilder.TIME,
                DtoCustomerTestBuilder.TIME
        );

        // When
        when(customerMapper.createCustomerRequestToCustomerEntity(eq(createCustomerRequest), any(Instant.class), any(UUID.class)))
                .thenReturn(customerEntity);
        when(customerRepository.save(customerEntity))
                .thenReturn(customerEntity);
        when(customerMapper.customerEntityToCustomerResponse(customerEntity))
                .thenReturn(customerResponse);

        // Execute
        CustomerResponse newCustomer = customerService.createNewCustomer(createCustomerRequest);

        // Then
        assertNotNull(newCustomer);
        assertEquals(customerResponse.name(), newCustomer.name());
        assertEquals(customerResponse.phone(), newCustomer.phone());
        assertNull(newCustomer.email());
        assertEquals(customerResponse.customerType(), newCustomer.customerType());
        assertEquals(customerResponse.createdAt(), newCustomer.createdAt());
        assertEquals(customerResponse.updatedAt(), newCustomer.updatedAt());

        verify(customerMapper).createCustomerRequestToCustomerEntity(eq(createCustomerRequest), any(Instant.class), any(UUID.class));
        verify(customerRepository).save(any(CustomerEntity.class));
        verify(customerMapper).customerEntityToCustomerResponse(any(CustomerEntity.class));
    }

    @DisplayName("Get customer successfully test.")
    @Test
    void getCustomerSuccessfullyTest() {

        // Given
        CustomerResponse customerResponse = DtoCustomerTestBuilder.getCustomerResponse();

        // When
        when(customerRepository.findByGuid(DtoCustomerTestBuilder.GUID))
                .thenReturn(Optional.of(customerEntity));
        when(customerMapper.customerEntityToCustomerResponse(customerEntity))
                .thenReturn(customerResponse);

        // Execute
        CustomerResponse customer = customerService.getCustomer(DtoCustomerTestBuilder.GUID);

        // then
        assertNotNull(customer);
        assertEquals(customerResponse.guid(), customer.guid());
        assertEquals(customerResponse.name(), customer.name());
        assertEquals(customerResponse.surname(), customer.surname());
        assertEquals(customerResponse.middleName(), customer.middleName());
        assertEquals(customerResponse.email(), customer.email());
        assertEquals(customerResponse.phone(), customer.phone());
        assertEquals(customerResponse.customerType(), customer.customerType());
        assertEquals(customerResponse.userProfileGuid(), customer.userProfileGuid());
        assertEquals(customerResponse.responsibleManagerGuid(), customer.responsibleManagerGuid());
        assertEquals(customerResponse.createdAt(), customer.createdAt());
        assertEquals(customerResponse.updatedAt(), customer.updatedAt());

        verify(customerRepository).findByGuid(any(UUID.class));
        verify(customerMapper).customerEntityToCustomerResponse(any(CustomerEntity.class));
    }

    @DisplayName("Get all customer successfully test.")
    @Test
    void getAllCustomerSuccessfullyTest() {
        // Given
        CustomerResponse customerResponse = DtoCustomerTestBuilder.getCustomerResponse();
        SetCustomersResponse setCustomersResponse = new SetCustomersResponse(List.of(customerResponse));

        // When
        when(customerRepository.findAll())
                .thenReturn(List.of(customerEntity));
        when(customerMapper.customerEntityListToSetCustomerResponse(List.of(customerEntity)))
                .thenReturn(setCustomersResponse);

        // Execute
        SetCustomersResponse allCustomers = customerService.getAllCustomers();

        // then
        assertNotNull(allCustomers);
        assertNotNull(allCustomers.setCustomers());

        CustomerResponse customer = allCustomers.setCustomers().getFirst();

        assertNotNull(customer);
        assertEquals(customerResponse.guid(), customer.guid());
        assertEquals(customerResponse.name(), customer.name());
        assertEquals(customerResponse.surname(), customer.surname());
        assertEquals(customerResponse.middleName(), customer.middleName());
        assertEquals(customerResponse.email(), customer.email());
        assertEquals(customerResponse.phone(), customer.phone());
        assertEquals(customerResponse.customerType(), customer.customerType());
        assertEquals(customerResponse.userProfileGuid(), customer.userProfileGuid());
        assertEquals(customerResponse.responsibleManagerGuid(), customer.responsibleManagerGuid());
        assertEquals(customerResponse.createdAt(), customer.createdAt());
        assertEquals(customerResponse.updatedAt(), customer.updatedAt());

        verify(customerRepository).findAll();
        verify(customerMapper).customerEntityListToSetCustomerResponse(any(List.class));
    }
}
