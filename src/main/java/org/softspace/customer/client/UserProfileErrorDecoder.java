package org.softspace.customer.client;

import feign.Response;
import feign.codec.ErrorDecoder;

public class UserProfileErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String s, Response response) {
        int status = response.status();

        return null;
    }
}
