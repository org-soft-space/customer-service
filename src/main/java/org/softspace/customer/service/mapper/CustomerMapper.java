package org.softspace.customer.service.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.softspace.customer.dto.customer.request.CreateCustomerRequest;
import org.softspace.customer.dto.customer.response.CustomerResponse;
import org.softspace.customer.entity.CustomerEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CustomerMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "guid", source = "customerEntity.guid")
    @Mapping(target = "name", source = "customerEntity.name")
    @Mapping(target = "surname", source = "customerEntity.surname")
    @Mapping(target = "middleName", source = "customerEntity.middleName")
    @Mapping(target = "email", source = "customerEntity.email")
    @Mapping(target = "phone", source = "customerEntity.phone")
    @Mapping(target = "customerType", source = "customerEntity.customerType")
    @Mapping(target = "userProfileGuid", source = "customerEntity.userProfileGuid")
    @Mapping(target = "responsibleManagerGuid", source = "customerEntity.responsibleManagerGuid")
    @Mapping(target = "createdAt", source = "customerEntity.createdAt")
    @Mapping(target = "updatedAt", source = "customerEntity.updatedAt")
    CustomerResponse customerEntityToCustomerResponse(CustomerEntity customerEntity);

    @BeanMapping(ignoreByDefault = true)
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

    List<CustomerResponse>  customerEntityListToCustomerResponseList(List<CustomerEntity> customerEntityList);
}
