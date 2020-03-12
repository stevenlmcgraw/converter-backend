package com.slowdraw.converterbackend.controller;

import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.payload.SiteUserProfile;
import com.slowdraw.converterbackend.payload.SiteUserSummary;
import com.slowdraw.converterbackend.repository.SiteUserRepository;
import com.slowdraw.converterbackend.security.CurrentSiteUser;
import com.slowdraw.converterbackend.security.UserPrincipal;
import com.slowdraw.converterbackend.service.SiteUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class SiteUserController {

    private SiteUserService siteUserService;

    //constructor injection
    public SiteUserController(SiteUserService siteUserService) {
        this.siteUserService = siteUserService;
    }

    @GetMapping("/currentUser")
    @PreAuthorize("hasRole('ROLE_USER')")
    public SiteUserSummary getCurrentUser(@CurrentSiteUser UserPrincipal currentUser) {

        return new SiteUserSummary(currentUser.getUsername(), currentUser.getEmail());
    }

    @GetMapping("/{username}")
    public SiteUserProfile getSiteUserProfile(@PathVariable(value = "username") String username) {

        SiteUser user = siteUserService.findUserById(username);

        return new SiteUserProfile(user.getUsername());
    }
}
