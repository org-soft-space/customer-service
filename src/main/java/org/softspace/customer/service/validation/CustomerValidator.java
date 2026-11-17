package org.softspace.customer.service.validation;

import org.apache.commons.lang3.StringUtils;
import org.openapitools.jackson.nullable.JsonNullable;
import org.softspace.customer.dto.customer.request.UpdateCustomerRequest;
import org.softspace.customer.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Predicate;

@Component
public class CustomerValidator {

    public static final String NO_ONE_FIELD_FOR_CONNECTION = "Must have one or both field for connecting.";
    public static final String NO_ONE_FIELD_IN_OBJECT = "There is no one parameter.";


    public void validateUpdateCustomerRequest(
            UpdateCustomerRequest updateCustomerRequest,
            String previousEmail,
            String previousPhone
    ) {

        validateParameter(
                updateCustomerRequest.name(),
                name -> name == null || name.isBlank(),
                "name",
                "name must not be blank"
        );

        validateParameter(
                updateCustomerRequest.customerType(),
                customerType -> customerType == null,
                "customerType",
                "customer type must not be null"
        );

        JsonNullable<String> email = updateCustomerRequest.email();
        if (email.isPresent()) {
            previousEmail = email.get();
        }

        JsonNullable<String> phone = updateCustomerRequest.phone();
        if (phone.isPresent()) {
            previousPhone = phone.get();
        }

        if (StringUtils.isBlank(previousEmail) && StringUtils.isBlank(previousPhone)){
            throw new ValidationException(
                    NO_ONE_FIELD_FOR_CONNECTION,
                    Map.of("cause", "Email or phone must be present")
            );
        }

        if (!isOneParameter(updateCustomerRequest)) {
            throw new ValidationException(
                    NO_ONE_FIELD_IN_OBJECT,
                    Map.of()
            );
        }
    }

    private <T> void validateParameter(
            JsonNullable<T> value,
            Predicate<T> predicate,
            String fieldName,
            String reason
    ) {
        if (value == null || !value.isPresent()) {
            return;
        }
        boolean result = predicate.test(value.orElse(null));
        if (result) {
            throw new ValidationException(
                    "Validation error of field.",
                    Map.of(fieldName, reason)
            );
        }
    }

    private boolean isOneParameter(UpdateCustomerRequest updateCustomerRequest) {
        return (updateCustomerRequest.name().isPresent())
                || (updateCustomerRequest.surname().isPresent())
                || (updateCustomerRequest.middleName().isPresent())
                || (updateCustomerRequest.email().isPresent())
                || (updateCustomerRequest.phone().isPresent())
                || (updateCustomerRequest.customerType().isPresent())
                || (updateCustomerRequest.userProfileGuid().isPresent())
                || (updateCustomerRequest.responsibleManagerGuid().isPresent());
    }
}
