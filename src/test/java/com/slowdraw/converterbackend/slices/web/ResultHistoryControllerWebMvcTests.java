package com.slowdraw.converterbackend.slices.web;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    //private final String RESULT_HISTORY_NOT_FOUND = "Formula not found.";

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

    private SiteUser testUser;

    private List<ResultHistory> testResultHistoryList;

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

        LOGGER.info(mongoOperations.findAll(ResultHistory.class).stream().map(ResultHistory::getEntryDate).toString());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void testGetAllUsernameResultHistoryReturnsAllResults() throws Exception {

        given(resultHistoryService.findAllByUsername(testUser.getUsername()))
                .willReturn(testResultHistoryList.stream().collect(Collectors.toList()));

        mockMvc.perform(get("/resultHistory/{username}", testUser.getUsername())
                .requestAttr("username", testUser.getUsername())
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
                .andReturn();
    }
}
