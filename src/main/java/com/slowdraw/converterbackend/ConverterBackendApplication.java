package com.slowdraw.converterbackend;

import com.slowdraw.converterbackend.domain.Role;
import com.slowdraw.converterbackend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ConverterBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConverterBackendApplication.class, args);
    }

}


