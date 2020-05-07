package com.slowdraw.converterbackend.unit.service;

import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.domain.Role;
import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.exception.FormulaException;
import com.slowdraw.converterbackend.exception.UserException;
import com.slowdraw.converterbackend.repository.FormulasRepository;
import com.slowdraw.converterbackend.repository.SiteUserRepository;
import com.slowdraw.converterbackend.service.FormulaService;
import com.slowdraw.converterbackend.service.SiteUserService;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FormulaServiceUnitTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteUserServiceUnitTests.class);

    private static final String FORMULA_NOT_FOUND = "Formula for %s not found.";

    @Mock
    private FormulasRepository formulasRepository;

    @InjectMocks
    private FormulaService formulaService;

    private SiteUser testUser;

    private List<Formula> testFormulaList;

    private int testFormulaListSize;

    @BeforeEach
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

        testFormulaList = new ArrayList<>();
        testFormulaList.add(areaCircle);
        testFormulaList.add(pythagoreanTheorem);
        testFormulaList.add(mhzToMeters);

        testFormulaListSize = testFormulaList.size();
    }

    @Test
    public void testGetAllFormulasWorks() {

        when(formulasRepository.findAll()).thenReturn(testFormulaList);

        Set<Formula> checkReturnSet = formulaService.getAllFormulas();

        Assertions.assertEquals(testFormulaListSize, checkReturnSet.size());
        Matchers.containsInAnyOrder(testFormulaList, checkReturnSet);
    }

    @Test
    public void testGetSingleFormulaWorks() {

        when(formulasRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testFormulaList.get(0)));

        Formula checkFormula =
                formulaService
                        .getSingleFormulaInfo(testFormulaList.get(0).getFormulaName());

        Assertions.assertNotNull(checkFormula);
        Assertions.assertEquals(testFormulaList.get(0), checkFormula);
        assertThat(checkFormula, instanceOf(Formula.class));
    }

    @Test
    public void testGetSingleFormulaThrowsFormulaExceptionWhenNotFound() {

        when(formulasRepository.findById(any(String.class)))
                .thenThrow(new FormulaException(
                        String.format(
                                FORMULA_NOT_FOUND,
                                testFormulaList.get(0).getFormulaName())));

        Exception exceptMe = Assertions.assertThrows(FormulaException.class, () ->
                formulaService
                        .getSingleFormulaInfo(
                                testFormulaList.get(0).getFormulaName()));

        Assertions.assertEquals(String.format(
                FORMULA_NOT_FOUND,
                testFormulaList.get(0).getFormulaName()),
                exceptMe.getMessage());
    }
}
