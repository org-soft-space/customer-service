package org.softspace.customer.dtotest;

import org.softspace.customer.dto.customer.request.CreateCustomerRequest;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.enums.CustomerType;

import java.time.Instant;
import java.util.UUID;

public class DtoCustomerTestBuilder {

    public static final UUID GUID = UUID.randomUUID();

    public static final String NAME = "Name";

    public static final String SURNAME = "Surname";

    public static final String MIDDLE_NAME = "MiddleName";

    public static final String EMAIL = "client@softspace.org";

    public static final String PHONE = "+79991234567";

    public static final CustomerType CUSTOMER_TYPE = CustomerType.LEAD;

    public static final UUID USER_PROFILE_GUID = UUID.randomUUID();

    public static final UUID RESPONSIBLE_MANAGER_GUID = UUID.randomUUID();

    public static final Instant TIME = Instant.now();

    public static CreateCustomerRequest getCreateCustomerRequest() {
        return new CreateCustomerRequest(
                NAME,
                SURNAME,
                MIDDLE_NAME,
                EMAIL,
                PHONE,
                CUSTOMER_TYPE,
                USER_PROFILE_GUID,
                RESPONSIBLE_MANAGER_GUID
        );
    }

    public static CustomerResponse getCustomerResponse() {
        return new CustomerResponse(
                GUID,
                NAME,
                SURNAME,
                MIDDLE_NAME,
                EMAIL,
                PHONE,
                CUSTOMER_TYPE,
                USER_PROFILE_GUID,
                RESPONSIBLE_MANAGER_GUID,
                TIME,
                TIME
        );
    }

}
