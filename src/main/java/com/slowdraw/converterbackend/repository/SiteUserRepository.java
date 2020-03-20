package com.slowdraw.converterbackend.repository;

import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.domain.SiteUser;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Set;

public interface SiteUserRepository extends MongoRepository<SiteUser, String> {

    Boolean existsByEmail(String email);
}
