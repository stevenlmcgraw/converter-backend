package com.slowdraw.converterbackend.controller;

import com.slowdraw.converterbackend.assembler.SiteUserEntityModelAssembler;
import com.slowdraw.converterbackend.exception.FormulaException;
import com.slowdraw.converterbackend.payload.SiteUserSummary;
import com.slowdraw.converterbackend.security.CurrentSiteUser;
import com.slowdraw.converterbackend.security.UserPrincipal;
import com.slowdraw.converterbackend.service.SiteUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class SiteUserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteUserController.class);

    private SiteUserService siteUserService;
    private SiteUserEntityModelAssembler siteUserEntityModelAssembler;

    //constructor injection
    public SiteUserController(SiteUserService siteUserService,
                              SiteUserEntityModelAssembler siteUserEntityModelAssembler) {
        this.siteUserService = siteUserService;
        this.siteUserEntityModelAssembler = siteUserEntityModelAssembler;
    }

    @GetMapping("/currentUser")
    @PreAuthorize("hasRole('ROLE_USER')")
    public SiteUserSummary getCurrentUser(@CurrentSiteUser UserPrincipal currentUser) {

        return new SiteUserSummary(currentUser.getUsername(), currentUser.getEmail());
    }

    @GetMapping("/{username}")
    public EntityModel<?> getSiteUserProfile(@PathVariable(value = "username") String username) {

        if(siteUserService.getUsernameFavoritesList(username).isEmpty()) {
            throw new FormulaException("Username has no favorites stored. Sorry mang.");
        }

        return new EntityModel<>(siteUserEntityModelAssembler
                .toModel(siteUserService.findUserById(username)));
    }

    @PostMapping("/{username}/favorites/{formulaName}")
    public Object addFormulaToSiteUserFavoritesSet(@PathVariable(value = "username") String username,
        @PathVariable(value = "formulaName") String formulaName) {

        return new EntityModel<>(siteUserEntityModelAssembler
                .toModel(siteUserService.saveFormulaToFavoritesSet(username, formulaName))
                );
    }

    @DeleteMapping("/{username}/favorites/delete/{formulaName}")
    public EntityModel<?> deleteFormulaFromUsernameFavorites(
            @PathVariable(value = "username") String username,
            @PathVariable(value = "formulaName") String formulaName) {

        return new EntityModel<>(siteUserEntityModelAssembler
                .toModel(siteUserService.deleteSingleFormulaFromFavorite(username, formulaName)));
    }

    @DeleteMapping("/{username}/favorites/delete")
    public EntityModel<?> deleteAllUsernameFavorites
            (@PathVariable(value = "username") String username) {

        return new EntityModel<>(siteUserEntityModelAssembler
                .toModel(siteUserService.deleteAllFavorites(username)));
    }
}
