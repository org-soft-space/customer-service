package org.softspace.customer.client.validator;

import lombok.RequiredArgsConstructor;
import org.softspace.customer.client.UserProfileClient;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserProfileValidator {

    private final UserProfileClient userProfileClient;

    public void validateUserProfileByGuid(UUID guid) {

    }
}
