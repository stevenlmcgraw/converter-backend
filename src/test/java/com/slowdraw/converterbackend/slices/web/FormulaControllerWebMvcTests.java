package com.slowdraw.converterbackend.slices.web;

import com.slowdraw.converterbackend.assembler.FormulaEntityModelAssembler;
import com.slowdraw.converterbackend.controller.FormulaController;
import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.repository.FormulasRepository;
import com.slowdraw.converterbackend.security.JwtAuthenticationEntryPoint;
import com.slowdraw.converterbackend.security.JwtAuthenticationFilter;
import com.slowdraw.converterbackend.security.JwtTokenProvider;
import com.slowdraw.converterbackend.service.FormulaService;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = FormulaController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(FormulaEntityModelAssembler.class)
@AutoConfigureDataMongo
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FormulaControllerWebMvcTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormulaControllerWebMvcTests.class);

    private static final String FORMULA_NOT_FOUND = "Formula not found.";

    @MockBean
    private FormulaService formulaService;

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

    private List<Formula> testFormulaList;

    @BeforeAll
    void initTests() {

        Formula areaCircle = Formula.builder()
                .formulaName("areaCircle")
                .formulaUrl("/areaCircle")
                .category("Mathematics")
                .displayName("Area of a Circle")
                .build();

        Formula mhzToMeters = Formula.builder()
                .formulaName("mhzToMeters")
                .formulaUrl("/mhzToMeters")
                .category("Physics")
                .displayName("MHz to Meters")
                .build();

        Formula pythagoreanTheorem = Formula.builder()
                .formulaName("pythagoreanTheorem")
                .formulaUrl("/pythagoreanTheorem")
                .category("Mathematics")
                .displayName("Pythagorean Theorem")
                .build();

        mongoOperations.dropCollection(Formula.class);
        mongoOperations.insert(areaCircle, "formulas");
        mongoOperations.insert(mhzToMeters, "formulas");
        mongoOperations.insert(pythagoreanTheorem, "formulas");

        testFormulaList = new ArrayList<>();
        testFormulaList.add(areaCircle);
        testFormulaList.add(mhzToMeters);
        testFormulaList.add(pythagoreanTheorem);
    }

    @Test
    public void testGetAllFormulasReturnsAllFormulas() throws Exception {

        String getAllFormulasLink = "http://localhost/formulas";

        List<String> allFormulasList = new ArrayList<>();
        allFormulasList.add(getAllFormulasLink);
        allFormulasList.add(getAllFormulasLink);
        allFormulasList.add(getAllFormulasLink);

        String areaCircleLink = "http://localhost/formulas/areaCircle";
        String mhzToMetersLink = "http://localhost/formulas/mhzToMeters";
        String pythagoreanTheoremLink = "http://localhost/formulas/pythagoreanTheorem";

        List<String> linksList = new ArrayList<>();
        linksList.add(areaCircleLink);
        linksList.add(mhzToMetersLink);
        linksList.add(pythagoreanTheoremLink);

        given(formulaService.getAllFormulas())
                .willReturn(testFormulaList
                        .stream().collect(Collectors.toSet()));

        mockMvc.perform(get("/formulas")
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("_embedded.formulas", hasSize(testFormulaList.size())))
                .andExpect(jsonPath("$._embedded.formulas[*].formulaName",
                        containsInAnyOrder(testFormulaList.stream()
                                .map(Formula::getFormulaName).toArray())))
                .andExpect(jsonPath("$._embedded.formulas[*].formulaUrl",
                        containsInAnyOrder(testFormulaList.stream()
                                .map(Formula::getFormulaUrl).toArray())))
                .andExpect(jsonPath("$._embedded.formulas[*].category",
                        containsInAnyOrder(testFormulaList.stream()
                                .map(Formula::getCategory).toArray())))
                .andExpect(jsonPath("$._embedded.formulas[*].displayName",
                        containsInAnyOrder(testFormulaList.stream()
                                .map(Formula::getDisplayName).toArray())))
                .andExpect(jsonPath("$._embedded.formulas[*]._links.getFormulaInfo.href",
                        containsInAnyOrder(linksList.stream().toArray())))
                .andExpect(jsonPath("$._embedded.formulas[*]._links.getAllFormulas.href",
                        containsInAnyOrder(allFormulasList.stream().toArray())))
                .andReturn();
    }

    @Test
    public void testGetSingleFormulaReturnsCorrectFormula() throws Exception {

        given(formulaService.getSingleFormulaInfo(testFormulaList.get(0).getFormulaName()))
                .willReturn(testFormulaList.get(0));

        mockMvc.perform(get("/formulas/{name}", testFormulaList.get(0).getFormulaName())
                .requestAttr("name", testFormulaList.get(0).getFormulaName())
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("formulaName",
                        is(testFormulaList.get(0).getFormulaName())))
                .andExpect(jsonPath("formulaUrl",
                        is(testFormulaList.get(0).getFormulaUrl())))
                .andExpect(jsonPath("category",
                        is(testFormulaList.get(0).getCategory())))
                .andExpect(jsonPath("displayName",
                        is(testFormulaList.get(0).getDisplayName())))
                .andExpect(jsonPath("_links.getFormulaInfo.href",
                        is("http://localhost/formulas/" + testFormulaList.get(0).getFormulaName())))
                .andExpect(jsonPath("_links.getAllFormulas.href",
                        is("http://localhost/formulas")))
                .andReturn();
    }

    @Test
    public void testGetSingleFormulaThrowsExceptionWithInvalidFormulaName() throws Exception {

        mockMvc.perform(get("/formulas/{name}", "badname")
                .requestAttr("name", "badname"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("formulaNotFound", is(FORMULA_NOT_FOUND)));
    }
}
