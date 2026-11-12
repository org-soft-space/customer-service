package org.softspace.customer.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.softspace.customer.dto.customer.response.ArchivedCustomerListResponse;
import org.softspace.customer.dto.customer.response.ArchivedCustomerResponse;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.dtotest.DtoCustomerTestBuilder;
import org.softspace.customer.entity.CustomerEntity;
import org.softspace.customer.exception.CustomerNotArchivedException;
import org.softspace.customer.exception.CustomerNotFoundException;
import org.softspace.customer.repository.CustomerRepository;
import org.softspace.customer.service.mapper.CustomerMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerArchiveServiceTest {

    @InjectMocks
    private CustomerArchiveService customerArchiveService;

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
                DtoCustomerTestBuilder.PHONE,
                DtoCustomerTestBuilder.EMAIL,
                DtoCustomerTestBuilder.CUSTOMER_TYPE,
                DtoCustomerTestBuilder.USER_PROFILE_GUID,
                DtoCustomerTestBuilder.RESPONSIBLE_MANAGER_GUID,
                DtoCustomerTestBuilder.TIME,
                DtoCustomerTestBuilder.TIME,
                Instant.now()
        );
    }

    // ---- Positive tests ----

    @DisplayName("Get all archived customers successfully test.")
    @Test
    void getAllArchivedCustomersSuccessfullyTest() {
        // When
        when(customerRepository.findAllByDeletedAtIsNotNull())
                .thenReturn(List.of(customerEntity));
        when(customerMapper.customerEntityListToArchivedCustomerResponseList(List.of(customerEntity)))
                .thenReturn(List.of(DtoCustomerTestBuilder.getArchivedCustomerResponse()));

        // Then
        ArchivedCustomerListResponse response = customerArchiveService.getAllArchivedCustomers();

        assertNotNull(response);
        assertNotNull(response.archivedCustomers());

        ArchivedCustomerResponse customerResponseResult = response.archivedCustomers().getFirst();

        assertNotNull(customerResponseResult);

        assertEquals(customerEntity.getGuid(), customerResponseResult.guid());
        assertEquals(customerEntity.getName(), customerResponseResult.name());
        assertEquals(customerEntity.getSurname(), customerResponseResult.surname());
        assertEquals(customerEntity.getMiddleName(), customerResponseResult.middleName());
        assertEquals(customerEntity.getEmail(), customerResponseResult.email());
        assertEquals(customerEntity.getPhone(), customerResponseResult.phone());
        assertEquals(customerEntity.getCustomerType(), customerResponseResult.customerType());
        assertEquals(customerEntity.getUserProfileGuid(), customerResponseResult.userProfileGuid());
        assertEquals(customerEntity.getResponsibleManagerGuid(), customerResponseResult.responsibleManagerGuid());
        assertEquals(customerEntity.getCreatedAt(), customerResponseResult.createdAt());
        assertEquals(customerEntity.getUpdatedAt(), customerResponseResult.updatedAt());

        verify(customerRepository).findAllByDeletedAtIsNotNull();
        verify(customerMapper).customerEntityListToArchivedCustomerResponseList(any(List.class));
    }

    @DisplayName("Get archived customer successfully test.")
    @Test
    void getArchivedCustomerSuccessfullyTest() {
        // Given
        UUID guid = DtoCustomerTestBuilder.GUID;
        ArchivedCustomerResponse customerResponse = DtoCustomerTestBuilder.getArchivedCustomerResponse();

        // When
        when(customerRepository.findByGuidAndDeletedAtIsNotNull(guid))
                .thenReturn(Optional.of(customerEntity));
        when(customerMapper.customerEntityToArchivedCustomerResponse(customerEntity))
                .thenReturn(customerResponse);

        // Then
        ArchivedCustomerResponse customerResponseResult = customerArchiveService.getArchivedCustomer(guid);

        assertNotNull(customerResponseResult);

        assertEquals(customerResponse.guid(), customerResponseResult.guid());
        assertEquals(customerResponse.name(), customerResponseResult.name());
        assertEquals(customerResponse.surname(), customerResponseResult.surname());
        assertEquals(customerResponse.middleName(), customerResponseResult.middleName());
        assertEquals(customerResponse.email(), customerResponseResult.email());
        assertEquals(customerResponse.phone(), customerResponseResult.phone());
        assertEquals(customerResponse.customerType(), customerResponseResult.customerType());
        assertEquals(customerResponse.userProfileGuid(), customerResponseResult.userProfileGuid());
        assertEquals(customerResponse.responsibleManagerGuid(), customerResponseResult.responsibleManagerGuid());
        assertEquals(customerResponse.createdAt(), customerResponseResult.createdAt());
        assertEquals(customerResponse.updatedAt(), customerResponseResult.updatedAt());

        assertEquals(customerEntity.getGuid(), customerResponseResult.guid());
        assertEquals(customerEntity.getName(), customerResponseResult.name());
        assertEquals(customerEntity.getSurname(), customerResponseResult.surname());
        assertEquals(customerEntity.getMiddleName(), customerResponseResult.middleName());
        assertEquals(customerEntity.getEmail(), customerResponseResult.email());
        assertEquals(customerEntity.getPhone(), customerResponseResult.phone());
        assertEquals(customerEntity.getCustomerType(), customerResponseResult.customerType());
        assertEquals(customerEntity.getUserProfileGuid(), customerResponseResult.userProfileGuid());
        assertEquals(customerEntity.getResponsibleManagerGuid(), customerResponseResult.responsibleManagerGuid());
        assertEquals(customerEntity.getCreatedAt(), customerResponseResult.createdAt());
        assertEquals(customerEntity.getUpdatedAt(), customerResponseResult.updatedAt());

        verify(customerRepository).findByGuidAndDeletedAtIsNotNull(guid);
        verify(customerMapper).customerEntityToArchivedCustomerResponse(customerEntity);
    }

    @DisplayName("Restore archived customer successfully test.")
    @Test
    void restoreArchivedCustomerSuccessfullyTest() {
        // Given
        UUID guid = DtoCustomerTestBuilder.GUID;
        ArchivedCustomerResponse customerResponse = DtoCustomerTestBuilder.getArchivedCustomerResponse();

        Instant updatedAtBeforeRestore = customerEntity.getUpdatedAt();

        // When
        when(customerRepository.findByGuid(guid))
                .thenReturn(Optional.of(customerEntity));
        when(customerMapper.customerEntityToArchivedCustomerResponse(customerEntity))
                .thenReturn(customerResponse);

        assertNotNull(customerEntity.getDeletedAt());

        // Then
        ArchivedCustomerResponse customerResponseResult = customerArchiveService.restoreArchivedCustomer(guid);

        assertNull(customerEntity.getDeletedAt());
        assertNotEquals(updatedAtBeforeRestore, customerEntity.getUpdatedAt());

        assertNotNull(customerResponseResult);

        assertEquals(customerResponse.guid(), customerResponseResult.guid());
        assertEquals(customerResponse.name(), customerResponseResult.name());
        assertEquals(customerResponse.surname(), customerResponseResult.surname());
        assertEquals(customerResponse.middleName(), customerResponseResult.middleName());
        assertEquals(customerResponse.email(), customerResponseResult.email());
        assertEquals(customerResponse.phone(), customerResponseResult.phone());
        assertEquals(customerResponse.customerType(), customerResponseResult.customerType());
        assertEquals(customerResponse.userProfileGuid(), customerResponseResult.userProfileGuid());
        assertEquals(customerResponse.responsibleManagerGuid(), customerResponseResult.responsibleManagerGuid());
        assertEquals(customerResponse.createdAt(), customerResponseResult.createdAt());

        assertEquals(customerEntity.getGuid(), customerResponseResult.guid());
        assertEquals(customerEntity.getName(), customerResponseResult.name());
        assertEquals(customerEntity.getSurname(), customerResponseResult.surname());
        assertEquals(customerEntity.getMiddleName(), customerResponseResult.middleName());
        assertEquals(customerEntity.getEmail(), customerResponseResult.email());
        assertEquals(customerEntity.getPhone(), customerResponseResult.phone());
        assertEquals(customerEntity.getCustomerType(), customerResponseResult.customerType());
        assertEquals(customerEntity.getUserProfileGuid(), customerResponseResult.userProfileGuid());
        assertEquals(customerEntity.getResponsibleManagerGuid(), customerResponseResult.responsibleManagerGuid());
        assertEquals(customerEntity.getCreatedAt(), customerResponseResult.createdAt());

        verify(customerRepository).findByGuid(guid);
        verify(customerMapper).customerEntityToArchivedCustomerResponse(customerEntity);

    }

    @DisplayName("Permanently delete archived customer successfully test.")
    @Test
    void permanentlyDeleteArchivedCustomerSuccessfullyTest() {
        // Given
        UUID guid = DtoCustomerTestBuilder.GUID;

        // When
        when(customerRepository.findByGuid(guid))
                .thenReturn(Optional.of(customerEntity));

        // Execute and then
        assertDoesNotThrow(() -> customerArchiveService.permanentlyDeleteArchivedCustomer(guid));

        verify(customerRepository).findByGuid(guid);
        verify(customerRepository).deleteByGuidAndDeletedAtIsNotNull(guid);
    }

    // ---- Negative tests ----

    @DisplayName("Get archived customer throws CustomerNotFoundException. Negative test.")
    @Test
    void getArchivedCustomerThrowsCustomerNotFoundExceptionTest() {
        // Given
        UUID guid = DtoCustomerTestBuilder.GUID;

        // When
        when(customerRepository.findByGuidAndDeletedAtIsNotNull(guid))
                .thenReturn(Optional.empty());

        assertThrows(
                CustomerNotFoundException.class,
                () -> customerArchiveService.getArchivedCustomer(guid)
        );

        verify(customerRepository).findByGuidAndDeletedAtIsNotNull(guid);
        verify(customerMapper, never()).customerEntityToCustomerResponse(any());
    }

    @DisplayName("Restore archived customer throws CustomerNotFoundException. Negative test.")
    @Test
    void restoreArchivedCustomerThrowsCustomerNotFoundExceptionTest() {
        // Given
        UUID guid = DtoCustomerTestBuilder.GUID;
        CustomerResponse customerResponse = DtoCustomerTestBuilder.getCustomerResponse();

        // When
        when(customerRepository.findByGuid(guid))
                .thenReturn(Optional.empty());

        assertThrows(
                CustomerNotFoundException.class,
                () -> customerArchiveService.restoreArchivedCustomer(guid)
        );

        verify(customerRepository).findByGuid(guid);
        verify(customerMapper, never()).customerEntityToCustomerResponse(any());
    }

    @DisplayName("Restore archived customer throws CustomerNotArchivedException. Negative test.")
    @Test
    void restoreArchivedCustomerThrowsCustomerNotArchivedExceptionTest() {
        // Given
        UUID guid = DtoCustomerTestBuilder.GUID;
        customerEntity.setDeletedAt(null);

        // When
        when(customerRepository.findByGuid(guid))
                .thenReturn(Optional.of(customerEntity));

        assertThrows(
                CustomerNotArchivedException.class,
                () -> customerArchiveService.restoreArchivedCustomer(guid)
        );

        verify(customerRepository).findByGuid(guid);
        verify(customerMapper, never()).customerEntityToCustomerResponse(any());
    }

    @DisplayName("Delete archived customer throws CustomerNotFoundException. Negative test.")
    @Test
    void deleteArchivedCustomerThrowsCustomerNotFoundExceptionTest() {
        // Given
        UUID guid = DtoCustomerTestBuilder.GUID;
        CustomerResponse customerResponse = DtoCustomerTestBuilder.getCustomerResponse();

        // When
        when(customerRepository.findByGuid(guid))
                .thenReturn(Optional.empty());

        assertThrows(
                CustomerNotFoundException.class,
                () -> customerArchiveService.permanentlyDeleteArchivedCustomer(guid)
        );

        verify(customerRepository).findByGuid(guid);
        verify(customerRepository, never()).deleteByGuidAndDeletedAtIsNotNull(any());
    }

    @DisplayName("Delete archived customer throws CustomerNotArchivedException. Negative test.")
    @Test
    void deleteArchivedCustomerThrowsCustomerNotArchivedExceptionTest() {
        // Given
        UUID guid = DtoCustomerTestBuilder.GUID;
        customerEntity.setDeletedAt(null);

        // When
        when(customerRepository.findByGuid(guid))
                .thenReturn(Optional.of(customerEntity));

        assertThrows(
                CustomerNotArchivedException.class,
                () -> customerArchiveService.permanentlyDeleteArchivedCustomer(guid)
        );

        verify(customerRepository).findByGuid(guid);
        verify(customerRepository, never()).deleteByGuidAndDeletedAtIsNotNull(any());

    }
}
