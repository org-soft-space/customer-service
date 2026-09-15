package org.softspace.customer.dto.customer.response;

import org.softspace.customer.enums.CustomerType;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID guid,
        String name,
        String surname,
        String middleName,
        String email,
        String phone,
        CustomerType customerType,
        UUID userProfileGuid,
        UUID responsibleManagerGuid,
        Instant createdAt,
        Instant updatedAt
) {
}
