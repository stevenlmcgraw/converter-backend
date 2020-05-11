package com.slowdraw.converterbackend.slices.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slowdraw.converterbackend.controller.LoginController;
import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.domain.Role;
import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.payload.LoginRequest;
import com.slowdraw.converterbackend.repository.RoleRepository;
import com.slowdraw.converterbackend.repository.SiteUserRepository;
import com.slowdraw.converterbackend.security.JwtAuthenticationEntryPoint;
import com.slowdraw.converterbackend.security.JwtAuthenticationFilter;
import com.slowdraw.converterbackend.security.JwtTokenProvider;
import com.slowdraw.converterbackend.service.SiteUserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureDataMongo
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginControllerWebMvcTests {

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private SiteUserService siteUserService;

    @MockBean
    private SiteUserRepository siteUserRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoOperations mongoOperations;

    private SiteUser testUser;

    private LoginRequest loginRequest;

    @BeforeAll
    void initTests() {

        Role userRole = new Role();
        userRole.setUsername("testUsername");
        userRole.setRoleName("ROLE_USER");

        Set<Role> roles = new HashSet<Role>();

        testUser = SiteUser.builder()
                .username("testUsername")
                .password("testPassword")
                .email("test@email.com")
                .favoritesList(new ArrayList<>())
                .roles(roles)
                .build();

        mongoOperations.insert(testUser, "users");

    }

    @Test
    public void testLoginControllerWorks() throws Exception {

        loginRequest = new LoginRequest();
        loginRequest.setUsername(testUser.getUsername());
        loginRequest.setPassword(testUser.getPassword());

        ObjectMapper mapper = new ObjectMapper();

        String loginJson = mapper.writeValueAsString(loginRequest);

        /*To avoid over-complicating a Web Slice test with excessive stubbing
        we will do JWT testing elsewhere and just test the jsonPath returned
        with this method call in LoginController. Hence, value of accessToken
        will just be null. Integration tests are more appropriate for that
        portion of testing.
         */
        mockMvc.perform(post("/auth/login")
                .content(loginJson).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken", is(nullValue())))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andReturn();
    }

    @Test
    public void testGetUsernameAvailabilityGivesTrueIfAvailable() throws Exception {

        given(siteUserService.checkUsernameAvailability(testUser.getUsername()))
                .willReturn(true);

        mockMvc.perform(get("/auth/getUsernameAvailability")
                .param("username", testUser.getUsername()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.available", is(true)))
                .andReturn();
    }

    @Test
    public void testGetUsernameAvailabilityGivesFalseIfNotAvailable() throws Exception {

        given(siteUserService.checkUsernameAvailability(testUser.getUsername()))
                .willReturn(false);

        mockMvc.perform(get("/auth/getUsernameAvailability")
                .param("username", testUser.getUsername()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.available", is(false)))
                .andReturn();
    }

    @Test
    public void testGetEmailAvailabilityGivesTrueIfAvailable() throws Exception {

        given(siteUserService.checkEmailAvailability(testUser.getEmail()))
                .willReturn(true);

        mockMvc.perform(get("/auth/getEmailAvailability")
                .param("email", testUser.getEmail()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.available", is(true)))
                .andReturn();
    }

    @Test
    public void testGetEmailAvailabilityGivesFalseIfNotAvailable() throws Exception {

        given(siteUserService.checkEmailAvailability(testUser.getEmail()))
                .willReturn(false);

        mockMvc.perform(get("/auth/getEmailAvailability")
                .param("email", testUser.getEmail()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.available", is(false)))
                .andReturn();
    }
}
