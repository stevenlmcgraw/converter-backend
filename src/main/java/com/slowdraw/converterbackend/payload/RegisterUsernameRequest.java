package com.slowdraw.converterbackend.payload;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RegisterUsernameRequest {

    @NotBlank
    @Size(min = 6, max = 25)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 30)
    private String password;
}
