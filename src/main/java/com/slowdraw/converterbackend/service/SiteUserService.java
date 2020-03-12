package com.slowdraw.converterbackend.service;

import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.exception.UserException;
import com.slowdraw.converterbackend.repository.SiteUserRepository;
import lombok.var;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;

@Service
public class SiteUserService {

    private SiteUserRepository siteUserRepository;

    public SiteUserService(SiteUserRepository siteUserRepository) {
        this.siteUserRepository = siteUserRepository;
    }

    public SiteUser persistNewUser(SiteUser user) {
        return siteUserRepository.save(user);
    }

    public SiteUser findUserById(String username) {
        return siteUserRepository.findById(username)
                .orElseThrow(() ->
                        new UserException("Username " + " not found."));
    }

    public SiteUser updateUserPassword(String username, String newPassword) {
        return siteUserRepository.findById(username).map(
                user -> {
                    user.setPassword(newPassword);
                    return siteUserRepository.save(user);
                }).orElseThrow(() -> new UserException("Username " + " not found.")
        );
    }

    public void deleteUser(String username) {
        siteUserRepository.deleteById(username);
    }

    public ResponseEntity<?> errorMap(BindingResult result){

        var errorMap = new HashMap<>();

        for(FieldError error: result.getFieldErrors()){
            errorMap.put(error.getField(),error.getDefaultMessage());
        }

        return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
    }

    public Boolean checkUsernameAvailability(String username) {
        return !siteUserRepository.existsById(username);
    }

    public Boolean checkEmailAvailability(String email) {
        return !siteUserRepository.existsByEmail(email);
    }
}
