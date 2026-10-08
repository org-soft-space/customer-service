package org.softspace.customer;

import org.junit.jupiter.api.Test;
import org.softspace.customer.repository.CustomerRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest
class CustomerServiceApplicationTests {

    @MockitoBean
    private CustomerRepository customerRepository;

	@Test
	void contextLoads() {
	}

}
