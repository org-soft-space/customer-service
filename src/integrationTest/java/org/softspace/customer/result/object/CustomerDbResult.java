package org.softspace.customer.result.object;

import org.softspace.customer.enums.CustomerType;

import java.time.Instant;
import java.util.UUID;

public record CustomerDbResult(
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
        Instant updatedAt,
        Instant deletedAt
) {
}
