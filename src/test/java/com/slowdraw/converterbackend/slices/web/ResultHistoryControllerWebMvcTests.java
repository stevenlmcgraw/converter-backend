package com.slowdraw.converterbackend.slices.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slowdraw.converterbackend.assembler.ResultHistoryAssembleLinksForDeleteMethods;
import com.slowdraw.converterbackend.assembler.ResultHistoryEntityModelAssembler;
import com.slowdraw.converterbackend.controller.ResultHistoryController;
import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.domain.ResultHistory;
import com.slowdraw.converterbackend.domain.Role;
import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.repository.ResultHistoryRepository;
import com.slowdraw.converterbackend.security.JwtAuthenticationEntryPoint;
import com.slowdraw.converterbackend.security.JwtAuthenticationFilter;
import com.slowdraw.converterbackend.security.JwtTokenProvider;
import com.slowdraw.converterbackend.service.ResultHistoryService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ResultHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ResultHistoryEntityModelAssembler.class)
@AutoConfigureDataMongo
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ResultHistoryControllerWebMvcTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultHistoryControllerWebMvcTests.class);

    private final String USERNAME_HAS_NO_RESULT_HISTORY = "No calculation/conversion result history for user.";
    private  String RESULT_HISTORY_NOT_FOUND = "Given result not found.";;

    @MockBean
    private ResultHistoryService resultHistoryService;

    @MockBean
    private ResultHistoryRepository resultHistoryRepository;

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

    @MockBean
    private ResultHistoryAssembleLinksForDeleteMethods linksForDeleteMethods;

    private SiteUser testUser;

    private List<ResultHistory> testResultHistoryList;
    private List<String> selfLinksList;
    private List<String> getAllResultsMatchList;
    private List<String> saveResultLinkList;
    private List<String> deleteSingleLinksList;
    private List<String> deleteAllResultsMatchList;
    private List<Boolean> deleteAllPathToKeyTemplated;

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

        mongoOperations.insert(testUser, "users");

        Map<String, Object> resultMap1 = new HashMap<>();
        resultMap1.put("x", 10);
        resultMap1.put("y", 23);
        resultMap1.put("result", 2300);

        Map<String, Object> resultMap2 = new HashMap<>();
        resultMap1.put("x", 100);
        resultMap1.put("y", 230);
        resultMap1.put("result", 23000);

        Map<String, Object> resultMap3 = new HashMap<>();
        resultMap1.put("x", 1000);
        resultMap1.put("y", 2300);
        resultMap1.put("result", 230000);

        ResultHistory result1 = ResultHistory.builder()
                .username(testUser.getUsername())
                .title("Result 1")
                .message("Just a little test, that's all.")
                .entryDate(new Date())
                .calculationAttributes(resultMap1)
                .build();

        ResultHistory result2 = ResultHistory.builder()
                .username(testUser.getUsername())
                .title("Result 2")
                .message("Just a little test, do not fret.")
                .entryDate(new Date())
                .calculationAttributes(resultMap2)
                .build();

        ResultHistory result3 = ResultHistory.builder()
                .username(testUser.getUsername())
                .title("Result 3")
                .message("Just a another test, chill out bud.")
                .entryDate(new Date())
                .calculationAttributes(resultMap3)
                .build();

        mongoOperations.dropCollection(ResultHistory.class);
        mongoOperations.insert(result1);
        mongoOperations.insert(result2, "result_history");
        mongoOperations.insert(result3, "result_history");

        testResultHistoryList = new ArrayList<>();
        testResultHistoryList.add(result1);
        testResultHistoryList.add(result2);
        testResultHistoryList.add(result3);

        //this is all for validating the links returned
        String selfLinkString = "http://localhost/resultHistory/%s/%s";
        String allResultsString = "http://localhost/resultHistory/%s";
        String saveResultString = "http://localhost/resultHistory";
        String deleteSingleResult = "http://localhost/resultHistory/delete/%s/%s";
        String deleteAllResultsString = "http://localhost/resultHistory/delete/%s";

        List<String> resultIdArray = mongoOperations.findAll(ResultHistory.class).stream()
                .map(ResultHistory::getId).collect(Collectors.toList());

        selfLinksList = resultIdArray.stream()
                .map(element -> {
                    String mutate = String.format(selfLinkString, testUser.getUsername(), element);
                    return mutate;
                })
                .collect(Collectors.toList());

        getAllResultsMatchList = Stream.generate(String::new)
                .limit(testResultHistoryList.size())
                .map(element ->
                        String.format(allResultsString, testUser.getUsername()))
                .collect(Collectors.toList());

        saveResultLinkList = Stream.generate(String::new)
                .limit(testResultHistoryList.size())
                .map(element -> saveResultString)
                .collect(Collectors.toList());

        deleteSingleLinksList = resultIdArray.stream()
                .map(element -> {
                    String mutate = String.format(deleteSingleResult, testUser.getUsername(), element);
                    return mutate;
                })
                .collect(Collectors.toList());

        deleteAllResultsMatchList = Stream.generate(String::new)
                .limit(testResultHistoryList.size())
                .map(element ->
                        String.format(deleteAllResultsString, testUser.getUsername()))
                .collect(Collectors.toList());

        LOGGER.info(mongoOperations.findAll(ResultHistory.class).stream().map(ResultHistory::getEntryDate).toString());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAllUsernameResultHistoryReturnsAllResults() throws Exception {

        given(resultHistoryService.findAllByUsername(testUser.getUsername()))
                .willReturn(testResultHistoryList.stream().collect(Collectors.toList()));

        mockMvc.perform(get("/resultHistory/{username}", testUser.getUsername())
                //.requestAttr("username", testUser.getUsername())
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$._embedded.resultHistories", hasSize(testResultHistoryList.size())))
                .andExpect(jsonPath("$._embedded.resultHistories[*].id",
                        containsInAnyOrder(mongoOperations.findAll(ResultHistory.class)
                                .stream()
                                .map(ResultHistory::getId).toArray())))
                .andExpect(jsonPath("$._embedded.resultHistories[*].username",
                        containsInAnyOrder(testResultHistoryList.stream()
                                .map(ResultHistory::getUsername).toArray())))
                .andExpect(jsonPath("$._embedded.resultHistories[*].title",
                        containsInAnyOrder(testResultHistoryList.stream()
                                .map(ResultHistory::getTitle).toArray())))
                .andExpect(jsonPath("$._embedded.resultHistories[*].message",
                        containsInAnyOrder(testResultHistoryList.stream()
                                .map(ResultHistory::getMessage).toArray())))
//                .andExpect(jsonPath("$._embedded.resultHistories[*].entryDate",
//                        containsInAnyOrder(testResultHistoryList.stream()
//                                .map(ResultHistory::getEntryDate).toArray())))
                .andExpect(jsonPath("$._embedded.resultHistories[*].calculationAttributes",
                        containsInAnyOrder(testResultHistoryList.stream()
                                .map(ResultHistory::getCalculationAttributes).toArray())))
                .andExpect(jsonPath("$.._links.self.href",
                        containsInAnyOrder(selfLinksList.toArray())))
                .andExpect(jsonPath("$.._links.getAllUsernameResultHistory.href",
                        containsInAnyOrder(getAllResultsMatchList.toArray())))
                .andExpect(jsonPath("$.._links.saveSingleResultHistory.href",
                        containsInAnyOrder(saveResultLinkList.toArray())))
                .andExpect(jsonPath("$.._links.deleteSpecificResultHistory.href",
                        containsInAnyOrder(deleteSingleLinksList.toArray())))
                .andExpect(jsonPath("$.._links.deleteAllUsernameResultHistory.href",
                        containsInAnyOrder(deleteAllResultsMatchList.toArray())))
                .andReturn();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetUsernameResultHistoryThrowsNoResultResponseWhenNoResultHistoryPresent()
            throws Exception {

        Role userRole = new Role();
        userRole.setUsername("testUsername");
        userRole.setRoleName("ROLE_USER");

        Set<Role> roles = new HashSet<Role>();

        SiteUser testUser1  = SiteUser.builder()
                .username("testUsername2")
                .password("testPassword2")
                .email("test2@email.com")
                .favoritesList(new ArrayList<>())
                .roles(roles)
                .build();

        mockMvc.perform(get("/resultHistory/{username}", testUser1.getUsername()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("resultHistoryNotFound", is(USERNAME_HAS_NO_RESULT_HISTORY)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetSpecificResultHistoryInstanceWorks() throws Exception {

        List<ResultHistory> resultHistoryList = mongoOperations.findAll(ResultHistory.class);

        ResultHistory testResultHistory1 = resultHistoryList.get(0);

        given(resultHistoryService.findById(testResultHistory1.getId())).willReturn(testResultHistory1);

        mockMvc.perform(get("/resultHistory/{username}/{id}",
                testUser.getUsername(), testResultHistory1.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id", is(testResultHistory1.getId())))
                .andExpect(jsonPath("username", is(testUser.getUsername())))
                .andExpect(jsonPath("title", is(testResultHistory1.getTitle())))
                .andExpect(jsonPath("message", is(testResultHistory1.getMessage())))
                //.andExpect(jsonPath("entryDate", is(testResultHistory1.getEntryDate())))
                .andExpect(jsonPath("calculationAttributes",
                        aMapWithSize(testResultHistory1.getCalculationAttributes().size())))
                .andExpect(jsonPath("calculationAttributes[*]",
                        containsInAnyOrder(testResultHistory1.getCalculationAttributes().values().toArray())))
                .andExpect(jsonPath("_links.self.href",
                        is("http://localhost/resultHistory/" + testUser.getUsername() +
                                "/" + testResultHistory1.getId())))
                .andExpect(jsonPath("_links.getAllUsernameResultHistory.href",
                        is("http://localhost/resultHistory/" + testUser.getUsername())))
                .andExpect(jsonPath("_links.deleteSpecificResultHistory.href",
                        is("http://localhost/resultHistory/delete/" + testUser.getUsername() +
                                "/" + testResultHistory1.getId())))
                .andExpect(jsonPath("_links.deleteAllUsernameResultHistory.href",
                        is("http://localhost/resultHistory/delete/" + testUser.getUsername())))
                .andReturn();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetSingleResultHistoryThrowsNotFoundResponseWhenDoesNotExist() throws Exception {

        mockMvc.perform(get("/resultHistory/{username}/{id}", testUser.getUsername(), "bogus"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("resultHistoryNotFound",
                        is(RESULT_HISTORY_NOT_FOUND)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testSaveNewResultHistoryWorksProperly() throws Exception {

        List<ResultHistory> resultHistoryList = mongoOperations.findAll(ResultHistory.class);

        ResultHistory testResultHistory2 = resultHistoryList.get(0);

        ObjectMapper mapper = new ObjectMapper();

        String resultJson = mapper.writeValueAsString(testResultHistory2);

        LOGGER.info(resultJson);

        given(resultHistoryService.persistResultHistory(testResultHistory2)).willReturn(testResultHistory2);

        mockMvc.perform(post("/resultHistory")
                .content(resultJson).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("id",
                        is(testResultHistory2.getId())))
                .andExpect(jsonPath("username",
                        is(testResultHistory2.getUsername())))
                .andExpect(jsonPath("title",
                        is(testResultHistory2.getTitle())))
                .andExpect(jsonPath("message",
                        is(testResultHistory2.getMessage())))
              //.andExpect(jsonPath("entryDate", is(testResultHistory1.getEntryDate())))
                .andExpect(jsonPath("calculationAttributes",
                        aMapWithSize(testResultHistory2.getCalculationAttributes().size())))
                .andExpect(jsonPath("calculationAttributes[*]",
                        containsInAnyOrder(testResultHistory2.getCalculationAttributes().values().toArray())))
                .andExpect(jsonPath("_links.self.href",
                        is("http://localhost/resultHistory/" + testUser.getUsername() +
                                "/" + testResultHistory2.getId())))
                .andExpect(jsonPath("_links.getAllUsernameResultHistory.href",
                        is("http://localhost/resultHistory/" + testUser.getUsername())))
                .andExpect(jsonPath("_links.deleteSpecificResultHistory.href",
                        is("http://localhost/resultHistory/delete/" + testUser.getUsername() +
                                "/" + testResultHistory2.getId())))
                .andExpect(jsonPath("_links.deleteAllUsernameResultHistory.href",
                        is("http://localhost/resultHistory/delete/" + testUser.getUsername())))
                .andReturn();
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testSaveResultHistoryGivesErrorMessageIfFailureOccurs() throws Exception {

        mockMvc.perform(post("/resultHistory")
                .content(String.valueOf(nullValue())).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();
    }


}
