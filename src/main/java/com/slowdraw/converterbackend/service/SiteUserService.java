package com.slowdraw.converterbackend.service;

import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.exception.UserException;
import com.slowdraw.converterbackend.repository.SiteUserRepository;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SiteUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteUserService.class);

    private static final String USERNAME_NOT_FOUND = "Username %s not found.";

    private final SiteUserRepository siteUserRepository;
    private final FormulaService formulaService;

    public SiteUserService(SiteUserRepository siteUserRepository, FormulaService formulaService) {
        this.siteUserRepository = siteUserRepository;
        this.formulaService = formulaService;
    }

    public SiteUser findUserById(String username) {

        //sanity check: username exists
        if(!siteUserRepository.findById(username).isPresent())
            throw new UserException(
                    String.format(USERNAME_NOT_FOUND, username));

        return siteUserRepository.findById(username)
                .orElseThrow(() ->
                        new UserException(String.format(
                                USERNAME_NOT_FOUND,
                                username)));
    }

    public SiteUser updateUserPassword(String username, String newPassword) {

        //sanity check: username exists
        if(!siteUserRepository.findById(username).isPresent())
            throw new UserException(
                    String.format(USERNAME_NOT_FOUND, username));

        return siteUserRepository.findById(username).map(
                user -> {
                    user.setPassword(newPassword);
                    return siteUserRepository.save(user);
                }).orElseThrow(() ->
                new UserException(String.format(
                        USERNAME_NOT_FOUND,
                        username))
        );
    }

    public void deleteUser(String username) {
        siteUserRepository.deleteById(username);
    }


    public SiteUser modifyUsernameFavoritesList(String username, List<String> newPositions) {

        //sanity check: username exists
        if(!siteUserRepository.findById(username).isPresent())
            throw new UserException(
                    String.format(USERNAME_NOT_FOUND, username));

        return siteUserRepository.findById(username).map(
                user -> {
                    user.setFavoritesList(newPositions.stream()
                            .map(formula -> formulaService.getSingleFormulaInfo(formula)
                    ).collect(Collectors.toList()));
                    return siteUserRepository.save(user);
                }
        ).orElseThrow(() ->
                new UserException(String.format(
                        USERNAME_NOT_FOUND,
                        username)));
    }

    public SiteUser saveFormulaToFavoritesList(String username, String formulaName) {

        //sanity check: username exists
        if(!siteUserRepository.findById(username).isPresent())
            throw new UserException(
                    String.format(USERNAME_NOT_FOUND, username));

        //make sure there are no duplicates in favorites list
        if(!siteUserRepository.findById(username).get()
                .getFavoritesList().stream().filter(
                        formula ->
                                formula.getFormulaName().equals(formulaName))
                .collect(Collectors.toList()).isEmpty()) {

            return siteUserRepository.save(siteUserRepository.findById(username)
                    .orElseThrow(() ->
                            new UserException(String.format(
                                    USERNAME_NOT_FOUND,
                                    username))));
        }

        //add to favorites list
        return siteUserRepository.findById(username).map(
                user -> {
                    user.getFavoritesList()
                            .add(formulaService.getSingleFormulaInfo(formulaName));

                    return siteUserRepository.save(user);
                }
        ).orElseThrow(() ->
                new UserException(String.format(
                        USERNAME_NOT_FOUND,
                        username)));
    }

    public SiteUser deleteAllFavorites(String username) {

        //sanity check: username exists
        if(!siteUserRepository.findById(username).isPresent())
            throw new UserException(
                    String.format(USERNAME_NOT_FOUND, username));

        return siteUserRepository.findById(username).map(
                user -> {
                    user.setFavoritesList(Collections.emptyList());
                    return siteUserRepository.save(user);
                }
        ).orElseThrow(() ->
                new UserException(String.format(
                        USERNAME_NOT_FOUND,
                        username)));
    }

    public SiteUser deleteSingleFormulaFromFavorite(String username, String formulaName) {

        //sanity check: username exists
        if(!siteUserRepository.findById(username).isPresent())
            throw new UserException(
                    String.format(USERNAME_NOT_FOUND, username));

        return siteUserRepository.findById(username).map(
                user -> {
                    user.setFavoritesList(user.getFavoritesList().stream().filter(
                            formula ->
                                    !formula.getFormulaName().equals(formulaName)
                    ).collect(Collectors.toList()));

                    return siteUserRepository.save(user);
                }
        ).orElseThrow(() ->
                new UserException(String.format(
                        USERNAME_NOT_FOUND,
                        username)));
    }

    public Boolean checkUsernameAvailability(String username) {
        return !siteUserRepository.existsById(username);
    }

    public Boolean checkEmailAvailability(String email) {
        return !siteUserRepository.existsByEmail(email);
    }

    public ResponseEntity<?> errorMap(BindingResult result){

        var errorMap = new HashMap<>();

        for(FieldError error: result.getFieldErrors()){
            errorMap.put(error.getField(),error.getDefaultMessage());
        }

        return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
    }
}