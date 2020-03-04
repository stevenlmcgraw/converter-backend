package com.slowdraw.converterbackend.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultHistoryNotFoundResponse {

    public String resultHistoryNotFound;

    public ResultHistoryNotFoundResponse(String resultHistoryNotFound) {
        this.resultHistoryNotFound = resultHistoryNotFound;
    }
}
