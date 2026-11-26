package org.softspace.customer.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.softspace.customer.dto.customer.request.CreateCustomerRequest;
import org.softspace.customer.dto.customer.response.CustomerListResponse;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.dto.error.ErrorResponse;
import org.softspace.customer.enums.CustomerType;
import org.softspace.customer.exception.error.ErrorCode;
import org.softspace.customer.result.ObjectBuilder;
import org.softspace.customer.result.object.CustomerDbResult;
import org.softspace.customer.service.validation.CustomerValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomerIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

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
    void createNewCustomer() {
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
        ResponseEntity<CustomerResponse> customerResponse = testRestTemplate.postForEntity(BASE_URL, createCustomerRequest, CustomerResponse.class);
        CustomerResponse body = customerResponse.getBody();
        HttpHeaders headers = customerResponse.getHeaders();

        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertThat(customerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assertNotNull(body);
        assertNotNull(body.guid());
        assertEquals(createCustomerRequest.name(), body.name());
        assertEquals(createCustomerRequest.surname(), body.surname());
        assertEquals(createCustomerRequest.middleName(), body.middleName());
        assertEquals(createCustomerRequest.email(), body.email());
        assertEquals(createCustomerRequest.phone(), body.phone());
        assertEquals(createCustomerRequest.customerType(), body.customerType());
        assertEquals(createCustomerRequest.userProfileGuid(), body.userProfileGuid());
        assertEquals(createCustomerRequest.responsibleManagerGuid(), body.responsibleManagerGuid());

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

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/cleanup.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @ParameterizedTest(name = "name={0}, surname={1}, middleName={2}, email={3}, phone={4}, customerType={5}, userProfileGuid={6}, responsibleManagerGuid={7}")
    @MethodSource("paramsForNewCustomer")
    void createNewCustomerWithParams(
            String name,
            String surname,
            String middleName,
            String email,
            String phone,
            CustomerType customerType,
            UUID userProfileGuid,
            UUID responsibleManagerGuid
    ) {
        // Given
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                name,
                surname,
                middleName,
                email,
                phone,
                customerType,
                userProfileGuid,
                responsibleManagerGuid
        );

        // Execute and then
        ResponseEntity<CustomerResponse> response = testRestTemplate.postForEntity(BASE_URL, createCustomerRequest, CustomerResponse.class);

        CustomerResponse body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertNotNull(body);
        assertNotNull(body.guid());

        assertEquals(createCustomerRequest.name(), body.name());
        assertEquals(createCustomerRequest.surname(), body.surname());
        assertEquals(createCustomerRequest.middleName(), body.middleName());
        assertEquals(createCustomerRequest.email(), body.email());
        assertEquals(createCustomerRequest.phone(), body.phone());
        assertEquals(createCustomerRequest.customerType(), body.customerType());
        assertEquals(createCustomerRequest.userProfileGuid(), body.userProfileGuid());

        CustomerDbResult customerDbResult = jdbcTemplate.queryForObject(
                "SELECT * FROM customer",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs)
        );

        Integer countNewCustomers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer",
                Integer.class
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

    private static Stream<Arguments> paramsForNewCustomer() {
        UUID guidOfManager = UUID.randomUUID();
        String email = "new-user@softspace.org";
        String phone = "+79991234567";
        return Stream.of(
                Arguments.of(
                        "NAME",
                        "SURNAME",
                        "MIDDLE_NAME",
                        email,
                        phone,
                        CustomerType.LEAD,
                        guidOfManager,
                        guidOfManager
                ),
                Arguments.of(
                        "NAME",
                        null,
                        null,
                        null,
                        phone,
                        CustomerType.CUSTOMER,
                        null,
                        null
                ),
                Arguments.of(
                        "NAME",
                        null,
                        null,
                        email,
                        null,
                        CustomerType.CUSTOMER,
                        null,
                        null
                )
        );
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
        ResponseEntity<String> response = testRestTemplate.getForEntity(BASE_URL + "/{guid}", String.class, CUSTOMER_GUID);
        HttpHeaders headers = response.getHeaders();
        String actualResponse = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertNotNull(actualResponse);

        String expectedJson = new ClassPathResource("responses/customerResponse.json").getContentAsString(StandardCharsets.UTF_8);
        JSONAssert.assertEquals(expectedJson, actualResponse, true);
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
    void findAllCustomers() {
        // Execute and then
        ResponseEntity<CustomerListResponse> response = testRestTemplate.getForEntity(BASE_URL, CustomerListResponse.class);
        HttpHeaders headers = response.getHeaders();
        CustomerListResponse body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertNotNull(body);
        assertNotNull(body.customers());
        assertFalse(body.customers().isEmpty());
        assertThat(body.customers().size()).isEqualTo(1);
        assertThat(body.customers().getFirst().guid()).isEqualTo(CUSTOMER_GUID);

//                .andExpect(jsonPath("$.customers[0].id").doesNotExist())
    }

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Get empty customer list. Positive test")
    @Test
    void getEmptyCustomerList() {

        Integer customerCountAfterDeleteRequest = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer",
                Integer.class
        );

        ResponseEntity<CustomerListResponse> response = testRestTemplate.getForEntity(BASE_URL, CustomerListResponse.class);
        HttpHeaders headers = response.getHeaders();
        CustomerListResponse body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(headers.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertNotNull(body);
        assertNotNull(body.customers());
        assertTrue(body.customers().isEmpty());

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
    void updateCustomer() {
        // Given
        String updatedName = "UPDATED NAME";
        String updatedSurname = "UPDATED SURNAME";
        String updatedMiddleName = "UPDATED MIDDLE_NAME";
        String updatedEmail = "updated-user@softspace.org";
        String updatedPhone = "+79997777777";
        CustomerType updatedCustomerType = CustomerType.CUSTOMER;
        UUID updatedGuidOfUserProfile = UUID.randomUUID();
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
                updatedGuidOfUserProfile,
                updatedGuidOfManager
        );

        // Customer before update
        CustomerDbResult customerDbResultBeforeUpdate = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE guid = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                CUSTOMER_GUID
        );

        // Execute and then
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<CustomerResponse> response = testRestTemplate.exchange(
                BASE_URL + "/{guid}",
                HttpMethod.PATCH,
                new HttpEntity<>(updateCustomerRequest, httpHeaders),
                CustomerResponse.class,
                CUSTOMER_GUID
        );
        CustomerResponse body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(headers.getContentType());
        assertTrue(headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertNotNull(body);
        assertThat(body.guid()).isEqualTo(CUSTOMER_GUID);
        assertThat(body.name()).isEqualTo(updatedName);
        assertThat(body.surname()).isEqualTo(updatedSurname);
        assertThat(body.middleName()).isEqualTo(updatedMiddleName);
        assertThat(body.email()).isEqualTo(updatedEmail);
        assertThat(body.phone()).isEqualTo(updatedPhone);
        assertThat(body.customerType()).isEqualTo(updatedCustomerType);
        assertThat(body.userProfileGuid()).isEqualTo(updatedGuidOfUserProfile);
        assertThat(body.responsibleManagerGuid()).isEqualTo(updatedGuidOfManager);


        // TODO: JSONAssert.assertEquals() -- для тестирования с JSON файлами.
        // TODO: если надо получить ответ до десериализации в объект, то можно воспользоваться JsonNode.class.
        //  String expectedJson = new ClassPathResource("responses/example.json").getContentAsString(StandardCharsets.UTF_8);


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
        assertEquals(updatedGuidOfUserProfile, customerDbResultAfterUpdate.userProfileGuid());
        assertEquals(updatedGuidOfManager, customerDbResultAfterUpdate.responsibleManagerGuid());
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
    @ParameterizedTest(name = "fieldName={0}, value={1}")
    @MethodSource("parametersForUpdate")
    void updateEachFieldOfCustomerWithParams(
            String fieldName,
            Object value
    ) {

        String email = "new-user@softspace.org";
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                "NAME",
                "SURNAME",
                "MIDDLE_NAME",
                email,
                "+79991234567",
                CustomerType.LEAD,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        // Customer before update
        CustomerDbResult customerDbResultBeforeUpdate = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE guid = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                CUSTOMER_GUID
        );

        ObjectNode expectedCustomerTree = objectMapper.<ObjectNode>valueToTree(customerDbResultBeforeUpdate);
        JsonNode valueNode = objectMapper.valueToTree(value);

        // Fields are difference.
        assertThat(expectedCustomerTree.path(fieldName).asText()).isNotEqualTo(valueNode.toString());

        // Change a field.
        expectedCustomerTree.set(fieldName, valueNode.deepCopy());

        ObjectNode request = objectMapper.createObjectNode();
        request.set(fieldName, valueNode);

        // Execute and then
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<CustomerResponse> response = testRestTemplate.exchange(
                BASE_URL + "/{guid}",
                HttpMethod.PATCH,
                new HttpEntity<>(request, httpHeaders),
                CustomerResponse.class,
                CUSTOMER_GUID
        );
        CustomerResponse body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(headers.getContentType());
        assertTrue(headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertNotNull(body);
        assertThat(body.guid()).isEqualTo(CUSTOMER_GUID);

        // Customer after update
        CustomerDbResult customerDbResultAfterUpdate = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE guid = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                CUSTOMER_GUID
        );

        // Prepare JSON objects.
        ObjectNode responseBodyTree = objectMapper.<ObjectNode>valueToTree(body);
        ObjectNode actualCustomerTree = objectMapper.<ObjectNode>valueToTree(customerDbResultAfterUpdate);

        expectedCustomerTree.remove("updatedAt");
        expectedCustomerTree.remove("deletedAt");
        actualCustomerTree.remove("deletedAt");
        responseBodyTree.remove("updatedAt");
        actualCustomerTree.remove("updatedAt");

        assertThat(responseBodyTree).isEqualTo(actualCustomerTree);
        assertThat(expectedCustomerTree).isEqualTo(actualCustomerTree);

        // Assert updated value.
        assertThat(actualCustomerTree.path(fieldName)).isEqualTo(valueNode);

        // Customer exists before and after update.
        assertNotNull(customerDbResultBeforeUpdate);
        assertNotNull(customerDbResultAfterUpdate);

        // deletedAt field is empty.
        assertNull(customerDbResultBeforeUpdate.deletedAt());
        assertNull(customerDbResultAfterUpdate.deletedAt());

        // Unmodified createdAt fields and deference updatedAt fields.
        assertNotEquals(customerDbResultBeforeUpdate.updatedAt(), customerDbResultAfterUpdate.updatedAt());
        assertEquals(customerDbResultBeforeUpdate.createdAt(), customerDbResultAfterUpdate.createdAt());
    }

    private static Stream<Arguments> parametersForUpdate() {
        String updatedEmail = "updated-user@softspace.org";
        String updatedPhone = "+79997777777";
        return Stream.of(
                Arguments.of("name", "Updated name"),
                Arguments.of("surname", "Updated surname"),
                Arguments.of("middleName", "Updated middleName"),
                Arguments.of("email", updatedEmail),
                Arguments.of("phone", updatedPhone),
                Arguments.of("customerType", CustomerType.CUSTOMER.name()),
                Arguments.of("userProfileGuid", UUID.randomUUID().toString()),
                Arguments.of("responsibleManagerGuid", UUID.randomUUID().toString()),
                Arguments.of("surname", null),
                Arguments.of("middleName", null),
                Arguments.of("email", null),
                Arguments.of("phone", null),
                Arguments.of("userProfileGuid", null),
                Arguments.of("responsibleManagerGuid", null)
        );
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
    void softDeleteCustomer() {
        CustomerDbResult customerDbResultBeforeDelete = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE GUID = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                CUSTOMER_GUID
        );

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                BASE_URL + "/{guid}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class,
                CUSTOMER_GUID
        );
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertNull(responseEntity.getBody());

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
    void getCustomerListWithoutSoftDeletedCustomer() {

        testRestTemplate.delete(BASE_URL + "/{guid}", CUSTOMER_GUID);

        Integer customerCountAfterDeleteRequest = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer WHERE guid = ?",
                Integer.class,
                CUSTOMER_GUID
        );

        ResponseEntity<CustomerListResponse> response = testRestTemplate.getForEntity(BASE_URL, CustomerListResponse.class);
        CustomerListResponse body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotNull(headers.getContentType());
        assertTrue(headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertNotNull(body);
        assertNotNull(body.customers());
        assertTrue(body.customers().isEmpty());

        assertEquals(1, customerCountAfterDeleteRequest);
    }


    // ------ NEGATIVE TESTS -------

    // Create

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Create new customer without connects. Negative test")
    @Test
    void createNewCustomerWithoutConnects() {
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                "NAME",
                "SURNAME",
                "MIDDLE_NAME",
                null,
                null,
                CustomerType.LEAD,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity(BASE_URL, createCustomerRequest, ErrorResponse.class);
        ErrorResponse body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertNotNull(headers.getContentType());
        assertTrue(headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertNotNull(body);

        assertThat(body.status()).isEqualTo(ErrorCode.VALIDATION_ERROR.name());
        assertThat(body.message()).isEqualTo("Must have one or both field to contact.");
        assertThat(body.details().get("fieldName")).isEqualTo("email or phone");

        Integer customerCountAfterFind = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer",
                Integer.class
        );

        assertEquals(0, customerCountAfterFind);
    }

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Create new customer with empty connects. Negative test")
    @Test
    void createNewCustomerWithEmptyConnects() {
        CreateCustomerRequest createCustomerRequest = new CreateCustomerRequest(
                "NAME",
                "SURNAME",
                "MIDDLE_NAME",
                "",
                "",
                CustomerType.LEAD,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity(BASE_URL, createCustomerRequest, ErrorResponse.class);
        ErrorResponse body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertNotNull(headers.getContentType());
        assertTrue(headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertNotNull(body);

        assertThat(body.status()).isEqualTo(ErrorCode.VALIDATION_ERROR.name());

        Integer customerCountAfterFind = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer",
                Integer.class
        );

        assertEquals(0, customerCountAfterFind);
    }

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Create new customer with unknown customer type. Negative test")
    @Test
    void createNewCustomerWithUnknownCustomerType() {
        // Given
        String createCustomerRequest = """
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
                "NAME",
                "SURNAME",
                "MIDDLE_NAME",
                "user@softspace.org",
                "+79997777777",
                "UnknownCustomerType",
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        // Execute and then
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ErrorResponse> response = testRestTemplate.postForEntity(
                BASE_URL,
                new HttpEntity<>(createCustomerRequest, httpHeaders),
                ErrorResponse.class
        );
        ErrorResponse body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertNotNull(headers.getContentType());
        assertTrue(headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertNotNull(body);

        assertThat(body.status()).isEqualTo(ErrorCode.FIELD_INVALID.name());
        assertThat(body.details().get("fieldName")).isEqualTo("customerType");

        Integer customerCountAfterFind = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer",
                Integer.class
        );

        assertEquals(0, customerCountAfterFind);
    }

    // Get

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @DisplayName("Get non-existent customer. Negative test")
    @Test
    void getNonExistentCustomer() {

        ResponseEntity<ErrorResponse> response = testRestTemplate.getForEntity(BASE_URL + "/{guid}", ErrorResponse.class, CUSTOMER_GUID);
        ErrorResponse body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertNotNull(headers.getContentType());
        assertTrue(headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertNotNull(body);

        assertThat(body.status()).isEqualTo(ErrorCode.CUSTOMER_NOT_FOUND.name());

        Integer customerCountAfterFind = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer",
                Integer.class
        );

        assertEquals(0, customerCountAfterFind);
    }

    // Update

    @Sql(
            scripts = "classpath:sql/customer/truncate.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:sql/customer/cleanup.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    @ParameterizedTest(name = "fieldNameToCreate={0}, valueToCreate={1}, fieldNameToUpdate={2}, valueToUpdate={3}")
    @MethodSource("parametersForContactsUpdate")
    void updateContactFieldsOfCustomerWithParams(
            Object emailToCreate,
            Object emailToUpdate,
            Object phoneToCreate,
            Object phoneToUpdate
    ) {

        ObjectNode createCustomerRequest = objectMapper.createObjectNode();
        createCustomerRequest.put("name", "Name");
        createCustomerRequest.set("customerType", objectMapper.valueToTree(CustomerType.LEAD));
        createCustomerRequest.set("email", objectMapper.valueToTree(emailToCreate));
        createCustomerRequest.set("phone", objectMapper.valueToTree(phoneToCreate));

        // Execute and then
        ResponseEntity<CustomerResponse> customerResponse = testRestTemplate.postForEntity(
                BASE_URL,
                createCustomerRequest,
                CustomerResponse.class
        );

        assertThat(customerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID customerGuid = customerResponse.getBody().guid();


        // Customer before update
        Integer customerCountBeforeUpdate = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer",
                Integer.class
        );

        ObjectNode request = objectMapper.createObjectNode();
        request.set("email", objectMapper.valueToTree(emailToUpdate));
        request.set("phone", objectMapper.valueToTree(phoneToUpdate));

        // Execute and then
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
                BASE_URL + "/{guid}",
                HttpMethod.PATCH,
                new HttpEntity<>(request, httpHeaders),
                ErrorResponse.class,
                customerGuid
        );
        ErrorResponse body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertNotNull(headers.getContentType());
        assertTrue(headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertNotNull(body);
        assertThat(body.message()).isEqualTo(CustomerValidator.NO_ONE_FIELD_FOR_CONNECTION);
        assertThat(body.status()).isEqualTo("VALIDATION_ERROR");

        // Customer after update
        Integer customerCountAfterUpdate = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer",
                Integer.class
        );

        // TODO: Переделать и сравнить объекты до и после update. Удалить лишнее.
//        assertThat(customerCountBeforeUpdate).isEqualTo(1);
//        assertThat(customerCountAfterUpdate).isEqualTo(1);
    }

    private static Stream<Arguments> parametersForContactsUpdate() {
        return Stream.of(
                Arguments.of("user@softspace.org", "", null, null),
                Arguments.of("user@softspace.org", null, null, null),
                Arguments.of(null, null, "+79997777777", null)
        );
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
    void softDeleteCustomerTwice() {
        CustomerDbResult customerDbResultBeforeDelete = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE GUID = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                CUSTOMER_GUID
        );
        ResponseEntity<Void> firstDeletedResponse = testRestTemplate.exchange(
                BASE_URL + "/{guid}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class,
                CUSTOMER_GUID
        );
        assertThat(firstDeletedResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertNull(firstDeletedResponse.getBody());

        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
                BASE_URL + "/{guid}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                ErrorResponse.class,
                CUSTOMER_GUID
        );
        ErrorResponse body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertNotNull(headers.getContentType());
        assertTrue(headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertNotNull(body);

        assertThat(body.status()).isEqualTo(ErrorCode.CUSTOMER_NOT_FOUND.name());

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
    void softDeleteNotFoundCustomer() {

        Integer customerCountBeforeDeleteRequest = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer WHERE guid = ?",
                Integer.class,
                CUSTOMER_GUID
        );

        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
                BASE_URL + "/{guid}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                ErrorResponse.class,
                CUSTOMER_GUID
        );
        ErrorResponse body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertNotNull(headers.getContentType());
        assertTrue(headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertNotNull(body);

        assertThat(body.status()).isEqualTo(ErrorCode.CUSTOMER_NOT_FOUND.name());

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
    void getSoftDeletedCustomer() {

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                BASE_URL + "/{guid}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class,
                CUSTOMER_GUID
        );
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Integer customerCountAfterDeleteRequest = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM customer WHERE guid = ?",
                Integer.class,
                CUSTOMER_GUID
        );

        ResponseEntity<ErrorResponse> response = testRestTemplate.getForEntity(BASE_URL + "/{guid}", ErrorResponse.class, CUSTOMER_GUID);

        ErrorResponse body = response.getBody();
        HttpHeaders headers = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertNotNull(headers.getContentType());
        assertTrue(headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertNotNull(body);

        assertThat(body.status()).isEqualTo(ErrorCode.CUSTOMER_NOT_FOUND.name());

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
    void updateSoftDeletedCustomer() {
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
        ResponseEntity<CustomerResponse> createdCustomerResponse = testRestTemplate.postForEntity(BASE_URL, createCustomerRequest, CustomerResponse.class);
        CustomerResponse body = createdCustomerResponse.getBody();
        HttpHeaders headers = createdCustomerResponse.getHeaders();

        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
        assertThat(createdCustomerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertNotNull(body);
        assertNotNull(body.guid());

        UUID customerGuid = body.guid();

        // Delete customer request.
        ResponseEntity<Void> responseEntity = testRestTemplate.exchange(
                BASE_URL + "/{guid}",
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class,
                customerGuid
        );
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Customer before update
        CustomerDbResult customerDbResultBeforeUpdate = jdbcTemplate.queryForObject(
                "SELECT * FROM customer WHERE guid = ?",
                (ResultSet rs, int num) -> ObjectBuilder.getCustomerDbResult(rs),
                customerGuid
        );

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
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(
                BASE_URL + "/{guid}",
                HttpMethod.PATCH,
                new HttpEntity<>(updateCustomerRequest, httpHeaders),
                ErrorResponse.class,
                customerGuid
        );
        ErrorResponse errorBody = response.getBody();
        HttpHeaders errorHeaders = response.getHeaders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertNotNull(errorHeaders.getContentType());
        assertTrue(errorHeaders.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON));
        assertNotNull(errorBody);
        assertThat(errorBody.status()).isEqualTo(ErrorCode.CUSTOMER_NOT_FOUND.name());

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

        assertNotNull(customerDbResultBeforeUpdate);
        assertNotNull(customerDbResultAfterSoftDeleteAndUpdate);
        assertThat(customerDbResultBeforeUpdate.guid()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.guid());
        assertThat(customerDbResultBeforeUpdate.name()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.name());
        assertThat(customerDbResultBeforeUpdate.surname()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.surname());
        assertThat(customerDbResultBeforeUpdate.middleName()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.middleName());
        assertThat(customerDbResultBeforeUpdate.phone()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.phone());
        assertThat(customerDbResultBeforeUpdate.email()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.email());
        assertThat(customerDbResultBeforeUpdate.customerType()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.customerType());
        assertThat(customerDbResultBeforeUpdate.userProfileGuid()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.userProfileGuid());
        assertThat(customerDbResultBeforeUpdate.responsibleManagerGuid()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.responsibleManagerGuid());
        assertThat(customerDbResultBeforeUpdate.createdAt()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.createdAt());
        assertThat(customerDbResultBeforeUpdate.updatedAt()).isEqualTo(customerDbResultAfterSoftDeleteAndUpdate.updatedAt());
        assertThat(customerDbResultBeforeUpdate.deletedAt()).isNotNull();
        assertThat(customerDbResultAfterSoftDeleteAndUpdate.deletedAt()).isNotNull();
    }
}
