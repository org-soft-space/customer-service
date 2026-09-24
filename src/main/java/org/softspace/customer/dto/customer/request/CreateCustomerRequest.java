package org.softspace.customer.dto.customer.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.softspace.customer.dto.DtoPattern;
import org.softspace.customer.enums.CustomerType;

import java.util.UUID;

public record CreateCustomerRequest(
        @NotBlank
        @Size(max = 100)
        String name,
        @Size(max = 100)
        String surname,
        @Size(max = 100)
        String middleName,
        @Email
        String email,
        @Pattern(
                regexp = DtoPattern.phonePattern,
                message = "Phone must be in international format, for example: +79991234567"
        )
        String phone,
        @NotNull
        CustomerType customerType,
        UUID userProfileGuid,
        UUID responsibleManagerGuid
) {
}
