package org.softspace.customer.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.softspace.customer.dto.customer.request.CreateCustomerRequest;
import org.softspace.customer.enums.CustomerType;
import org.softspace.customer.result.ObjectBuilder;
import org.softspace.customer.result.object.CustomerDbResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.ResultSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String BASE_URL = "/api/v1/customers";
    private static final UUID CUSTOMER_GUID = UUID.fromString("c479a77f-0386-4a0f-9245-0927d25b0e77");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withInitScript("sql/init/create-schema.sql")
            .withUrlParam("currentSchema", "customer")
            .withUsername("postgres");

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    // ------ POSITIVE TESTS -------

    // CREATE
    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/cleanup.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Create new customer. Positive test")
    @Test
    void createNewCustomer() throws Exception {
        // Given
        UUID guidOfManager = UUID.randomUUID();
        String email = "new-user@softspace.org";
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                "NAME",
                "SURNAME",
                "MIDDLE_NAME",
                email,
                "+79991234567",
                CustomerType.LEAD,
                guidOfManager,
                guidOfManager
        );

        // Execute and then
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCustomerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.guid").isNotEmpty())
                .andExpect(jsonPath("$.name").value(createCustomerRequest.name()))
                .andExpect(jsonPath("$.surname").value(createCustomerRequest.surname()))
                .andExpect(jsonPath("$.middleName").value(createCustomerRequest.middleName()))
                .andExpect(jsonPath("$.email").value(createCustomerRequest.email()))
                .andExpect(jsonPath("$.phone").value(createCustomerRequest.phone()))
                .andExpect(jsonPath("$.customerType").value(createCustomerRequest.customerType().name()))
                .andExpect(jsonPath("$.userProfileGuid").value(createCustomerRequest.userProfileGuid().toString()))
                .andExpect(jsonPath("$.responsibleManagerGuid").value(createCustomerRequest.responsibleManagerGuid().toString()));

        CustomerDbResult customerDbResult = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE email = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                email
        );

        Integer countNewCustomers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer WHERE email = ?",
                Integer.class,
                email
        );

        assertThat(countNewCustomers).isEqualTo(1);

        assertNotNull(customerDbResult);
        assertThat(customerDbResult.name()).isEqualTo(createCustomerRequest.name());
        assertThat(customerDbResult.surname()).isEqualTo(createCustomerRequest.surname());
        assertThat(customerDbResult.middleName()).isEqualTo(createCustomerRequest.middleName());
        assertThat(customerDbResult.phone()).isEqualTo(createCustomerRequest.phone());
        assertThat(customerDbResult.email()).isEqualTo(createCustomerRequest.email());
        assertThat(customerDbResult.customerType()).isEqualTo(createCustomerRequest.customerType());
        assertThat(customerDbResult.userProfileGuid()).isEqualTo(createCustomerRequest.userProfileGuid());
        assertThat(customerDbResult.responsibleManagerGuid()).isEqualTo(createCustomerRequest.responsibleManagerGuid());
        assertThat(customerDbResult.createdAt()).isNotNull();
        assertThat(customerDbResult.updatedAt()).isNotNull();
        assertThat(customerDbResult.deletedAt()).isNull();
    }

    // Get

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/insert.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/cleanup.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Find customer. Positive test")
    @Test
    void findCustomer() throws Exception {
        // Execute and then
        mockMvc.perform(get(BASE_URL + "/{guid}", CUSTOMER_GUID))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id").doesNotExist())
                .andExpect(jsonPath("guid").value(CUSTOMER_GUID.toString()));
    }

    // Get all

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/insert.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/cleanup.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Find all customers. Positive test")
    @Test
    void findAllCustomers() throws Exception {
        // Execute and then
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customers").isArray())
                .andExpect(jsonPath("$.customers").isNotEmpty())
                .andExpect(jsonPath("$.customers.length()").value(1))
                .andExpect(jsonPath("$.customers[0].id").doesNotExist())
                .andExpect(jsonPath("$.customers[0].guid").value(CUSTOMER_GUID.toString()));
    }

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Get empty customer list. Positive test")
    @Test
    void getEmptyCustomerList() throws Exception {

        Integer customerCountAfterDeleteRequest = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer",
                Integer.class
        );

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customers").isArray())
                .andExpect(jsonPath("$.customers").isEmpty());

        assertEquals(0, customerCountAfterDeleteRequest);
    }

    // Update

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/insert.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/cleanup.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Update customer. Positive test")
    @Test
    void updateCustomer() throws Exception {
        // Given
        String updatedName = "UPDATED NAME";
        String updatedSurname = "UPDATED SURNAME";
        String updatedMiddleName = "UPDATED MIDDLE_NAME";
        String updatedEmail = "updated-user@softspace.org";
        String updatedPhone = "+79997777777";
        CustomerType updatedCustomerType = CustomerType.CUSTOMER;
        UUID updatedGuidOfManager = UUID.randomUUID();

        String updateCustomerRequest = """
                {
                  "name": "%s",
                  "surname": "%s",
                  "middleName": "%s",
                  "email": "%s",
                  "phone": "%s",
                  "customerType": "%s",
                  "userProfileGuid": "%s",
                  "responsibleManagerGuid": "%s"
                }
                """.formatted(
                updatedName,
                updatedSurname,
                updatedMiddleName,
                updatedEmail,
                updatedPhone,
                updatedCustomerType,
                updatedGuidOfManager,
                updatedGuidOfManager
        );

        // Customer before update
        CustomerDbResult customerDbResultBeforeUpdate = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE guid = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                CUSTOMER_GUID
        );

        // Execute and then
        mockMvc.perform(patch(BASE_URL + "/{guid}", CUSTOMER_GUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateCustomerRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.guid").value(CUSTOMER_GUID.toString()))
                .andExpect(jsonPath("$.name").value(updatedName))
                .andExpect(jsonPath("$.surname").value(updatedSurname))
                .andExpect(jsonPath("$.middleName").value(updatedMiddleName))
                .andExpect(jsonPath("$.email").value(updatedEmail))
                .andExpect(jsonPath("$.phone").value(updatedPhone))
                .andExpect(jsonPath("$.customerType").value(updatedCustomerType.name()))
                .andExpect(jsonPath("$.userProfileGuid").value(updatedGuidOfManager.toString()))
                .andExpect(jsonPath("$.responsibleManagerGuid").value(updatedGuidOfManager.toString()));


        // Customer after update
        CustomerDbResult customerDbResultAfterUpdate = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE guid = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                CUSTOMER_GUID
        );

        // Customer exists before and after update.
        assertNotNull(customerDbResultBeforeUpdate);
        assertNotNull(customerDbResultAfterUpdate);

        // deletedAt field is empty.
        assertNull(customerDbResultBeforeUpdate.deletedAt());
        assertNull(customerDbResultAfterUpdate.deletedAt());

        // Deference objects.
        assertNotEquals(customerDbResultBeforeUpdate, customerDbResultAfterUpdate);

        // Unmodified createdAt fields and deference updatedAt fields.
        assertNotEquals(customerDbResultBeforeUpdate.updatedAt(), customerDbResultAfterUpdate.updatedAt());
        assertEquals(customerDbResultBeforeUpdate.createdAt(), customerDbResultAfterUpdate.createdAt());

        // Equals requested fields with result.
        assertEquals(updatedName, customerDbResultAfterUpdate.name());
        assertEquals(updatedSurname, customerDbResultAfterUpdate.surname());
        assertEquals(updatedMiddleName, customerDbResultAfterUpdate.middleName());
        assertEquals(updatedEmail, customerDbResultAfterUpdate.email());
        assertEquals(updatedPhone, customerDbResultAfterUpdate.phone());
        assertEquals(updatedCustomerType, customerDbResultAfterUpdate.customerType());
        assertEquals(updatedGuidOfManager, customerDbResultAfterUpdate.userProfileGuid());
        assertEquals(updatedGuidOfManager, customerDbResultAfterUpdate.responsibleManagerGuid());
    }

    // Delete

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/insert.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/cleanup.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("First soft delete customer. Positive test")
    @Test
    void softDeleteCustomer() throws Exception {
        CustomerDbResult customerDbResultBeforeDelete = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE GUID = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                CUSTOMER_GUID
        );

        mockMvc.perform(delete(BASE_URL + "/{guid}", CUSTOMER_GUID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        CustomerDbResult customerDbResulAfterDelete = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE GUID = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                CUSTOMER_GUID
        );

        assertNotNull(customerDbResultBeforeDelete);
        assertNotNull(customerDbResulAfterDelete);
        assertThat(customerDbResultBeforeDelete.createdAt()).isEqualTo(customerDbResulAfterDelete.createdAt());
        assertThat(customerDbResulAfterDelete.updatedAt()).isAfter(customerDbResultBeforeDelete.updatedAt());
        assertNull(customerDbResultBeforeDelete.deletedAt());
        assertNotNull(customerDbResulAfterDelete.deletedAt());
    }

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/insert.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/cleanup.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Get customer list without soft deleted customers. Positive test")
    @Test
    void getCustomerListWithoutSoftDeletedCustomer() throws Exception {

        mockMvc.perform(delete(BASE_URL + "/{guid}", CUSTOMER_GUID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        Integer customerCountAfterDeleteRequest = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer WHERE guid = ?",
                Integer.class,
                CUSTOMER_GUID
        );

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customers").isArray())
                .andExpect(jsonPath("$.customers").isEmpty());

        assertEquals(1, customerCountAfterDeleteRequest);
    }


    // ------ NEGATIVE TESTS -------




    // Get

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Get non-existent customer. Negative test")
    @Test
    void getNonExistentCustomer() throws Exception {

        mockMvc.perform(get(BASE_URL + "/{guid}", CUSTOMER_GUID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status").value("CUSTOMER_NOT_FOUND"));

        Integer customerCountAfterFind = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer",
                Integer.class
        );

        assertEquals(0, customerCountAfterFind);
    }


    // Delete

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/insert.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/cleanup.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Soft delete customer twice. Negative test")
    @Test
    void softDeleteCustomerTwice() throws Exception {
        CustomerDbResult customerDbResultBeforeDelete = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE GUID = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                CUSTOMER_GUID
        );

        mockMvc.perform(delete(BASE_URL + "/{guid}", CUSTOMER_GUID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(delete(BASE_URL + "/{guid}", CUSTOMER_GUID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status").value("CUSTOMER_NOT_FOUND"));

        CustomerDbResult customerDbResulAfterDelete = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE GUID = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                CUSTOMER_GUID
        );

        assertNotNull(customerDbResultBeforeDelete);
        assertNotNull(customerDbResulAfterDelete);
        assertThat(customerDbResultBeforeDelete.createdAt()).isEqualTo(customerDbResulAfterDelete.createdAt());
        assertThat(customerDbResulAfterDelete.updatedAt()).isAfter(customerDbResultBeforeDelete.updatedAt());
        assertNotNull(customerDbResulAfterDelete.deletedAt());
    }

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Soft delete not found customer. Negative test")
    @Test
    void softDeleteNotFoundCustomer() throws Exception {

        Integer customerCountBeforeDeleteRequest = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer WHERE guid = ?",
                Integer.class,
                CUSTOMER_GUID
        );

        mockMvc.perform(delete(BASE_URL + "/{guid}", CUSTOMER_GUID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status").value("CUSTOMER_NOT_FOUND"));

        Integer customerCountAfterDeleteRequest = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer WHERE guid = ?",
                Integer.class,
                CUSTOMER_GUID
        );

        assertEquals(0, customerCountBeforeDeleteRequest);
        assertEquals(0, customerCountAfterDeleteRequest);
    }

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/insert.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/cleanup.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Get soft deleted customer. Negative test")
    @Test
    void getSoftDeletedCustomer() throws Exception {

        mockMvc.perform(delete(BASE_URL + "/{guid}", CUSTOMER_GUID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        Integer customerCountAfterDeleteRequest = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer WHERE guid = ?",
                Integer.class,
                CUSTOMER_GUID
        );

        mockMvc.perform(get(BASE_URL + "/{guid}", CUSTOMER_GUID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status").value("CUSTOMER_NOT_FOUND"));

        assertEquals(1, customerCountAfterDeleteRequest);
    }

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/cleanup.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @DisplayName("Update soft deleted customer. Negative test")
    @Test
    void updateSoftDeletedCustomer() throws Exception {
        // Given
        UUID guidOfManager = UUID.randomUUID();
        String email = "new-user@softspace.org";
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                "NAME",
                "SURNAME",
                "MIDDLE_NAME",
                email,
                "+79991234567",
                CustomerType.LEAD,
                guidOfManager,
                guidOfManager
        );

        // Create new customer.
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCustomerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.guid").isNotEmpty());


        // Customer before update
        CustomerDbResult customerDbResultBeforeSoftDeleteAndUpdate = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE email = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                email
        );
        UUID customerGuid = customerDbResultBeforeSoftDeleteAndUpdate.guid();

        // Delete customer request.
        mockMvc.perform(delete(BASE_URL + "/{guid}", customerGuid))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Update customer request.
        String updatedName = "UPDATED NAME";
        String updatedSurname = "UPDATED SURNAME";
        String updatedMiddleName = "UPDATED MIDDLE_NAME";
        String updatedEmail = "updated-user@softspace.org";
        String updatedPhone = "+79997777777";
        CustomerType updatedCustomerType = CustomerType.CUSTOMER;
        UUID updatedGuidOfManager = UUID.randomUUID();

        String updateCustomerRequest = """
                {
                  "name": "%s",
                  "surname": "%s",
                  "middleName": "%s",
                  "email": "%s",
                  "phone": "%s",
                  "customerType": "%s",
                  "userProfileGuid": "%s",
                  "responsibleManagerGuid": "%s"
                }
                """.formatted(
                updatedName,
                updatedSurname,
                updatedMiddleName,
                updatedEmail,
                updatedPhone,
                updatedCustomerType,
                updatedGuidOfManager,
                updatedGuidOfManager
        );

        // Update deleted customer
        mockMvc.perform(patch(BASE_URL + "/{guid}", customerGuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateCustomerRequest))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status").value("CUSTOMER_NOT_FOUND"));


        // --- Then ---

        CustomerDbResult customerDbResultAfterSoftDeleteAndUpdate = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE guid = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                customerGuid
        );

        Integer countNewCustomers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer WHERE guid = ?",
                Integer.class,
                customerGuid
        );

        assertThat(countNewCustomers).isEqualTo(1);

        assertNotNull(customerDbResultBeforeSoftDeleteAndUpdate);
        assertNotNull(customerDbResultAfterSoftDeleteAndUpdate);
        assertThat(customerDbResultBeforeSoftDeleteAndUpdate.guid()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.guid());
        assertThat(customerDbResultBeforeSoftDeleteAndUpdate.name()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.name());
        assertThat(customerDbResultBeforeSoftDeleteAndUpdate.surname()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.surname());
        assertThat(customerDbResultBeforeSoftDeleteAndUpdate.middleName()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.middleName());
        assertThat(customerDbResultBeforeSoftDeleteAndUpdate.phone()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.phone());
        assertThat(customerDbResultBeforeSoftDeleteAndUpdate.email()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.email());
        assertThat(customerDbResultBeforeSoftDeleteAndUpdate.customerType()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.customerType());
        assertThat(customerDbResultBeforeSoftDeleteAndUpdate.userProfileGuid()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.userProfileGuid());
        assertThat(customerDbResultBeforeSoftDeleteAndUpdate.responsibleManagerGuid()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.responsibleManagerGuid());
        assertThat(customerDbResultBeforeSoftDeleteAndUpdate.createdAt()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.createdAt());
        assertThat(customerDbResultBeforeSoftDeleteAndUpdate.updatedAt()).isNotEqualTo(customerDbResultAfterSoftDeleteAndUpdate.updatedAt());
        assertThat(customerDbResultBeforeSoftDeleteAndUpdate.deletedAt()).isNull();
        assertThat(customerDbResultAfterSoftDeleteAndUpdate.deletedAt()).isNotNull();
    }
}
