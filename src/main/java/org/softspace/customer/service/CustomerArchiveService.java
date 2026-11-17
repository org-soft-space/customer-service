package org.softspace.customer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.softspace.customer.dto.customer.response.ArchivedCustomerListResponse;
import org.softspace.customer.dto.customer.response.ArchivedCustomerResponse;
import org.softspace.customer.entity.CustomerEntity;
import org.softspace.customer.exception.CustomerNotArchivedException;
import org.softspace.customer.exception.CustomerNotFoundException;
import org.softspace.customer.repository.CustomerRepository;
import org.softspace.customer.service.mapper.CustomerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerArchiveService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional(readOnly = true)
    public ArchivedCustomerListResponse getAllArchivedCustomers() {
        log.info("Get all archived customers");
        List<CustomerEntity> customerArchivedCustomerList = customerRepository.findAllByDeletedAtIsNotNull();
        return new ArchivedCustomerListResponse(
                customerMapper.customerEntityListToArchivedCustomerResponseList(customerArchivedCustomerList)
        );
    }

    @Transactional(readOnly = true)
    public ArchivedCustomerResponse getArchivedCustomer(UUID guid) {
        log.info("Get archived customer by guid: {}", guid);
        CustomerEntity customerEntity = customerRepository.findByGuidAndDeletedAtIsNotNull(guid)
                .orElseThrow(
                        () -> new CustomerNotFoundException(
                                "Archived customer not found",
                                Map.of("GUID", guid)
                        )
                );
        return customerMapper.customerEntityToArchivedCustomerResponse(customerEntity);
    }

    @Transactional
    public ArchivedCustomerResponse restoreArchivedCustomer(UUID guid) {
        log.info("Restore archived customer: {}", guid);
        CustomerEntity customerEntity = customerRepository.findByGuid(guid)
                .orElseThrow(
                        () -> new CustomerNotFoundException(
                                "Archived customer not found",
                                Map.of("GUID", guid)
                        )
                );
        if (customerEntity.getDeletedAt() == null) {
            throw new CustomerNotArchivedException(
                    "Customer exists, but not archived",
                    Map.of("GUID", guid)
            );
        }
        Instant newUpdatedAt = Instant.now();
        customerEntity.setDeletedAt(null);
        customerEntity.setUpdatedAt(newUpdatedAt);
        return customerMapper.customerEntityToArchivedCustomerResponse(customerEntity);
    }

    @Transactional
    public void permanentlyDeleteArchivedCustomer(UUID guid) {
        log.info("Delete archived customer: {}", guid);
        CustomerEntity customerEntity = customerRepository.findByGuid(guid)
                .orElseThrow(
                        () -> new CustomerNotFoundException(
                                "Customer not found",
                                Map.of("GUID", guid)
                        )
                );
        if (customerEntity.getDeletedAt() == null) {
            throw new CustomerNotArchivedException(
                    "Customer exists, but not archived",
                    Map.of("GUID", guid)
            );
        }
        customerRepository.deleteByGuidAndDeletedAtIsNotNull(guid);
    }
}
