package com.slowdraw.converterbackend.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultHistoryPayload {

    private String id;

    private String username;

    private String title;

    private String message;

    private Date entryDate;

    private Map<String, Object> calculationAttributes;
}
