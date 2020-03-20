package com.slowdraw.converterbackend.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

@Data
@Builder(builderClassName = "UserBuilder", toBuilder = true)
@JsonDeserialize(builder = SiteUser.UserBuilder.class)
@Document(collection = "users")
public class SiteUser {

    @Id
    private String username;

    private String password;

    private String email;

    @DBRef
    private Set<Formula> favoritesSet;

    @DBRef
    private Set<Role> roles;

    //helper function
    public Set<Formula> addFormulaToFavoritesSet(Formula formula) {
        this.favoritesSet.add(formula);

        return this.favoritesSet;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserBuilder {

    }
}
