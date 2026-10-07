package org.softspace.customer.dto.customer.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.openapitools.jackson.nullable.JsonNullable;
import org.softspace.customer.dto.DtoPattern;
import org.softspace.customer.enums.CustomerType;

import java.util.UUID;

public record UpdateCustomerRequest(

        JsonNullable<@Size(max = 100) String> name,
        JsonNullable<@Size(max = 100) String> surname,
        JsonNullable<@Size(max = 100) String> middleName,
        JsonNullable<@Size(max = 255) @Email String> email,
        JsonNullable<@Size(max = 20) @Pattern(
                regexp = DtoPattern.phonePattern,
                message = "Phone must be in international format, for example: +79991234567"
        ) String> phone,
        JsonNullable<CustomerType> customerType,
        JsonNullable<UUID> userProfileGuid,
        JsonNullable<UUID> responsibleManagerGuid
) {
}
