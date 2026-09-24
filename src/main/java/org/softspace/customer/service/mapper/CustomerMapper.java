package org.softspace.customer.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.softspace.customer.dto.customer.request.CreateCustomerRequest;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.dto.customer.response.SetCustomersResponse;
import org.softspace.customer.entity.CustomerEntity;
import org.springframework.context.annotation.Primary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Primary
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerMapper {

    // TODO: уточнить, будет ли id ignore здесь являться как доп защита,
    //  в случае если в dto добавят это поле.
    CustomerResponse customerEntityToCustomerResponse(CustomerEntity customerEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "guid", source = "customerGuid")
    @Mapping(target = "name", source = "createCustomerRequest.name")
    @Mapping(target = "surname", source = "createCustomerRequest.surname")
    @Mapping(target = "middleName", source = "createCustomerRequest.middleName")
    @Mapping(target = "email", source = "createCustomerRequest.email")
    @Mapping(target = "phone", source = "createCustomerRequest.phone")
    @Mapping(target = "customerType", source = "createCustomerRequest.customerType")
    @Mapping(target = "userProfileGuid", source = "createCustomerRequest.userProfileGuid")
    @Mapping(target = "responsibleManagerGuid", source = "createCustomerRequest.responsibleManagerGuid")
    @Mapping(target = "createdAt", source = "newTime")
    @Mapping(target = "updatedAt", source = "newTime")
    CustomerEntity createCustomerRequestToCustomerEntity(CreateCustomerRequest createCustomerRequest,
                                                         Instant newTime, UUID customerGuid);

    default SetCustomersResponse customerEntityListToSetCustomerResponse(List<CustomerEntity> customerEntityList) {
        List<CustomerResponse> customerMappedList = customerEntityList.stream().map(this::customerEntityToCustomerResponse).toList();
        return new SetCustomersResponse(customerMappedList);
    }
}
