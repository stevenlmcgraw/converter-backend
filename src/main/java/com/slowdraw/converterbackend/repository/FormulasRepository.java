package com.slowdraw.converterbackend.repository;

import com.slowdraw.converterbackend.domain.Formula;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FormulasRepository extends MongoRepository<Formula, String> {

}
