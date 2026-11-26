package org.softspace.customer.client;

import org.softspace.customer.client.dto.UserProfileClientResponse;
import org.softspace.customer.config.UserProfileFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "user-profile-client",
        url = "${clients.user-profile.base-url}",
        configuration = UserProfileFeignConfig.class
)
public interface UserProfileClient {

    @GetMapping("api/v1/users/{guid}")
    public UserProfileClientResponse getUserProfile(
            @PathVariable("guid") UUID guid
    );
}
