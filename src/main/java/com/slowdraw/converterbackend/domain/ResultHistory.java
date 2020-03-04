package com.slowdraw.converterbackend.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Builder(builderClassName = "ResultHistoryBuilder", toBuilder = true)
@JsonDeserialize(builder = ResultHistory.ResultHistoryBuilder.class)
@Document(collection = "result_history")
public class ResultHistory {

    @Id
    private String id;

    private String username;

    private Map<String, Object> calculationAttributes;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ResultHistoryBuilder {

    }
}
