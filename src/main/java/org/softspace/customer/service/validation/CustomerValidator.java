package org.softspace.customer.service.validation;

import org.openapitools.jackson.nullable.JsonNullable;
import org.softspace.customer.dto.customer.request.UpdateCustomerRequest;
import org.softspace.customer.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Predicate;

@Component
public class CustomerValidator {

    // TODO: name или customerType переданы как null — 400 Bad Request;
    // TODO: после обновления должен оставаться хотя бы один непустой контакт: email или телефон;
    // TODO: пустое тело запроса — 400 Bad Request;
    public void validateUpdateCustomerRequest(
            UpdateCustomerRequest updateCustomerRequest,
            String lastEmail,
            String lastPhone
    ) {

        validateParameter(
                updateCustomerRequest.name(),
                name -> name == null || name.isBlank(),
                "name"
        );

        validateParameter(
                updateCustomerRequest.customerType(),
                customerType -> customerType == null,
                "customerType"
        );

        JsonNullable<String> email = updateCustomerRequest.email();
        if (email.isPresent()) {
            lastEmail = email.get();
        }

        JsonNullable<String> phone = updateCustomerRequest.phone();
        if (phone.isPresent()) {
            lastPhone = phone.get();
        }

        if (lastEmail == null && lastPhone == null) {
            throw new ValidationException(
                    "Must have one or both field for connecting.",
                    Map.of("fieldName", "email", "fieldName", "phone")
            );
        }

        if (!isOneParameter(updateCustomerRequest)) {
            throw new ValidationException(
                    "There is no one parameter.",
                    Map.of()
            );
        }
    }

    private <T> void validateParameter(
            JsonNullable<T> value,
            Predicate<T> predicate,
            String fieldName
    ) {
        if (value == null || !value.isPresent()) {
            return;
        }
        boolean result = predicate.test(value.orElse(null));
        if (result) {
            throw new ValidationException(
                    "Validation error of field.",
                    Map.of("fieldName", fieldName)
            );
        }
    }

    private boolean isOneParameter(UpdateCustomerRequest updateCustomerRequest) {
        return (updateCustomerRequest.name().isPresent() && updateCustomerRequest.name().get() != null)
                || (updateCustomerRequest.surname().isPresent() && updateCustomerRequest.surname().get()!= null)
                || (updateCustomerRequest.middleName().isPresent() && updateCustomerRequest.middleName().get()!= null)
                || (updateCustomerRequest.email().isPresent() && updateCustomerRequest.email().get()!= null)
                || (updateCustomerRequest.phone().isPresent() && updateCustomerRequest.phone().get()!= null)
                || (updateCustomerRequest.customerType().isPresent() && updateCustomerRequest.customerType().get()!= null)
                || (updateCustomerRequest.userProfileGuid().isPresent() && updateCustomerRequest.userProfileGuid().get()!= null)
                || (updateCustomerRequest.responsibleManagerGuid().isPresent() && updateCustomerRequest.responsibleManagerGuid().get()!= null);
    }
}
