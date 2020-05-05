package com.slowdraw.converterbackend.unit.service;

import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.domain.Role;
import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.exception.UserException;
import com.slowdraw.converterbackend.repository.FormulasRepository;
import com.slowdraw.converterbackend.repository.SiteUserRepository;
import com.slowdraw.converterbackend.service.FormulaService;
import com.slowdraw.converterbackend.service.SiteUserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SiteUserServiceTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteUserServiceTests.class);

    private static final String USERNAME_NOT_FOUND = "Username not found.";

    @Mock
    private SiteUserRepository siteUserRepository;

    @Mock
    private FormulaService formulaService;

    @Mock
    private FormulasRepository formulasRepository;

    @InjectMocks
    private SiteUserService siteUserService;

//    @Autowired
//    private MongoOperations mongoOperations;

    private SiteUser testUser;

    private List<Formula> testFormulaList;

    @BeforeAll
    void initSiteUserService() {

        Formula areaCircle = Formula.builder()
                .formulaName("areaCircle")
                .formulaUrl("/areaCircle")
                .category("Mathematics")
                .displayName("Area of a Circle")
                .build();

        Formula pythagoreanTheorem = Formula.builder()
                .formulaName("pythagoreanTheorem")
                .formulaUrl("/pythagoreanTheorem")
                .category("Mathematics")
                .displayName("Pythagorean Theorem")
                .build();

        Formula mhzToMeters = Formula.builder()
                .formulaName("mhzToMeters")
                .formulaUrl("/mhzToMeters")
                .category("Physics")
                .displayName("MHz to Meters")
                .build();

        testFormulaList = new ArrayList<>();
        testFormulaList.add(areaCircle);
        testFormulaList.add(pythagoreanTheorem);
        testFormulaList.add(mhzToMeters);

        Role userRole = new Role();
        userRole.setUsername("testUsername");
        userRole.setRoleName("ROLE_USER");

        Set<Role> roles = new HashSet<Role>();
        roles.add(userRole);

        testUser = SiteUser.builder()
                .username("testUsername")
                .password("testPassword")
                .email("test@email.com")
                .favoritesList(testFormulaList)
                .roles(roles)
                .build();

        //siteUserRepository.save(testUser);
    }

    @Test
    void testFindUserById() {
        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testUser));
        SiteUser newUser = siteUserService.findUserById("testUsername");
        assertThat(newUser).isNotNull();
        LOGGER.info(newUser.toString());
    }

    @Test
    void testFindUserByIdNotFoundThrowsException() {
        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenThrow(UserException.class);
        Assertions.assertThrows(UserException.class, () -> {
            siteUserService.findUserById("testUsername");
        });
    }

    @Test
    void testModifyUsernameFavoritesListWorks() {

        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testUser));

        lenient().when(siteUserRepository.save(any(SiteUser.class))).thenReturn(testUser);

        SiteUser verifyUserSaved =
                siteUserService
                        .modifyUsernameFavoritesList(testUser.getUsername(), new ArrayList<>());

        assertThat(verifyUserSaved)
                .isNotNull()
                .isInstanceOf(SiteUser.class)
                .hasFieldOrPropertyWithValue("username", testUser.getUsername())
                .hasFieldOrPropertyWithValue("password", testUser.getPassword())
                .hasFieldOrPropertyWithValue("email", testUser.getEmail())
                .hasFieldOrPropertyWithValue("favoritesList", testUser.getFavoritesList())
                .hasFieldOrPropertyWithValue("roles", testUser.getRoles());
    }

    @Test
    public void testModifyUsernameFavoritesListThrowsExceptionOnBadUsername() {

        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenThrow(new UserException(USERNAME_NOT_FOUND));

        Assertions.assertThrows(UserException.class, () -> {
            siteUserService.modifyUsernameFavoritesList("testUsername", new ArrayList<>());
        });
    }

    @Test
    public void testModifyUsernameFavoritesListThrowsExceptionWhenSavingUpdatedUser() {

        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testUser));

        lenient().when(siteUserRepository.save(any(SiteUser.class)))
                .thenThrow(new UserException(USERNAME_NOT_FOUND));

        Assertions.assertThrows(UserException.class, () -> {
            siteUserService.modifyUsernameFavoritesList("testUsername", new ArrayList<>());
        });
    }
}
