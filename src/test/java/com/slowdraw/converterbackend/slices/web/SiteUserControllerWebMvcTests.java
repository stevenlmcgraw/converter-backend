package com.slowdraw.converterbackend.slices.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slowdraw.converterbackend.assembler.SiteUserEntityModelAssembler;
import com.slowdraw.converterbackend.controller.SiteUserController;
import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.domain.Role;
import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.exception.UserAdvice;
import com.slowdraw.converterbackend.exception.UserException;
import com.slowdraw.converterbackend.repository.FormulasRepository;
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
import org.mockito.stubbing.Answer;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private final String USERNAME_NOT_FOUND = "Username not found.";
    private final String FORMULA_NOT_FOUND = "Formula not found.";

    @MockBean
    private SiteUserService siteUserService;

    @MockBean
    private SiteUserRepository siteUserRepository;

    @MockBean
    private FormulasRepository formulasRepository;

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
    void initTests() {

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

        mongoOperations.insert(areaCircle, "formulas");
        mongoOperations.insert(pythagoreanTheorem, "formulas");
        mongoOperations.insert(mhzToMeters, "formulas");

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
        mongoOperations.insert(testUser, "users");
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
                .andExpect(jsonPath("userNotFound", is(USERNAME_NOT_FOUND)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testAddFormulaToFavoritesListReturnsUpdatedSiteUser() throws Exception {

        given(siteUserService.saveFormulaToFavoritesList(
                testUser.getUsername(), "mhzToMeters")).willReturn(testUser);

        mockMvc.perform(post("/user/{username}/favorites/{formulaName}",
                testUser.getUsername(), "mhzToMeters")
                .requestAttr("username", testUser.getUsername())
                .requestAttr("formulaName", "mhzToMeters")
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("username", is(testUser.getUsername())))
                .andExpect(jsonPath("favoritesList[2].formulaName",
                        is("mhzToMeters")))
                .andExpect(jsonPath("$._links.getSiteUserProfile.href",
                        is("http://localhost/user/" + testUser.getUsername())))
                .andReturn();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testAddFormulaToFavoritesListThrowsUserExceptionIfNoUsernameFound() throws Exception {

        mockMvc.perform(post("/user/{username}/favorites/{formulaName}",
                "unknown", "mhzToMeters")
                .requestAttr("username", "unknown")
                .requestAttr("formulaName", "mhzToMeters"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("userNotFound", is(USERNAME_NOT_FOUND)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testUpdateUsernameFavoritesOrderReturnsLinks() throws Exception {

        String[] newOrder = {"mhzToMeters", "areaCircle", "pythagoreanTheorem"};
        ObjectMapper mapper = new ObjectMapper();
        String newOrderJson = mapper.writeValueAsString(newOrder);

        given(siteUserService.modifyUsernameFavoritesList(
                testUser.getUsername(), Arrays.asList(newOrder)))
                .willReturn(testUser);

        mockMvc.perform(put("/user/{username}/favorites/reorder", testUser.getUsername())
                .requestAttr("username", testUser.getUsername())
                .content(newOrderJson).contentType(MediaType.APPLICATION_JSON)
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
    public void testUpdateFormulaToFavoritesListThrowsUserExceptionIfNoUsernameFound() throws Exception {

        String[] newOrder = {"mhzToMeters", "areaCircle", "pythagoreanTheorem"};
        ObjectMapper mapper = new ObjectMapper();
        String newOrderJson = mapper.writeValueAsString(newOrder);

        mockMvc.perform(post("/user/{username}/favorites/reorder",testUser.getUsername())
                .content(newOrderJson).contentType(MediaType.APPLICATION_JSON)
                .requestAttr("username", testUser.getUsername()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("userNotFound", is(USERNAME_NOT_FOUND)));
    }
}
