package com.slowdraw.converterbackend.service;

import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.exception.UserException;
import com.slowdraw.converterbackend.repository.UserRepository;
import lombok.var;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public SiteUser persistNewUser(SiteUser user) {
        return userRepository.save(user);
    }

    public SiteUser findUserById(String username) {
        return userRepository.findById(username)
                .orElseThrow(() ->
                        new UserException("Username " + " not found."));
    }

    public SiteUser updateUserPassword(String username, String newPassword) {
        return userRepository.findById(username).map(
                user -> {
                    user.setPassword(newPassword);
                    return userRepository.save(user);
                }).orElseThrow(() -> new UserException("Username " + " not found.")
        );
    }

    public void deleteUser(String username) {
        userRepository.deleteById(username);
    }

    public ResponseEntity<?> errorMap(BindingResult result){

        var errorMap = new HashMap<>();

        for(FieldError error: result.getFieldErrors()){
            errorMap.put(error.getField(),error.getDefaultMessage());
        }

        return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
    }

    public Boolean checkUsernameAvailability(String username) {
        return !userRepository.existsById(username);
    }

    public Boolean checkEmailAvailability(String email) {
        return !userRepository.existsByEmail(email);
    }
}