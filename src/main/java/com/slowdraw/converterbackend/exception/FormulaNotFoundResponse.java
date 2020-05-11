package com.slowdraw.converterbackend.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FormulaNotFoundResponse {

    public String formulaNotFound;

    public FormulaNotFoundResponse(String formulaNotFound) {

        this.formulaNotFound = formulaNotFound;
    }
}
