package org.softspace.customer.config;

import feign.codec.ErrorDecoder;
import org.softspace.customer.client.UserProfileErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserProfileFeignConfig {
    @Bean
    public ErrorDecoder getDecoder() {
        return new UserProfileErrorDecoder();
    }
}
