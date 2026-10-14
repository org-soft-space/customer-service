package org.softspace.customer.repository;

import org.softspace.customer.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    Optional<CustomerEntity> findByGuidAndDeletedAtIsNull(UUID guid);

    List<CustomerEntity> findAllByDeletedAtIsNull();
}
