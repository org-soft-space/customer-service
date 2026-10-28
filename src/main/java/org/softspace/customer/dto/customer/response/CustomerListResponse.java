package org.softspace.customer.dto.customer.response;

import java.util.List;

public record CustomerListResponse(
        List<CustomerResponse> customers
) {
}
