package org.softspace.customer.dto.customer.response;

import java.util.List;

public record SetCustomersResponse(
        List<CustomerResponse> setCustomers
) {
}
