package com.slowdraw.converterbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class FormulaNotFoundResponse {

    public String formulaNotFound;

    public FormulaNotFoundResponse(String formulaNotFound) {

        this.formulaNotFound = formulaNotFound;
    }
}
