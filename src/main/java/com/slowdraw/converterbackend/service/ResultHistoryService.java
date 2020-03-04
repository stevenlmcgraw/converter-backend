package com.slowdraw.converterbackend.service;

import com.slowdraw.converterbackend.domain.ResultHistory;
import com.slowdraw.converterbackend.exception.ResultHistoryException;
import com.slowdraw.converterbackend.repository.ResultHistoryRepository;
import lombok.var;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.List;

@Service
public class ResultHistoryService {

    private ResultHistoryRepository resultHistoryRepository;

    public ResultHistoryService(ResultHistoryRepository resultHistoryRepository) {
        this.resultHistoryRepository = resultHistoryRepository;
    }

    public List<ResultHistory> findAll() {
        return resultHistoryRepository.findAll();
    }

    public ResultHistory findById(String id) {
        return resultHistoryRepository.findById(id)
                .orElseThrow(() -> new ResultHistoryException("No record with ID " + id + " found."));
    }

    public List<ResultHistory> findAllByUsername (String username) {
        return resultHistoryRepository.findByUsername(username);
    }

    public ResultHistory persistResultHistory(ResultHistory resultHistory) {
        return resultHistoryRepository.save(resultHistory);
    }

    public ResultHistory updateResultHistory(ResultHistory resultHistory, String username, String id) {

        return resultHistoryRepository.findById(id).map(
                result -> {
                    result.setCalculationAttributes(resultHistory.getCalculationAttributes());
                    return resultHistoryRepository.save(result);
                }).orElseGet(() -> resultHistoryRepository.save(resultHistory));

    }

    public void deleteSingleResultHistory(String id) {
        resultHistoryRepository.delete(
                resultHistoryRepository.findById(id)
                        .orElseThrow(() -> new ResultHistoryException("No record with ID " + id + " found." )));
    }

    public void deleteUsernameAllResultHistory(String username) {
        resultHistoryRepository.removeByUsername(username);
    }

    public ResponseEntity<?> errorMap(BindingResult result){

        var errorMap = new HashMap<>();

        for(FieldError error: result.getFieldErrors()){
            errorMap.put(error.getField(),error.getDefaultMessage());
        }

        return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);

    }
}
