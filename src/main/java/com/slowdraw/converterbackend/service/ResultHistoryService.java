package com.slowdraw.converterbackend.service;

import com.slowdraw.converterbackend.domain.ResultHistory;
import com.slowdraw.converterbackend.exception.ResultHistoryException;
import com.slowdraw.converterbackend.repository.ResultHistoryRepository;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;

@Service
public class ResultHistoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultHistoryService.class);

    private static final String RESULT_HISTORY_NOT_FOUND = "No record with ID %s found.";
    private static final String NO_HISTORY_FOR_USERNAME =
            "No result history exists for Username %s.";

    private final ResultHistoryRepository resultHistoryRepository;

    public ResultHistoryService(ResultHistoryRepository resultHistoryRepository) {
        this.resultHistoryRepository = resultHistoryRepository;
    }

    public ResultHistory findById(String id) {
        return resultHistoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResultHistoryException(
                                String.format(RESULT_HISTORY_NOT_FOUND, id)));
    }

    public List<ResultHistory> findAllByUsername (String username) {
        return resultHistoryRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResultHistoryException(
                                String.format(NO_HISTORY_FOR_USERNAME,
                                        username)));
    }

    public ResultHistory persistResultHistory(ResultHistory resultHistory) {
        return resultHistoryRepository.save(resultHistory);
    }

    public ResultHistory updateResultHistory(ResultHistory resultHistory, String id) {

        return resultHistoryRepository.findById(id).map(
                result -> {
                    result.setCalculationAttributes(resultHistory.getCalculationAttributes());
                    return resultHistoryRepository.save(result);
                }).orElseGet(() -> resultHistoryRepository.save(resultHistory));

    }

    public void deleteSingleResultHistory(String id) {

        resultHistoryRepository.delete(
                resultHistoryRepository.findById(id)
                        .orElseThrow(() ->
                                new ResultHistoryException(
                                        String.format(RESULT_HISTORY_NOT_FOUND, id))));
    }

    public void deleteUsernameAllResultHistory(String username) {

        resultHistoryRepository.removeByUsername(username)
                .orElseThrow(() ->
                        new ResultHistoryException(
                                String.format(NO_HISTORY_FOR_USERNAME,
                                        username)));
    }

    public ResponseEntity<?> errorMap(BindingResult result){

        var errorMap = new HashMap<>();

        for(FieldError error: result.getFieldErrors()){
            errorMap.put(error.getField(),error.getDefaultMessage());
        }

        return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
    }
}
