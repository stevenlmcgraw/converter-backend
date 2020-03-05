package com.slowdraw.converterbackend.exception;

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
    resultHistoryNotFoundResponseResponseEntity(UserNotFoundResponse exception) {

        UserNotFoundResponse response = new UserNotFoundResponse(exception.getUserNotFound());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
