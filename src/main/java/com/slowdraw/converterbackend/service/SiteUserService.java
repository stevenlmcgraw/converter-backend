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

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SiteUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteUserService.class);

    private SiteUserRepository siteUserRepository;
    private FormulaService formulaService;

    public SiteUserService(SiteUserRepository siteUserRepository, FormulaService formulaService) {
        this.siteUserRepository = siteUserRepository;
        this.formulaService = formulaService;
    }

    public SiteUser persistNewUser(SiteUser user) {
        return siteUserRepository.save(user);
    }

    public SiteUser findUserById(String username) {

        return siteUserRepository.findById(username)
                .orElseThrow(() ->
                        new UserException("Username not found."));
    }

    public SiteUser updateUserPassword(String username, String newPassword) {
        return siteUserRepository.findById(username).map(
                user -> {
                    user.setPassword(newPassword);
                    return siteUserRepository.save(user);
                }).orElseThrow(() -> new UserException("Username not found.")
        );
    }

    public void deleteUser(String username) {
        siteUserRepository.deleteById(username);
    }

    public Set<Formula> getUsernameFavoritesSet(String username) {

        SiteUser siteUser = siteUserRepository.findById(username)
                .orElseThrow(() ->
                        new UserException("Username ain't a valid username, bud."));

        return siteUser.getFavoritesSet();
    }

    public SiteUser modifyUsernameFavoritesSet(String username, List<String> newPositions) {

        LOGGER.info("*@*@*@*SiteUserService modifyFaves() username is: " + username);
        LOGGER.info("*@*@*@*SiteUserService modifyFaves() list is: " + newPositions);

        return siteUserRepository.findById(username).map(
                user -> {
                    user.setFavoritesSet(user.getFavoritesSet().stream().map(
                            formula -> {
                                formula.setPosition(
                                        newPositions.indexOf(formula.getFormulaName()));
                                return formula;
                            }
                    ).collect(Collectors.toSet()));
                    return siteUserRepository.save(user);
                }
        ).orElseThrow(() -> new UserException("Username exists not!"));
    }

    public SiteUser saveFormulaToFavoritesSet(String username, String formulaName) {

        //if SiteUser has no favorites create Set
        if(siteUserRepository.findById(username).get().getFavoritesSet() == null |
        siteUserRepository.findById(username).get().getFavoritesSet().size() == 0) {
            return siteUserRepository.findById(username).map(
                    user -> {
                        user.setFavoritesSet(Stream.of(formulaService
                                .getSingleFormulaInfo(formulaName)).map(
                                        formula -> {
                                            formula.setPosition(0);
                                            return formula;
                                        })
                                .collect(Collectors.toSet()));

                        return siteUserRepository.save(user);
                    }).orElseThrow(() ->
                            new UserException("I'm sorry, but that username does not exist."));
        }

        //make sure there are no duplicates in favorites list
        if(!siteUserRepository.findById(username).get()
                .getFavoritesSet().stream().filter(
                        formula ->
                                formula.getFormulaName().equals(formulaName))
                .collect(Collectors.toList()).isEmpty()) {

            return siteUserRepository.save(siteUserRepository.findById(username)
                    .orElseThrow(() -> new UserException("Username not found.")));
        }

        //add to favorites list and set position
        return siteUserRepository.findById(username).map(
                user -> {
                    user.addFormulaToFavoritesSet(formulaService
                            .getSingleFormulaInfo(formulaName)).stream()
                            .filter(
                                    formula ->
                                            formula.getFormulaName().equals(formulaName))
                            .findAny().get().setPosition(user.getFavoritesSet().size() - 1);

                    return siteUserRepository.save(user);
                }
        ).orElseThrow(() -> new UserException("Username not found, partner."));
    }

    public SiteUser deleteAllFavorites(String username) {

        return siteUserRepository.findById(username).map(
                user -> {
                    user.removeAllFormulasFromFavoritesSet();
                    return siteUserRepository.save(user);
                }
        ).orElseThrow(() -> new UserException("Username does not exist, dude."));
    }

    public SiteUser deleteSingleFormulaFromFavorite(String username, String formulaName) {

        return siteUserRepository.findById(username).map(
                user -> {
                    user.removeFormulaFromFavoritesSet(
                            formulaService.getSingleFormulaInfo(formulaName));
                    return siteUserRepository.save(user);
                }
        ).orElseThrow(() -> new UserException("Username not found."));
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
