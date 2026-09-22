package org.softspace.customer.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.softspace.customer.dto.customer.request.CreateCustomerRequest;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.dto.customer.response.SetCustomersResponse;
import org.softspace.customer.entity.CustomerEntity;
import org.softspace.customer.enums.CustomerType;
import org.springframework.context.annotation.Primary;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Primary
@Mapper(componentModel = "spring")
public interface CustomerMapper {

    // TODO: уточнить, будет ли id ignore здесь являться как доп защита,
    //  в случае если в dto добавят это поле.
//    @Mapping(target = "id", ignore = true)
    CustomerResponse customerEntityToCustomerResponse(CustomerEntity customerEntity);

    // TODO: уточнить, поля userProfileGuid, responsibleManagerGuid - должны ли быть доступны для изменения
    //  на этапе создани кастомера.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "guid", source = "customerGuid")
    @Mapping(target = "name", source = "createCustomerRequest.name")
    @Mapping(target = "surname", source = "createCustomerRequest.surname")
    @Mapping(target = "middleName", source = "createCustomerRequest.middleName")
    @Mapping(target = "email", source = "createCustomerRequest.email")
    @Mapping(target = "phone", source = "createCustomerRequest.phone")
    @Mapping(target = "customerType", source = "customerType")
    @Mapping(target = "createdAt", source = "newTime")
    @Mapping(target = "updatedAt", source = "newTime")
    CustomerEntity createCustomerRequestToCustomerEntity(CreateCustomerRequest createCustomerRequest,
                                                         Instant newTime, UUID customerGuid,
                                                         CustomerType customerType);

    default SetCustomersResponse customerEntityListToSetCustomerResponse(List<CustomerEntity> customerEntityList) {
        List<CustomerResponse> customerMappedList = customerEntityList.stream().map(this::customerEntityToCustomerResponse).toList();
        return new SetCustomersResponse(customerMappedList);
    }
}
