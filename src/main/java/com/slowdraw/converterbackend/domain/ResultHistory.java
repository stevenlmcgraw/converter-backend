package com.slowdraw.converterbackend.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "result_history")
public class ResultHistory {

    @Id
    private String id;

    private String username;

    private Map<String, Object> calculationAttributes = new HashMap<>();
}
