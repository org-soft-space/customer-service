package org.softspace.customer.controller.error.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.jackson.nullable.JsonNullable;
import org.softspace.customer.config.JacksonConfig;
import org.softspace.customer.controller.CustomerController;
import org.softspace.customer.dto.customer.request.CreateCustomerRequest;
import org.softspace.customer.dto.customer.request.UpdateCustomerRequest;
import org.softspace.customer.dtotest.DtoCustomerTestBuilder;
import org.softspace.customer.enums.CustomerType;
import org.softspace.customer.exception.CustomerNotFoundException;
import org.softspace.customer.exception.ValidationException;
import org.softspace.customer.exception.error.ErrorCode;
import org.softspace.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(CustomerController.class)
@Import(JacksonConfig.class)
public class ApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    private static String BASE_URL = "/api/v1/customers";

    // Negative tests

    @DisplayName("Create new customer method throws MethodArgumentNotValidException. Negative test")
    @ParameterizedTest(name = "name={0}, email={1}, phone={2}, customerType={3}")
    @MethodSource("createNewCustomerParams")
    void shouldMethodArgumentNotValidExceptionTest(
            String name,
            String email,
            String phone,
            CustomerType customerType
    ) throws Exception {
        // Given
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                name,
                null,
                null,
                email,
                phone,
                customerType,
                null,
                null
        );

        // Execute and expect
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCustomerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.path").value(BASE_URL));

        verify(customerService, never()).createNewCustomer(createCustomerRequest);
    }

    private static Stream<Arguments> createNewCustomerParams() {
        return Stream.of(
                Arguments.of(null, DtoCustomerTestBuilder.EMAIL, DtoCustomerTestBuilder.PHONE, DtoCustomerTestBuilder.CUSTOMER_TYPE),
                Arguments.of(" ", DtoCustomerTestBuilder.EMAIL, DtoCustomerTestBuilder.PHONE, DtoCustomerTestBuilder.CUSTOMER_TYPE),
                Arguments.of("Name", "email", DtoCustomerTestBuilder.PHONE, DtoCustomerTestBuilder.CUSTOMER_TYPE),
                Arguments.of("Name", " ", DtoCustomerTestBuilder.PHONE, DtoCustomerTestBuilder.CUSTOMER_TYPE),
                Arguments.of("Name", DtoCustomerTestBuilder.EMAIL, "Phone", DtoCustomerTestBuilder.CUSTOMER_TYPE),
                Arguments.of("Name", DtoCustomerTestBuilder.EMAIL, "", DtoCustomerTestBuilder.CUSTOMER_TYPE),
                Arguments.of("Name", DtoCustomerTestBuilder.EMAIL, " ", DtoCustomerTestBuilder.CUSTOMER_TYPE),
                Arguments.of("Name", DtoCustomerTestBuilder.EMAIL, DtoCustomerTestBuilder.PHONE, null)
        );
    }

    @DisplayName("Create customer method throws ValidationException. Negative test")
    @Test
    void shouldCreateCustomerValidationExceptionTest() throws Exception {
        // Given
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                "Name",
                null,
                null,
                null,
                null,
                CustomerType.CUSTOMER,
                null,
                null
        );

        // When
        when(customerService.createNewCustomer(createCustomerRequest)).thenThrow(ValidationException.class);

        // Execute and expect
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCustomerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.path").value(BASE_URL));

        verify(customerService).createNewCustomer(createCustomerRequest);
    }

    @DisplayName("Get customer method throws CustomerNotFoundException. Negative test")
    @Test
    void shouldGetCustomerNotFoundExceptionTest() throws Exception {
        // Given
        UUID customerGuid = DtoCustomerTestBuilder.GUID;

        // When
        when(customerService.getCustomer(customerGuid)).thenThrow(CustomerNotFoundException.class);

        // Execute and expect
        mockMvc.perform(get(BASE_URL + "/{guid}", customerGuid))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ErrorCode.CUSTOMER_NOT_FOUND.name()))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/" + customerGuid));

        verify(customerService).getCustomer(customerGuid);
    }

    @DisplayName("Get all customers method throws RuntimeException. Negative test")
    @Test
    void shouldGetAllCustomersRuntimeExceptionTest() throws Exception {

        // When
        when(customerService.getAllCustomers()).thenThrow(RuntimeException.class);

        // Execute and expect
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ErrorCode.INTERNAL_ERROR.name()))
                .andExpect(jsonPath("$.path").value(BASE_URL));

        verify(customerService).getAllCustomers();
    }

    @DisplayName("Update customer method throws MethodArgumentNotValidException. Negative test")
    @ParameterizedTest(name = "email = {0}, phone = {1}, expectedFieldName = {2}")
    @MethodSource("invalidUpdatingParams")
    void shouldUpdateCustomerMethodArgumentNotValidExceptionTest(
            String email,
            String phone,
            String expectedFieldName
    ) throws Exception {
        // Given
        UUID guid = DtoCustomerTestBuilder.GUID;
        UpdateCustomerRequest updateCustomerRequest = new UpdateCustomerRequest(
                JsonNullable.of("Test"),
                JsonNullable.of("Test"),
                JsonNullable.of("Test"),
                JsonNullable.of(email),
                JsonNullable.of(phone),
                JsonNullable.of(CustomerType.LEAD),
                JsonNullable.of(UUID.randomUUID()),
                JsonNullable.of(UUID.randomUUID())
        );

        // Execute and expect
        mockMvc.perform(patch(BASE_URL + "/{guid}", guid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCustomerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/" + guid))
                .andExpect(jsonPath("$.details." + expectedFieldName).exists());
        ;

        verify(customerService, never()).updateCustomer(any(UUID.class), any(UpdateCustomerRequest.class));
    }

    private static Stream<Arguments> invalidUpdatingParams() {
        return Stream.of(
                Arguments.of(DtoCustomerTestBuilder.EMAIL, "phone", "phone"),
                Arguments.of("email", DtoCustomerTestBuilder.PHONE, "email")
        );
    }

    @DisplayName("Update customer method throws CustomerNotFoundException. Negative test")
    @Test
    void shouldUpdateCustomerNotFoundExceptionTest() throws Exception {
        // Given
        UUID customerGuid = DtoCustomerTestBuilder.GUID;
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

        // When
        when(customerService.updateCustomer(customerGuid, updateCustomerRequest)).thenThrow(CustomerNotFoundException.class);

        // Execute and expect
        mockMvc.perform(patch(BASE_URL + "/{guid}", customerGuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCustomerRequest)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ErrorCode.CUSTOMER_NOT_FOUND.name()))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/" + customerGuid));

        verify(customerService).updateCustomer(customerGuid, updateCustomerRequest);
    }

    @DisplayName("Get customer type mismatch exception. Negative test")
    @Test
    void shouldGetCustomerTypeMismatchException() throws Exception {
        // Given
        String customerGuid = "abc";

        // Execute and expect
        MvcResult mvcResult = mockMvc.perform(get(BASE_URL + "/{guid}", customerGuid))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ErrorCode.TYPE_MISMATCH_ERROR.name()))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/" + customerGuid))
                .andReturn();
        assertInstanceOf(MethodArgumentTypeMismatchException.class, mvcResult.getResolvedException());
        verify(customerService, never()).getCustomer(any());
    }

    @DisplayName("Update customer type mismatch exception. Negative test")
    @Test
    void shouldUpdateCustomerTypeMismatchException() throws Exception {
        // Given
        String customerGuid = "abc";
        UpdateCustomerRequest request = DtoCustomerTestBuilder.getUpdateCustomerRequest();

        // Execute and expect
        MvcResult mvcResult = mockMvc.perform(patch(BASE_URL + "/{guid}", customerGuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(ErrorCode.TYPE_MISMATCH_ERROR.name()))
                .andExpect(jsonPath("$.path").value(BASE_URL + "/" + customerGuid))
                .andReturn();
        assertInstanceOf(MethodArgumentTypeMismatchException.class, mvcResult.getResolvedException());
        verify(customerService, never()).updateCustomer(any(), any());
    }
}
