package com.slowdraw.converterbackend.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UsernameAvailability {

    private Boolean available;
}
