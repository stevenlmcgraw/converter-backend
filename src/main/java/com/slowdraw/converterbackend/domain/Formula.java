package com.slowdraw.converterbackend.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder(builderClassName = "FormulaBuilder", toBuilder = true)
@JsonDeserialize(builder = Formula.FormulaBuilder.class)
@Document(collection = "formulas")
public class Formula {

    @Id
    private String formulaName;

    private String formulaUrl;

    @JsonPOJOBuilder(withPrefix = "")
    public static class FormulaBuilder {

    }
}
