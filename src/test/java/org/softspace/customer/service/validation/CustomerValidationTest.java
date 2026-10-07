package org.softspace.customer.service.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.jackson.nullable.JsonNullable;
import org.softspace.customer.dto.customer.request.UpdateCustomerRequest;
import org.softspace.customer.dtotest.DtoCustomerTestBuilder;
import org.softspace.customer.enums.CustomerType;
import org.softspace.customer.exception.ValidationException;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CustomerValidationTest {

    private final CustomerValidator validator = new CustomerValidator();

    @DisplayName("Validation UpdateCustomerRequest successfully test.")
    @Test
    void validationTest() {
        // Given
        String lastEmail = DtoCustomerTestBuilder.EMAIL;
        String lastPhone = DtoCustomerTestBuilder.PHONE;
        UpdateCustomerRequest updateCustomerRequest = DtoCustomerTestBuilder.getUpdateCustomerRequest();

        // Execute
        assertDoesNotThrow(
                () -> validator.validateUpdateCustomerRequest(updateCustomerRequest, lastEmail, lastPhone)
        );
    }

    @DisplayName("Validation contacts successfully test.")
    @ParameterizedTest(name = "newEmail = {0}, newPhone = {1}, lastEmail = {2}, lastPhone = {3}")
    @MethodSource("contactParameters")
    void customerContactTest(
            JsonNullable<String> newEmail,
            JsonNullable<String> newPhone,
            String lastEmail,
            String lastPhone
    ) {
        UpdateCustomerRequest updateCustomerRequest = new UpdateCustomerRequest(
                JsonNullable.of("Name"),
                JsonNullable.of("Surname"),
                JsonNullable.of("MiddleName"),
                newEmail,
                newPhone,
                JsonNullable.of(CustomerType.LEAD),
                JsonNullable.of(UUID.randomUUID()),
                JsonNullable.of(UUID.randomUUID())
        );

        assertDoesNotThrow(
                () -> validator.validateUpdateCustomerRequest(
                        updateCustomerRequest,
                        lastEmail,
                        lastPhone
                )
        );
    }

    private static Stream<Arguments> contactParameters() {
        return Stream.of(
                Arguments.of(JsonNullable.undefined(), JsonNullable.undefined(), DtoCustomerTestBuilder.EMAIL, DtoCustomerTestBuilder.PHONE),
                Arguments.of(JsonNullable.undefined(), JsonNullable.undefined(), DtoCustomerTestBuilder.EMAIL, null),
                Arguments.of(JsonNullable.undefined(), JsonNullable.undefined(), null, DtoCustomerTestBuilder.PHONE),
                Arguments.of(JsonNullable.of(null), JsonNullable.of(DtoCustomerTestBuilder.PHONE), DtoCustomerTestBuilder.EMAIL, null),
                Arguments.of(JsonNullable.of(DtoCustomerTestBuilder.EMAIL), JsonNullable.of(null), null, DtoCustomerTestBuilder.PHONE)
        );
    }

    // Negative tests

    @DisplayName("Input empty UpdateCustomerRequest with undefined fields negative test.")
    @Test
    void emptyRequestWithUndefinedTest() {
        // Given
        String lastEmail = DtoCustomerTestBuilder.EMAIL;
        String lastPhone = DtoCustomerTestBuilder.PHONE;
        UpdateCustomerRequest updateCustomerRequest = new UpdateCustomerRequest(
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined(),
                JsonNullable.undefined()
        );

        // Execute
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateUpdateCustomerRequest(updateCustomerRequest, lastEmail, lastPhone)
        );
    }

    @DisplayName("Input empty UpdateCustomerRequest with null fields negative test.")
    @Test
    void emptyRequestWithNullFieldsTest() {
        // Given
        String lastEmail = DtoCustomerTestBuilder.EMAIL;
        String lastPhone = DtoCustomerTestBuilder.PHONE;
        UpdateCustomerRequest updateCustomerRequest = new UpdateCustomerRequest(
                JsonNullable.of(null),
                JsonNullable.of(null),
                JsonNullable.of(null),
                JsonNullable.of(null),
                JsonNullable.of(null),
                JsonNullable.of(null),
                JsonNullable.of(null),
                JsonNullable.of(null)
        );

        // Execute
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> validator.validateUpdateCustomerRequest(updateCustomerRequest, lastEmail, lastPhone)
        );
    }

    @DisplayName("Validation UpdateCustomerRequest with not nullable fields negative test.")
    @ParameterizedTest(name = "name = {0}, customerType = {1}, expectedFieldName = {2}")
    @MethodSource("notNullableParameters")
    void notNullableFieldsTest(
            String name,
            CustomerType customerType,
            String expectedFieldName
    ) {
        UpdateCustomerRequest updateCustomerRequest = new UpdateCustomerRequest(
                JsonNullable.of(name),
                JsonNullable.of(null),
                JsonNullable.of(null),
                JsonNullable.of("updated.email@softspace.org"),
                JsonNullable.of("+981123456"),
                JsonNullable.of(customerType),
                JsonNullable.of(null),
                JsonNullable.of(null)
        );

        ValidationException exceptionResult = assertThrows(
                ValidationException.class,
                () -> validator.validateUpdateCustomerRequest(
                        updateCustomerRequest,
                        DtoCustomerTestBuilder.EMAIL,
                        DtoCustomerTestBuilder.PHONE
                )
        );

        assertEquals(expectedFieldName, exceptionResult.getDetails().get("fieldName"));
    }

    private static Stream<Arguments> notNullableParameters() {
        return Stream.of(
                Arguments.of(null, CustomerType.LEAD, "name"),
                Arguments.of("Name", null, "customerType")
        );
    }


    @DisplayName("Validation UpdateCustomerRequest with no one contact negative test.")
    @ParameterizedTest(name = "newEmail = {0}, newPhone = {1}, lastEmail = {2}, lastPhone = {3}")
    @MethodSource("noOneContactParameters")
    void noOneContactTest(
            JsonNullable<String> newEmail,
            JsonNullable<String> newPhone,
            String lastEmail,
            String lastPhone
    ) {
        UpdateCustomerRequest updateCustomerRequest = new UpdateCustomerRequest(
                JsonNullable.of("Name"),
                JsonNullable.of("Surname"),
                JsonNullable.of("MiddleName"),
                newEmail,
                newPhone,
                JsonNullable.of(CustomerType.LEAD),
                JsonNullable.of(UUID.randomUUID()),
                JsonNullable.of(UUID.randomUUID())
        );

        ValidationException exceptionResult = assertThrows(
                ValidationException.class,
                () -> validator.validateUpdateCustomerRequest(
                        updateCustomerRequest,
                        lastEmail,
                        lastPhone
                )
        );

        assertEquals(CustomerValidator.NO_ONE_FIELD_FOR_CONNECTION, exceptionResult.getMessage());
    }

    private static Stream<Arguments> noOneContactParameters() {
        return Stream.of(
                Arguments.of(JsonNullable.of(null), JsonNullable.of(null), DtoCustomerTestBuilder.EMAIL, DtoCustomerTestBuilder.PHONE),
                Arguments.of(JsonNullable.of(null), JsonNullable.undefined(), DtoCustomerTestBuilder.EMAIL, null),
                Arguments.of(JsonNullable.undefined(), JsonNullable.of(null), null, DtoCustomerTestBuilder.PHONE)
        );
    }
}
