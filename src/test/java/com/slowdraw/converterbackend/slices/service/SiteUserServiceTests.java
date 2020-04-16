package com.slowdraw.converterbackend.slices.service;

import com.slowdraw.converterbackend.controller.SiteUserController;
import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.domain.Role;
import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.exception.UserException;
import com.slowdraw.converterbackend.repository.SiteUserRepository;
import com.slowdraw.converterbackend.service.FormulaService;
import com.slowdraw.converterbackend.service.SiteUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class SiteUserServiceTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteUserServiceTests.class);

    @Mock
    private SiteUserRepository siteUserRepository;

    @Mock
    private FormulaService formulaService;

    @InjectMocks
    private SiteUserService siteUserService;

    private SiteUser testUser;

    @BeforeEach
    void initSiteUserService() {
        //siteUserService = new SiteUserService(siteUserRepository, formulaService);
        testUser = SiteUser.builder()
                .username("testUsername").password("testPassword")
                .email("test@email.com").favoritesList(new ArrayList<Formula>())
                .roles(new HashSet<Role>()).build();
    }

    @Test
    void testFindUserById() {
        when(siteUserRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testUser));
        SiteUser newUser = siteUserService.findUserById("testUsername");
        assertThat(newUser).isNotNull();
        LOGGER.info(newUser.toString());
    }

    @Test
    void testFindUserByIdNotFoundThrowsException() {
        when(siteUserRepository.findById(any(String.class)))
                .thenThrow(UserException.class);
        UserException exceptMe = Assertions.assertThrows(UserException.class, () -> {
            siteUserService.findUserById("testUsername");
        });
    }


}
