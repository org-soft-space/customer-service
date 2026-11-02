package org.softspace.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.softspace.customer.dto.customer.response.CustomerListResponse;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.dtotest.DtoCustomerTestBuilder;
import org.softspace.customer.service.CustomerArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@WebMvcTest(CustomerArchiveController.class)
public class CustomerArchiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerArchiveService customerArchiveService;

    @MockitoBean
    private Tracer tracer;

    private static String CUSTOMER_ARCHIVE_URL = "/api/admin/v1/customers/archive";


    @DisplayName("Get all archived customers. Positive test.")
    @Test
    void shouldGetAllArchivedCustomersSuccessfullyTest() throws Exception {
        // Given
        CustomerResponse customerResponse = DtoCustomerTestBuilder.getCustomerResponse();
        CustomerListResponse customerListResponse = new CustomerListResponse(List.of(customerResponse));

        // When
        when(customerArchiveService.getAllArchivedCustomers()).thenReturn(customerListResponse);

        // Then
        mockMvc.perform(get(CUSTOMER_ARCHIVE_URL))
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

        verify(customerArchiveService).getAllArchivedCustomers();
    }

    @DisplayName("Get archived customer. Positive test")
    @Test
    void shouldGetArchivedCustomerSuccessfullyTest() throws Exception {
        // Given
        CustomerResponse customerResponse = DtoCustomerTestBuilder.getCustomerResponse();
        UUID customerGuid = customerResponse.guid();

        // When
        when(customerArchiveService.getArchivedCustomer(customerGuid)).thenReturn(customerResponse);

        // Execute and expect
        mockMvc.perform(get(CUSTOMER_ARCHIVE_URL + "/{guid}", customerGuid))
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

        verify(customerArchiveService).getArchivedCustomer(customerGuid);
    }

    @DisplayName("Restore archived customer. Positive test")
    @Test
    void shouldRestoreArchivedCustomerSuccessfullyTest() throws Exception {
        // Given
        CustomerResponse customerResponse = DtoCustomerTestBuilder.getCustomerResponse();
        UUID customerGuid = customerResponse.guid();

        // When
        when(customerArchiveService.restoreArchivedCustomer(customerGuid)).thenReturn(customerResponse);

        // Execute and expect
        mockMvc.perform(post(CUSTOMER_ARCHIVE_URL + "/{guid}/restore", customerGuid))
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

        verify(customerArchiveService).restoreArchivedCustomer(customerGuid);
    }

    @DisplayName("Permanently delete archived customer. Positive test")
    @Test
    void shouldPermanentlyDeleteArchivedCustomerSuccessfullyTest() throws Exception {
        // Given
        UUID customerGuid = DtoCustomerTestBuilder.GUID;

        // Execute and expect
        mockMvc.perform(delete(CUSTOMER_ARCHIVE_URL + "/{guid}", customerGuid))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(customerArchiveService).permanentlyDeleteArchivedCustomer(customerGuid);
    }
}
