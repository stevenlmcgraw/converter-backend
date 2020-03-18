package com.slowdraw.converterbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ResultHistoryAdvice {

    @ResponseBody
    @ExceptionHandler(ResultHistoryException.class)
    public final ResponseEntity<ResultHistoryNotFoundResponse>
        resultHistoryNotFoundResponseResponseEntity(ResultHistoryException exception) {

        ResultHistoryNotFoundResponse response = new ResultHistoryNotFoundResponse(exception.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }
}
