package com.slowdraw.converterbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class FormulaAdvice {

    @ResponseBody
    @ExceptionHandler(FormulaException.class)
    public final ResponseEntity<FormulaNotFoundResponse>
        formulaNotFoundResponseResponseEntity(FormulaException exceptMe) {

        FormulaNotFoundResponse response = new FormulaNotFoundResponse(exceptMe.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
}
