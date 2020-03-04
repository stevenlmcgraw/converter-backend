package com.slowdraw.converterbackend.repository;

import com.slowdraw.converterbackend.domain.ResultHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ResultHistoryRepository extends MongoRepository<ResultHistory, String> {

    List<ResultHistory> findByUsername(String username);
    List<ResultHistory> removeByUsername(String username);
}
