package com.slowdraw.converterbackend.controller;

import com.slowdraw.converterbackend.domain.Role;
import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.payload.*;
import com.slowdraw.converterbackend.repository.RoleRepository;
import com.slowdraw.converterbackend.repository.UserRepository;
import com.slowdraw.converterbackend.security.CurrentSiteUser;
import com.slowdraw.converterbackend.security.JwtTokenProvider;
import com.slowdraw.converterbackend.security.UserPrincipal;
import com.slowdraw.converterbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    private AuthenticationManager authenticationManager;

    private UserService userService;

    private UserRepository userRepository;

    private RoleRepository roleRepository;

    private PasswordEncoder passwordEncoder;

    private JwtTokenProvider jwtTokenProvider;

    public LoginController(AuthenticationManager authenticationManager,
                           UserService userService,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {

        this.authenticationManager = authenticationManager;
        this.userService = userService;
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

    @GetMapping("/getUsernameAvailability")
    public UsernameAvailability getUsernameAvailability(@RequestParam(value = "username") String username) {
        return new UsernameAvailability(userService.checkUsernameAvailability(username));
    }

    @GetMapping("/getEmailAvailability")
    public EmailAvailability getEmailAvailability(@RequestParam(value = "email") String email) {
        return new EmailAvailability(userService.checkEmailAvailability(email));
    }

    @GetMapping("/currentUser")
    @PreAuthorize("hasRole('ROLE_USER')")
    public SiteUserSummary getCurrentUser(@CurrentSiteUser UserPrincipal currentUser) {

        LOGGER.debug("Made it to getCurrentUser()");

        return new SiteUserSummary(currentUser.getUsername(), currentUser.getEmail());
    }

}
