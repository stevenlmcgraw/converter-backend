package com.slowdraw.converterbackend.repository;

import com.slowdraw.converterbackend.domain.SiteUser;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SiteUserRepository extends MongoRepository<SiteUser, String> {

    Boolean existsByEmail(String email);
}
