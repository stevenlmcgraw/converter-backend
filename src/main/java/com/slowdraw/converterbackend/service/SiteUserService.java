package com.slowdraw.converterbackend.service;

import com.slowdraw.converterbackend.domain.Formula;
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
import java.util.stream.Stream;

@Service
public class SiteUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteUserService.class);

    private static final String USERNAME_NOT_FOUND = "Username not found.";

    private final SiteUserRepository siteUserRepository;
    private final FormulaService formulaService;

    public SiteUserService(SiteUserRepository siteUserRepository, FormulaService formulaService) {
        this.siteUserRepository = siteUserRepository;
        this.formulaService = formulaService;
    }

    public SiteUser persistNewUser(SiteUser user) {
        return siteUserRepository.save(user);
    }

    public SiteUser findUserById(String username) {

        //sanity check: username exists
        if(!siteUserRepository.findById(username).isPresent())
            throw new UserException(USERNAME_NOT_FOUND);

        return siteUserRepository.findById(username)
                .orElseThrow(() ->
                        new UserException(USERNAME_NOT_FOUND));
    }

    public SiteUser updateUserPassword(String username, String newPassword) {

        //sanity check: username exists
        if(!siteUserRepository.findById(username).isPresent())
            throw new UserException(USERNAME_NOT_FOUND);

        return siteUserRepository.findById(username).map(
                user -> {
                    user.setPassword(newPassword);
                    return siteUserRepository.save(user);
                }).orElseThrow(() -> new UserException(USERNAME_NOT_FOUND)
        );
    }

    public void deleteUser(String username) {
        siteUserRepository.deleteById(username);
    }

    public List<Formula> getUsernameFavoritesSet(String username) {

        //sanity check: username exists
        if(!siteUserRepository.findById(username).isPresent())
            throw new UserException(USERNAME_NOT_FOUND);

        SiteUser siteUser = siteUserRepository.findById(username)
                .orElseThrow(() ->
                        new UserException(USERNAME_NOT_FOUND));

        return siteUser.getFavoritesList();
    }

    public SiteUser modifyUsernameFavoritesList(String username, List<String> newPositions) {

        //sanity check: username exists
        if(!siteUserRepository.findById(username).isPresent())
            throw new UserException(USERNAME_NOT_FOUND);

        return siteUserRepository.findById(username).map(
                user -> {
                    user.setFavoritesList(newPositions.stream()
                            .map(formula -> formulaService.getSingleFormulaInfo(formula)
                    ).collect(Collectors.toList()));
                    return siteUserRepository.save(user);
                }
        ).orElseThrow(() -> new UserException(USERNAME_NOT_FOUND));
    }

    public SiteUser saveFormulaToFavoritesList(String username, String formulaName) {

        //sanity check: username exists
        if(!siteUserRepository.findById(username).isPresent())
            throw new UserException(USERNAME_NOT_FOUND);

        //if SiteUser has no favorites create List and add formula
        if(siteUserRepository.findById(username).get().getFavoritesList() == null ||
        siteUserRepository.findById(username).get().getFavoritesList().size() == 0) {
            return siteUserRepository.findById(username).map(
                    user -> {
                        user.setFavoritesList(Stream.of(formulaService
                                .getSingleFormulaInfo(formulaName))
                                .collect(Collectors.toList()));

                        return siteUserRepository.save(user);
                    }).orElseThrow(() ->
                            new UserException(USERNAME_NOT_FOUND));
        }

        //make sure there are no duplicates in favorites list
        if(!siteUserRepository.findById(username).get()
                .getFavoritesList().stream().filter(
                        formula ->
                                formula.getFormulaName().equals(formulaName))
                .collect(Collectors.toList()).isEmpty()) {

            return siteUserRepository.save(siteUserRepository.findById(username)
                    .orElseThrow(() -> new UserException(USERNAME_NOT_FOUND)));
        }

        //add to favorites list
        return siteUserRepository.findById(username).map(
                user -> {
                    user.getFavoritesList()
                            .add(formulaService.getSingleFormulaInfo(formulaName));

                    return siteUserRepository.save(user);
                }
        ).orElseThrow(() -> new UserException(USERNAME_NOT_FOUND));
    }

    public SiteUser deleteAllFavorites(String username) {

        //sanity check: username exists
        if(!siteUserRepository.findById(username).isPresent())
            throw new UserException(USERNAME_NOT_FOUND);

        return siteUserRepository.findById(username).map(
                user -> {
                    user.setFavoritesList(Collections.emptyList());
                    return siteUserRepository.save(user);
                }
        ).orElseThrow(() -> new UserException(USERNAME_NOT_FOUND));
    }

    public SiteUser deleteSingleFormulaFromFavorite(String username, String formulaName) {

        //sanity check: username exists
        if(!siteUserRepository.findById(username).isPresent())
            throw new UserException(USERNAME_NOT_FOUND);

        return siteUserRepository.findById(username).map(
                user -> {
                    user.setFavoritesList(user.getFavoritesList().stream().filter(
                            formula ->
                                    !formula.getFormulaName().equals(formulaName)
                    ).collect(Collectors.toList()));

                    return siteUserRepository.save(user);
                }
        ).orElseThrow(() -> new UserException(USERNAME_NOT_FOUND));
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