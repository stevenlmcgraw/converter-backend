package com.slowdraw.converterbackend.slices.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slowdraw.converterbackend.assembler.SiteUserEntityModelAssembler;
import com.slowdraw.converterbackend.controller.SiteUserController;
import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.domain.ResultHistory;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
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

    List<Formula> checkFavoritesList;
    private List<String> siteUserLinksList;

    private String getSiteUserProfile = "http://localhost/user/%s";
    private String addFormulaToFavorites =
            "http://localhost/user/%s/favorites/{formulaName}";
    private String deleteFormulaFromFavorites =
            "http://localhost/user/%s/favorites/delete/{formulaName}";
    private String deleteAllFormulasFromFavorites =
            "http://localhost/user/%s/favorites/delete";

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
        roles.add(userRole);

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
//    @WithMockUser(roles = "USER")
//    public void testGetCurrentUserReturnsCurrentUser() throws Exception {
//
//        UserPrincipal testUserPrincipal = UserPrincipal.createUserPrincipal(testUser);
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        String testUserPrincipalJson = mapper.writeValueAsString(testUserPrincipal);
//
//
//        mockMvc.perform(get("/user/currentUser")
//                .content(testUserPrincipalJson)
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaTypes.HAL_JSON))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("username", is(testUser.getUsername())))
//                .andExpect(jsonPath("email", is(testUser.getEmail())))
//                .andReturn();
//
//
//    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetSiteUserProfileReturnsProfileAndLinks() throws Exception {

        //instantiate siteUserLinksList
        siteUserLinksList = new ArrayList<>();

        //add the _links strings to siteUserLinksList for verifying jsonPath
        siteUserLinksList.add(String.format(getSiteUserProfile, testUser.getUsername()));
        siteUserLinksList.add(String.format(addFormulaToFavorites, testUser.getUsername()));
        siteUserLinksList.add(String.format(deleteFormulaFromFavorites, testUser.getUsername()));
        siteUserLinksList.add(String.format(deleteAllFormulasFromFavorites, testUser.getUsername()));

        //instantiate checkFavoritesList
        checkFavoritesList = testUser.getFavoritesList();

        given(siteUserService.findUserById(testUser.getUsername())).willReturn(testUser);

        mockMvc.perform(get("/user/{username}", testUser.getUsername())
                .requestAttr("username", testUser.getUsername())
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("username",
                        is(testUser.getUsername())))
                .andExpect(jsonPath("password",
                        is(testUser.getPassword())))
                .andExpect(jsonPath("email",
                        is(testUser.getEmail())))
                .andExpect(jsonPath("$.favoritesList[*].formulaName",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getFormulaName).toArray())))
                .andExpect(jsonPath("$.favoritesList[*].formulaUrl",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getFormulaUrl).toArray())))
                .andExpect(jsonPath("$.favoritesList[*].category",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getCategory).toArray())))
                .andExpect(jsonPath("$.favoritesList[*].displayName",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getDisplayName).toArray())))
                .andExpect(jsonPath("$.roles[0].username",
                        is(testUser.getUsername())))
                .andExpect(jsonPath("$.roles[0].roleName", is("ROLE_USER")))
                .andExpect(jsonPath("_links[*].href",
                        containsInAnyOrder(siteUserLinksList.toArray())))
                .andExpect(jsonPath("$._links.addFormulaToFavorites.templated",
                        is(true)))
                .andExpect(jsonPath("$._links.deleteFormulaFromFavorites.templated",
                        is(true)))
                .andReturn();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetSiteUserProfileThrowsUserExceptionWhenUsernameNotFound() throws Exception {

        given(siteUserService.findUserById(testUser.getUsername()))
                .willThrow(UserException.class);

        mockMvc.perform(get("/user/{username}", "unknown")
                .requestAttr("username", "unknown"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("userNotFound", is(USERNAME_NOT_FOUND)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testAddFormulaToFavoritesListReturnsUpdatedSiteUser() throws Exception {

        //instantiate siteUserLinksList
        siteUserLinksList = new ArrayList<>();

        //add the _links strings to siteUserLinksList for verifying jsonPath
        siteUserLinksList.add(String.format(getSiteUserProfile, testUser.getUsername()));
        siteUserLinksList.add(String.format(addFormulaToFavorites, testUser.getUsername()));
        siteUserLinksList.add(String.format(deleteFormulaFromFavorites, testUser.getUsername()));
        siteUserLinksList.add(String.format(deleteAllFormulasFromFavorites, testUser.getUsername()));

        //instantiate checkFavoritesList
        checkFavoritesList = testUser.getFavoritesList();

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
                .andExpect(jsonPath("username",
                        is(testUser.getUsername())))
                .andExpect(jsonPath("password",
                        is(testUser.getPassword())))
                .andExpect(jsonPath("email",
                        is(testUser.getEmail())))
                .andExpect(jsonPath("$.favoritesList[*].formulaName",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getFormulaName).toArray())))
                .andExpect(jsonPath("$.favoritesList[*].formulaUrl",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getFormulaUrl).toArray())))
                .andExpect(jsonPath("$.favoritesList[*].category",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getCategory).toArray())))
                .andExpect(jsonPath("$.favoritesList[*].displayName",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getDisplayName).toArray())))
                .andExpect(jsonPath("$.roles[0].username",
                        is(testUser.getUsername())))
                .andExpect(jsonPath("$.roles[0].roleName", is("ROLE_USER")))
                .andExpect(jsonPath("_links[*].href",
                        containsInAnyOrder(siteUserLinksList.toArray())))
                .andExpect(jsonPath("$._links.addFormulaToFavorites.templated",
                        is(true)))
                .andExpect(jsonPath("$._links.deleteFormulaFromFavorites.templated",
                        is(true)))
                .andReturn();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testAddFormulaToFavoritesListThrowsUserExceptionIfNoUsernameFound() throws Exception {

        mockMvc.perform(post("/user/{username}/favorites/{formulaName}",
                "unknown", "mhzToMeters")
                .requestAttr("username", "unknown")
                .requestAttr("formulaName", "mhzToMeters"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("userNotFound", is(USERNAME_NOT_FOUND)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testUpdateUsernameFavoritesOrderReturnsLinks() throws Exception {

        //instantiate siteUserLinksList
        siteUserLinksList = new ArrayList<>();

        //add the _links strings to siteUserLinksList for verifying jsonPath
        siteUserLinksList.add(String.format(getSiteUserProfile, testUser.getUsername()));
        siteUserLinksList.add(String.format(addFormulaToFavorites, testUser.getUsername()));
        siteUserLinksList.add(String.format(deleteFormulaFromFavorites, testUser.getUsername()));
        siteUserLinksList.add(String.format(deleteAllFormulasFromFavorites, testUser.getUsername()));

        //instantiate checkFavoritesList
        checkFavoritesList = testUser.getFavoritesList();

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
                .andExpect(jsonPath("username",
                        is(testUser.getUsername())))
                .andExpect(jsonPath("password",
                        is(testUser.getPassword())))
                .andExpect(jsonPath("email",
                        is(testUser.getEmail())))
                .andExpect(jsonPath("$.favoritesList[*].formulaName",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getFormulaName).toArray())))
                .andExpect(jsonPath("$.favoritesList[*].formulaUrl",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getFormulaUrl).toArray())))
                .andExpect(jsonPath("$.favoritesList[*].category",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getCategory).toArray())))
                .andExpect(jsonPath("$.favoritesList[*].displayName",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getDisplayName).toArray())))
                .andExpect(jsonPath("$.roles[0].username",
                        is(testUser.getUsername())))
                .andExpect(jsonPath("$.roles[0].roleName", is("ROLE_USER")))
                .andExpect(jsonPath("_links[*].href",
                        containsInAnyOrder(siteUserLinksList.toArray())))
                .andExpect(jsonPath("$._links.addFormulaToFavorites.templated",
                        is(true)))
                .andExpect(jsonPath("$._links.deleteFormulaFromFavorites.templated",
                        is(true)))
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
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("userNotFound", is(USERNAME_NOT_FOUND)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testDeleteFormulaFromFavoritesListWorks() throws Exception {

        //instantiate siteUserLinksList
        siteUserLinksList = new ArrayList<>();

        //add the _links strings to siteUserLinksList for verifying jsonPath
        siteUserLinksList.add(String.format(getSiteUserProfile, testUser.getUsername()));
        siteUserLinksList.add(String.format(addFormulaToFavorites, testUser.getUsername()));
        siteUserLinksList.add(String.format(deleteFormulaFromFavorites, testUser.getUsername()));
        siteUserLinksList.add(String.format(deleteAllFormulasFromFavorites, testUser.getUsername()));

        Formula mhzToMeters = mongoOperations.findById("mhzToMeters", Formula.class);

        //get current testUser favorites list
        checkFavoritesList = testUser.getFavoritesList();

        //remove mhzToMeters and use this list to check jsonPath
        checkFavoritesList.remove(mhzToMeters);

        //make a second user and mutate favoritesList field to ensure test cases
        //do not depend on order they are run
        SiteUser testUser2 = mongoOperations.findById(testUser.getUsername(), SiteUser.class);

        //change testUser2 favoritesList
        testUser2.setFavoritesList(checkFavoritesList);

        //SiteUser roles field is a DBRef, we need to add it
        testUser2.setRoles(testUser.getRoles());

        given(siteUserService
                .deleteSingleFormulaFromFavorite(testUser.getUsername(),
                        mhzToMeters.getFormulaName()))
                .willReturn(testUser2);

        mockMvc.perform(delete("/user/{username}/favorites/delete/{formulaName}",
                testUser2.getUsername(), mhzToMeters.getFormulaName()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("username",
                        is(testUser2.getUsername())))
                .andExpect(jsonPath("password",
                        is(testUser2.getPassword())))
                .andExpect(jsonPath("email",
                        is(testUser2.getEmail())))
                .andExpect(jsonPath("$.favoritesList[*].formulaName",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getFormulaName).toArray())))
                .andExpect(jsonPath("$.favoritesList[*].formulaUrl",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getFormulaUrl).toArray())))
                .andExpect(jsonPath("$.favoritesList[*].category",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getCategory).toArray())))
                .andExpect(jsonPath("$.favoritesList[*].displayName",
                        containsInAnyOrder(checkFavoritesList.stream()
                                .map(Formula::getDisplayName).toArray())))
                .andExpect(jsonPath("$.roles[0].username",
                        is(testUser2.getUsername())))
                .andExpect(jsonPath("$.roles[0].roleName", is("ROLE_USER")))
                .andExpect(jsonPath("_links[*].href",
                        containsInAnyOrder(siteUserLinksList.toArray())))
                .andExpect(jsonPath("$._links.addFormulaToFavorites.templated",
                        is(true)))
                .andExpect(jsonPath("$._links.deleteFormulaFromFavorites.templated",
                        is(true)))
                .andReturn();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testDeleteSingleFormulaGivesUsernameNotFoundIfNotFound() throws Exception {

        mockMvc.perform(delete("/user/{username}/favorites/delete/{formulaName}",
                "unknown", "mhzToMeters"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("userNotFound", is(USERNAME_NOT_FOUND)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testDeleteAllFormulasFromFavoritesListWorks() throws Exception {

        //instantiate siteUserLinksList
        siteUserLinksList = new ArrayList<>();

        //add the _links strings to siteUserLinksList for verifying jsonPath
        siteUserLinksList.add(String.format(getSiteUserProfile, testUser.getUsername()));
        siteUserLinksList.add(String.format(addFormulaToFavorites, testUser.getUsername()));
        siteUserLinksList.add(String.format(deleteFormulaFromFavorites, testUser.getUsername()));
        siteUserLinksList.add(String.format(deleteAllFormulasFromFavorites, testUser.getUsername()));


        //get current testUser favorites list
        checkFavoritesList = testUser.getFavoritesList();


        //make a second user and mutate favoritesList field to ensure test cases
        //do not depend on order they are run
        SiteUser testUser2 = mongoOperations.findById(testUser.getUsername(), SiteUser.class);

        //change testUser2 favoritesList to have nothing
        testUser2.setFavoritesList(new ArrayList<>());

        //SiteUser roles field is a DBRef, we need to add it
        testUser2.setRoles(testUser.getRoles());

        given(siteUserService
                .deleteAllFavorites(testUser.getUsername()))
                .willReturn(testUser2);

        mockMvc.perform(delete("/user/{username}/favorites/delete",
                testUser2.getUsername()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("username",
                        is(testUser2.getUsername())))
                .andExpect(jsonPath("password",
                        is(testUser2.getPassword())))
                .andExpect(jsonPath("email",
                        is(testUser2.getEmail())))
                .andExpect(jsonPath("$.favoritesList",
                        empty()))
                .andExpect(jsonPath("$.roles[0].username",
                        is(testUser2.getUsername())))
                .andExpect(jsonPath("$.roles[0].roleName", is("ROLE_USER")))
                .andExpect(jsonPath("_links[*].href",
                        containsInAnyOrder(siteUserLinksList.toArray())))
                .andExpect(jsonPath("$._links.addFormulaToFavorites.templated",
                        is(true)))
                .andExpect(jsonPath("$._links.deleteFormulaFromFavorites.templated",
                        is(true)))
                .andReturn();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testDeleteAllFormulasGivesUsernameNotFoundIfNotFound() throws Exception {

        mockMvc.perform(delete("/user/{username}/favorites/delete",
                "unknown", "mhzToMeters"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("userNotFound", is(USERNAME_NOT_FOUND)));
    }
}
