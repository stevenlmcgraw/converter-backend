package com.slowdraw.converterbackend.unit.service;

import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.domain.ResultHistory;
import com.slowdraw.converterbackend.domain.Role;
import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.exception.FormulaException;
import com.slowdraw.converterbackend.exception.ResultHistoryException;
import com.slowdraw.converterbackend.exception.UserException;
import com.slowdraw.converterbackend.repository.FormulasRepository;
import com.slowdraw.converterbackend.repository.ResultHistoryRepository;
import com.slowdraw.converterbackend.repository.SiteUserRepository;
import com.slowdraw.converterbackend.service.FormulaService;
import com.slowdraw.converterbackend.service.ResultHistoryService;
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
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ResultHistoryServiceUnitTests {

    private static final String RESULT_HISTORY_NOT_FOUND = "No record with ID %s found.";
    private static final String NO_HISTORY_FOR_USERNAME =
            "No result history exists for Username %s.";

    @Mock
    private ResultHistoryRepository resultHistoryRepository;

    @InjectMocks
    private ResultHistoryService resultHistoryService;

    private SiteUser testUser;

    private List<ResultHistory> testResultHistoryList;

    private int testResultHistoryListSize;

    @BeforeEach
    void initTests() {

        testUser = SiteUser.builder()
                .username("testUsername")
                .password("testPassword")
                .email("test@email.com")
                .favoritesList(new ArrayList<>())
                .roles(new HashSet<>())
                .build();

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
                .id("mockId1")
                .username(testUser.getUsername())
                .title("Result 1")
                .message("Just a little test, that's all.")
                .entryDate(new Date())
                .calculationAttributes(resultMap1)
                .build();

        ResultHistory result2 = ResultHistory.builder()
                .id("mockId2")
                .username(testUser.getUsername())
                .title("Result 2")
                .message("Just a little test, do not fret.")
                .entryDate(new Date())
                .calculationAttributes(resultMap2)
                .build();

        ResultHistory result3 = ResultHistory.builder()
                .id("mockId3")
                .username(testUser.getUsername())
                .title("Result 3")
                .message("Just a another test, chill out bud.")
                .entryDate(new Date())
                .calculationAttributes(resultMap3)
                .build();

        testResultHistoryList = new ArrayList<>();
        testResultHistoryList.add(result1);
        testResultHistoryList.add(result2);
        testResultHistoryList.add(result3);

        testResultHistoryListSize = testResultHistoryList.size();
    }

    @Test
    public void testFindByIdWorks() {

        when(resultHistoryRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testResultHistoryList.get(0)));

        ResultHistory checkResult =
                resultHistoryService.findById(testResultHistoryList.get(0).getId());

        Assertions.assertNotNull(checkResult);
        Assertions.assertEquals(testResultHistoryList.get(0), checkResult);
        assertThat(checkResult, instanceOf(ResultHistory.class));
    }

    @Test
    public void testFindByIdThrowsNotFoundExceptionIfNotFound() {

        when(resultHistoryRepository.findById(any(String.class)))
                .thenThrow(
                        new ResultHistoryException(
                                String.format(RESULT_HISTORY_NOT_FOUND,
                                        testResultHistoryList.get(0).getId())));

        Exception exceptMe = Assertions.assertThrows(ResultHistoryException.class, () ->
                resultHistoryService.findById(testResultHistoryList.get(0).getId()));

        Assertions.assertEquals(
                String.format(
                        RESULT_HISTORY_NOT_FOUND,
                        testResultHistoryList.get(0).getId()),
                exceptMe.getMessage());
    }

    @Test
    public void testFindAllByUsernameReturnsAllWithSameUsername() {

        when(resultHistoryRepository.findByUsername(any(String.class)))
                .thenReturn(Optional.ofNullable(testResultHistoryList));

        List<ResultHistory> checkResult =
                resultHistoryService
                        .findAllByUsername(testResultHistoryList.get(0).getUsername());

        Assertions.assertEquals(testResultHistoryListSize, checkResult.size());
        Matchers.containsInAnyOrder(testResultHistoryList, checkResult);
    }

    @Test
    public void testFindAllByUsernameThrowsNotFoundExceptionIfNotFound() {

        when(resultHistoryRepository.findByUsername(any(String.class)))
                .thenThrow(
                        new ResultHistoryException(
                                String.format(NO_HISTORY_FOR_USERNAME,
                                        testResultHistoryList.get(0).getUsername())));

        Exception exceptMe =
                Assertions.assertThrows(ResultHistoryException.class, () ->
                resultHistoryService
                        .findAllByUsername(
                                testResultHistoryList.get(0).getUsername()));

        Assertions.assertEquals(
                String.format(
                        NO_HISTORY_FOR_USERNAME,
                        testResultHistoryList.get(0).getUsername()),
                exceptMe.getMessage());
    }

    @Test
    public void testPersistResultHistoryWorks() {

        when(resultHistoryRepository.save(any(ResultHistory.class)))
                .thenReturn(testResultHistoryList.get(0));

        ResultHistory checkResult =
                resultHistoryService.persistResultHistory(testResultHistoryList.get(0));

        Assertions.assertNotNull(checkResult);
        Assertions.assertEquals(testResultHistoryList.get(0), checkResult);
        assertThat(checkResult, instanceOf(ResultHistory.class));

    }

    @Test
    public void testUpdateResultHistoryWorks() {

        when(resultHistoryRepository.save(any(ResultHistory.class)))
                .thenReturn(testResultHistoryList.get(0));

        ResultHistory checkResult =
                resultHistoryService
                        .updateResultHistory(
                                testResultHistoryList.get(0),
                                testResultHistoryList.get(0).getId());

        Assertions.assertNotNull(checkResult);
        Assertions.assertEquals(testResultHistoryList.get(0), checkResult);
        assertThat(checkResult, instanceOf(ResultHistory.class));
    }

    @Test
    public void testDeleteSingleResultHistoryThrowsResultHistoryExceptionIdNotFound() {

        doThrow(new ResultHistoryException(
                String.format(RESULT_HISTORY_NOT_FOUND,
                        testResultHistoryList.get(0).getId())))
                .when(resultHistoryRepository)
                .deleteById(any(String.class));

        Exception exceptMe =
                Assertions.assertThrows(ResultHistoryException.class, () ->
                        resultHistoryService
                                .deleteSingleResultHistory(
                                        testResultHistoryList.get(0).getId()));

        Assertions.assertEquals(String.format(
                RESULT_HISTORY_NOT_FOUND,
                testResultHistoryList.get(0).getId()),
                exceptMe.getMessage());
    }

    @Test
    public void testDeleteAllResultHistoryThrowsResultHistoryExceptionIdNotFound() {

        doThrow(new ResultHistoryException(
                String.format(NO_HISTORY_FOR_USERNAME,
                        testResultHistoryList.get(0).getUsername())))
                .when(resultHistoryRepository)
                .removeByUsername(any(String.class));

        Exception exceptMe =
                Assertions.assertThrows(ResultHistoryException.class, () ->
                        resultHistoryService
                                .deleteUsernameAllResultHistory(
                                        testResultHistoryList.get(0).getUsername()));

        Assertions.assertEquals(String.format(
                NO_HISTORY_FOR_USERNAME,
                testResultHistoryList.get(0).getUsername()),
                exceptMe.getMessage());
    }
}
