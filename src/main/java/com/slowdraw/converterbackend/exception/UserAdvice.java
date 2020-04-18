package com.slowdraw.converterbackend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class UserAdvice {

    @ResponseBody
    @ExceptionHandler(UserException.class)
    public final ResponseEntity<UserNotFoundResponse>
    userNotFoundResponseResponseEntity(UserException exception) {

        UserNotFoundResponse response = new UserNotFoundResponse(exception.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
