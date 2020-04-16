package com.slowdraw.converterbackend.slices.web;

import com.slowdraw.converterbackend.assembler.SiteUserEntityModelAssembler;
import com.slowdraw.converterbackend.controller.SiteUserController;
import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.domain.Role;
import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.exception.UserException;
import com.slowdraw.converterbackend.repository.SiteUserRepository;
import com.slowdraw.converterbackend.security.JwtAuthenticationEntryPoint;
import com.slowdraw.converterbackend.security.JwtAuthenticationFilter;
import com.slowdraw.converterbackend.security.JwtTokenProvider;
import com.slowdraw.converterbackend.security.UserPrincipal;
import com.slowdraw.converterbackend.service.SiteUserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SiteUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SiteUserEntityModelAssembler.class)
@AutoConfigureDataMongo
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SiteUserControllerWebMvcTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteUserControllerWebMvcTests.class);

    @MockBean
    private SiteUserService siteUserService;

    @MockBean
    private SiteUserRepository siteUserRepository;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MongoOperations mongoOperations;

    private SiteUser testUser;

    @BeforeAll
    void initSiteUserService() {

        Formula areaCircle = Formula.builder()
                .formulaName("areaCircle")
                .formulaUrl("/areaCircle")
                .category("Mathematics")
                .displayName("Area of a Circle")
                .build();

        Formula pythagoreanTheorem = Formula.builder()
                .formulaName("pythagoreanTheorem")
                .formulaUrl("/pythagoreanTheorem")
                .category("Mathematics")
                .displayName("Pythagorean Theorem")
                .build();

        Formula mhzToMeters = Formula.builder()
                .formulaName("mhzToMeters")
                .formulaUrl("/mhzToMeters")
                .category("Physics")
                .displayName("MHz to Meters")
                .build();

        List<Formula> formulaList = new ArrayList<>();
        formulaList.add(areaCircle);
        formulaList.add(pythagoreanTheorem);
        formulaList.add(mhzToMeters);

        Role userRole = new Role();
        userRole.setUsername("testUsername");
        userRole.setRoleName("ROLE_USER");

        Set<Role> roles = new HashSet<Role>();

        testUser = SiteUser.builder()
                .username("testUsername")
                .password("testPassword")
                .email("test@email.com")
                .favoritesList(formulaList)
                .roles(roles)
                .build();

        mongoOperations.dropCollection(SiteUser.class);
        mongoOperations.insert(testUser);
        SiteUser newUser = mongoOperations.findById(testUser.getUsername(), SiteUser.class);

    }

//    @Test
//    public void testGetCurrentUserReturnsCurrentUser() {
//        when(siteUserService.)
//    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetSiteUserProfileReturnsProfileAndLinks() throws Exception {

        given(siteUserService.findUserById(testUser.getUsername())).willReturn(testUser);

        mockMvc.perform(get("/user/{username}", testUser.getUsername())
                .requestAttr("username", testUser.getUsername())
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("username", is(testUser.getUsername())))
                .andExpect(jsonPath("$._links.getSiteUserProfile.href",
                        is("http://localhost/user/" + testUser.getUsername())))
                .andReturn();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetSiteUserProfileThrowsUserExceptionWhenUsernameNotFound() throws Exception {

        given(siteUserService.findUserById(testUser.getUsername()))
                .willThrow(UserException.class);

        mockMvc.perform(get("/user/{username}", "unknown")
                .requestAttr("username", "unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("userNotFound", is("Username not found.")));
    }


}
