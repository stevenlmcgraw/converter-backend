package com.slowdraw.converterbackend.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "roles")
public class Role {

    @Id
    Long id;

    private RoleName roleName;

}
