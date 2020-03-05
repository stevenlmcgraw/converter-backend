package com.slowdraw.converterbackend.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNotFoundResponse {

    public String userNotFound;

    public UserNotFoundResponse(String userNotFound) {
        this.userNotFound = userNotFound;
    }
}
