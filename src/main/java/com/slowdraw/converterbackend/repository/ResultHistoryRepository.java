package com.slowdraw.converterbackend.repository;

import com.slowdraw.converterbackend.domain.ResultHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ResultHistoryRepository extends MongoRepository<ResultHistory, String> {

    Optional<List<ResultHistory>> findByUsername(String username);
    Optional<List<ResultHistory>> removeByUsername(String username);
}
