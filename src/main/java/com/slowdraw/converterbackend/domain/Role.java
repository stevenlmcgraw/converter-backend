package com.slowdraw.converterbackend.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "roles")
public class Role {

    @Id
    String username;

    private String roleName;
}
