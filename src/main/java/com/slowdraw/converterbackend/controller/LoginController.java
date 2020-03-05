package com.slowdraw.converterbackend.controller;

import com.slowdraw.converterbackend.domain.Role;
import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.payload.ApiResponse;
import com.slowdraw.converterbackend.payload.JwtAuthenticationResponse;
import com.slowdraw.converterbackend.payload.LoginRequest;
import com.slowdraw.converterbackend.payload.RegisterUsernameRequest;
import com.slowdraw.converterbackend.repository.RoleRepository;
import com.slowdraw.converterbackend.repository.UserRepository;
import com.slowdraw.converterbackend.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private AuthenticationManager authenticationManager;

    private UserRepository userRepository;

    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    private JwtTokenProvider jwtTokenProvider;

    public LoginController(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(),
                                loginRequest.getPassword()
                        )
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerSiteUser(
            @Valid @RequestBody RegisterUsernameRequest registerUsernameRequest) {

        if(userRepository.existsById(registerUsernameRequest.getUsername())) {
            return new ResponseEntity(
                    new ApiResponse(false, "Username is already in use."),
                    HttpStatus.BAD_REQUEST);
        }

        SiteUser user = SiteUser.builder()
                .username(registerUsernameRequest.getUsername())
                .password(registerUsernameRequest.getPassword())
                .email(registerUsernameRequest.getEmail())
                .build();

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findByRoleName("ROLE_USER");

        user.setRoles(Collections.singleton(role));

        SiteUser persistedUser = userRepository.save(user);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/**")
                .buildAndExpand(persistedUser.getUsername()).toUri();

        return ResponseEntity.created(uri)
                .body(new ApiResponse(true, "Site user successfully registered."));
    }
}
