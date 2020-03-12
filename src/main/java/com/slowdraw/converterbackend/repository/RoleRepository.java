package com.slowdraw.converterbackend.repository;

import com.slowdraw.converterbackend.domain.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, String> {

    Role findByRoleName(String roleName);
}
