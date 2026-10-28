package org.softspace.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.softspace.customer.config.JacksonConfig;
import org.softspace.customer.dto.customer.request.CreateCustomerRequest;
import org.softspace.customer.dto.customer.request.UpdateCustomerRequest;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.dtotest.DtoCustomerTestBuilder;
import org.softspace.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import org.softspace.customer.dto.customer.response.CustomerListResponse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(CustomerController.class)
@Import(JacksonConfig.class)
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    private static final String BASE_URL = "/api/v1/customers";

    // Positive tests.

    @DisplayName("Create new customer. Positive test")
    @Test
    void shouldCreateNewCustomerSuccessfullyTest() throws Exception {
        // Given
        CreateCustomerRequest createCustomerRequest = DtoCustomerTestBuilder.getCreateCustomerRequest();
        CustomerResponse customerResponse = DtoCustomerTestBuilder.getCustomerResponse();

        // When
        when(customerService.createNewCustomer(createCustomerRequest)).thenReturn(customerResponse);

        // Execute and expect
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCustomerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.guid").value(customerResponse.guid().toString()))
                .andExpect(jsonPath("$.name").value(customerResponse.name()))
                .andExpect(jsonPath("$.surname").value(customerResponse.surname()))
                .andExpect(jsonPath("$.middleName").value(customerResponse.middleName()))
                .andExpect(jsonPath("$.email").value(customerResponse.email()))
                .andExpect(jsonPath("$.phone").value(customerResponse.phone()))
                .andExpect(jsonPath("$.customerType").value(customerResponse.customerType().toString()))
                .andExpect(jsonPath("$.userProfileGuid").value(customerResponse.userProfileGuid().toString()))
                .andExpect(jsonPath("$.responsibleManagerGuid").value(customerResponse.responsibleManagerGuid().toString()))
                .andExpect(jsonPath("$.createdAt").value(customerResponse.createdAt().toString()))
                .andExpect(jsonPath("$.updatedAt").value(customerResponse.updatedAt().toString()));

        verify(customerService).createNewCustomer(createCustomerRequest);
    }

    @DisplayName("Get customer. Positive test")
    @Test
    void shouldGetCustomerSuccessfullyTest() throws Exception {
        // Given
        CustomerResponse customerResponse = DtoCustomerTestBuilder.getCustomerResponse();
        UUID customerGuid = DtoCustomerTestBuilder.GUID;

        // When
        when(customerService.getCustomer(customerGuid)).thenReturn(customerResponse);

        // Execute and expect
        mockMvc.perform(get(BASE_URL + "/{guid}", customerGuid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.guid").value(customerResponse.guid().toString()))
                .andExpect(jsonPath("$.name").value(customerResponse.name()))
                .andExpect(jsonPath("$.surname").value(customerResponse.surname()))
                .andExpect(jsonPath("$.middleName").value(customerResponse.middleName()))
                .andExpect(jsonPath("$.email").value(customerResponse.email()))
                .andExpect(jsonPath("$.phone").value(customerResponse.phone()))
                .andExpect(jsonPath("$.customerType").value(customerResponse.customerType().toString()))
                .andExpect(jsonPath("$.userProfileGuid").value(customerResponse.userProfileGuid().toString()))
                .andExpect(jsonPath("$.responsibleManagerGuid").value(customerResponse.responsibleManagerGuid().toString()))
                .andExpect(jsonPath("$.createdAt").value(customerResponse.createdAt().toString()))
                .andExpect(jsonPath("$.updatedAt").value(customerResponse.updatedAt().toString()));

        verify(customerService).getCustomer(customerGuid);
    }

    @DisplayName("Get all customer. Positive test")
    @Test
    void shouldGetAllCustomersSuccessfullyTest() throws Exception {
        // Given
        CustomerResponse customerResponse = DtoCustomerTestBuilder.getCustomerResponse();
        CustomerListResponse customerListResponse = new CustomerListResponse(List.of(customerResponse));

        // When
        when(customerService.getAllCustomers()).thenReturn(customerListResponse);

        // Execute and expect
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customers").isArray())
                .andExpect(jsonPath("$.customers.length()").value(1))
                .andExpect(jsonPath("$.customers[0].guid").value(customerResponse.guid().toString()))
                .andExpect(jsonPath("$.customers[0].name").value(customerResponse.name()))
                .andExpect(jsonPath("$.customers[0].surname").value(customerResponse.surname()))
                .andExpect(jsonPath("$.customers[0].middleName").value(customerResponse.middleName()))
                .andExpect(jsonPath("$.customers[0].email").value(customerResponse.email()))
                .andExpect(jsonPath("$.customers[0].phone").value(customerResponse.phone()))
                .andExpect(jsonPath("$.customers[0].customerType").value(customerResponse.customerType().toString()))
                .andExpect(jsonPath("$.customers[0].userProfileGuid").value(customerResponse.userProfileGuid().toString()))
                .andExpect(jsonPath("$.customers[0].responsibleManagerGuid").value(customerResponse.responsibleManagerGuid().toString()))
                .andExpect(jsonPath("$.customers[0].createdAt").value(customerResponse.createdAt().toString()))
                .andExpect(jsonPath("$.customers[0].updatedAt").value(customerResponse.updatedAt().toString()));

        verify(customerService).getAllCustomers();
    }

    @DisplayName("Update customer. Positive test")
    @Test
    void shouldUpdateCustomerSuccessfullyTest() throws Exception {
        // Given
        UpdateCustomerRequest updateCustomerRequest = DtoCustomerTestBuilder.getUpdateCustomerRequest();
        CustomerResponse customerResponse = DtoCustomerTestBuilder.getCustomerResponse();
        UUID customerGuid = DtoCustomerTestBuilder.GUID;

        // When
        when(customerService.updateCustomer(customerGuid, updateCustomerRequest)).thenReturn(customerResponse);

        // Execute and expect
        mockMvc.perform(patch(BASE_URL + "/{guid}", customerGuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCustomerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.guid").value(customerResponse.guid().toString()))
                .andExpect(jsonPath("$.name").value(customerResponse.name()))
                .andExpect(jsonPath("$.surname").value(customerResponse.surname()))
                .andExpect(jsonPath("$.middleName").value(customerResponse.middleName()))
                .andExpect(jsonPath("$.email").value(customerResponse.email()))
                .andExpect(jsonPath("$.phone").value(customerResponse.phone()))
                .andExpect(jsonPath("$.customerType").value(customerResponse.customerType().toString()))
                .andExpect(jsonPath("$.userProfileGuid").value(customerResponse.userProfileGuid().toString()))
                .andExpect(jsonPath("$.responsibleManagerGuid").value(customerResponse.responsibleManagerGuid().toString()))
                .andExpect(jsonPath("$.createdAt").value(customerResponse.createdAt().toString()))
                .andExpect(jsonPath("$.updatedAt").value(customerResponse.updatedAt().toString()));

        verify(customerService).updateCustomer(customerGuid, updateCustomerRequest);
    }
}
