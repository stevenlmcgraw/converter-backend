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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SiteUserServiceUnitTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteUserServiceUnitTests.class);

    private static final String USERNAME_NOT_FOUND = "Username not found.";

    @Mock
    private SiteUserRepository siteUserRepository;

    @Mock
    private FormulaService formulaService;

    @Mock
    private FormulasRepository formulasRepository;

    @InjectMocks
    private SiteUserService siteUserService;

    private SiteUser testUser;

    private List<Formula> testFormulaList;

    @BeforeEach
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
    }

    @Test
    void testFindUserById() {
        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testUser));
        SiteUser newUser = siteUserService.findUserById(testUser.getUsername());
        assertThat(newUser).isNotNull();
        LOGGER.info(newUser.toString());
    }

    @Test
    void testFindUserByIdNotFoundThrowsException() {

        when(siteUserRepository.findById(any(String.class)))
                .thenThrow(new UserException(String.format(
                        USERNAME_NOT_FOUND,
                        testUser.getUsername())));

        Exception exceptMe = Assertions.assertThrows(UserException.class, () ->
                siteUserService.findUserById(testUser.getUsername()));

        Assertions.assertEquals(String.format(
                USERNAME_NOT_FOUND,
                testUser.getUsername()),
                exceptMe.getMessage());
    }

    @Test
    void testModifyUsernameFavoritesListWorks() {

        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testUser));

        lenient().when(siteUserRepository.save(any(SiteUser.class))).thenReturn(testUser);

        SiteUser verifyUserSaved =
                siteUserService
                        .modifyUsernameFavoritesList(
                                testUser.getUsername(),
                                testUser.getFavoritesList()
                                        .stream()
                                        .map(Formula::getFormulaName)
                                        .collect(Collectors.toList()));

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

        when(siteUserRepository.findById(any(String.class)))
                .thenThrow(new UserException(String.format(
                        USERNAME_NOT_FOUND,
                        testUser.getUsername())));

        Exception exceptMe = Assertions.assertThrows(UserException.class, () -> {
            siteUserService
                    .modifyUsernameFavoritesList(
                            testUser.getUsername(),
                            testUser.getFavoritesList()
                                    .stream()
                                    .map(Formula::getFormulaName)
                                    .collect(Collectors.toList()));
        });

        Assertions.assertEquals(String.format(
                USERNAME_NOT_FOUND,
                testUser.getUsername()),
                exceptMe.getMessage());
    }

    @Test
    public void testModifyUsernameFavoritesListThrowsExceptionWhenSavingUpdatedUser() {

        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testUser));

        lenient().when(siteUserRepository.save(any(SiteUser.class)))
                .thenThrow(new UserException(String.format(
                        USERNAME_NOT_FOUND,
                        testUser.getUsername())));

        Exception exceptMe = Assertions.assertThrows(UserException.class, () -> {
            siteUserService
                    .modifyUsernameFavoritesList(testUser.getUsername(),
                            testUser.getFavoritesList()
                                    .stream()
                                    .map(Formula::getFormulaName)
                                    .collect(Collectors.toList()));
        });

        Assertions.assertEquals(String.format(
                USERNAME_NOT_FOUND,
                testUser.getUsername()),
                exceptMe.getMessage());
    }

    @Test
    public void testSaveFormulaToFavoritesWorks() {

        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testUser));

        lenient().when(siteUserRepository.save(any(SiteUser.class)))
                .thenReturn(testUser);

        when(mock(SiteUserService.class)
                .saveFormulaToFavoritesList(any(String.class), any(String.class)))
                .thenReturn(testUser);

        SiteUser verifyUserSaved =
                siteUserService
                        .saveFormulaToFavoritesList(
                                testUser.getUsername(),
                                testUser.getFavoritesList().get(0).getFormulaName());

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
    public void testSaveFormulaToFavoritesListGivesUserExceptionUsernameNotFound() {

        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testUser));

        lenient().when(siteUserRepository.save(any(SiteUser.class)))
                .thenThrow(new UserException(String.format(
                        USERNAME_NOT_FOUND,
                        testUser.getUsername())));

        Exception exceptMe = Assertions.assertThrows(UserException.class, () -> {
            siteUserService
                    .saveFormulaToFavoritesList(testUser.getUsername(),
                            testUser.getFavoritesList().get(0).getFormulaName());
        });

        Assertions.assertEquals(String.format(
                USERNAME_NOT_FOUND,
                testUser.getUsername()),
                exceptMe.getMessage());
    }

    @Test
    void testDeleteAllFavoritesWorks() {

        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testUser));

        lenient().when(siteUserRepository.save(any(SiteUser.class)))
                .thenReturn(testUser);

        SiteUser verifyUserSaved =
                siteUserService
                        .deleteAllFavorites(testUser.getUsername());

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
    public void testDeleteAllFavoritesThrowsUserExceptionWithBadUsername() {

        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testUser));

        lenient().when(siteUserRepository.save(any(SiteUser.class)))
                .thenThrow(new UserException(String.format(
                        USERNAME_NOT_FOUND,
                        testUser.getUsername())));

        Exception exceptMe = Assertions.assertThrows(UserException.class, () -> {
            siteUserService.deleteAllFavorites(testUser.getUsername());
        });

        Assertions.assertEquals(String.format(
                USERNAME_NOT_FOUND,
                testUser.getUsername()),
                exceptMe.getMessage());
    }

    @Test
    void testDeleteSingleFormulaFromFavoritesWorks() {

        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testUser));

        lenient().when(siteUserRepository.save(any(SiteUser.class)))
                .thenReturn(testUser);

        SiteUser verifyUserSaved =
                siteUserService
                        .deleteSingleFormulaFromFavorite(
                                testUser.getUsername(),
                                testUser.getFavoritesList().get(0).getFormulaName());

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
    public void testDeleteSingleFormulaFromFavoritesThrowsUserExceptionWithBadUsername() {

        lenient().when(siteUserRepository.findById(any(String.class)))
                .thenReturn(Optional.ofNullable(testUser));

        lenient().when(siteUserRepository.save(any(SiteUser.class)))
                .thenThrow(new UserException(String.format(
                        USERNAME_NOT_FOUND,
                        testUser.getUsername())));

        Exception exceptMe = Assertions.assertThrows(UserException.class, () -> {
            siteUserService
                    .deleteSingleFormulaFromFavorite(
                            testUser.getUsername(),
                            testUser.getFavoritesList().get(0).getFormulaName());
        });

        Assertions.assertEquals(String.format(
                USERNAME_NOT_FOUND,
                testUser.getUsername()),
                exceptMe.getMessage());
    }

    @Test
    public void testCheckUsernameAvailabilityReturnsTrueIfAvailable() {

        when(siteUserRepository.existsById(any(String.class)))
                .thenReturn(false);

        Assertions.assertTrue(siteUserService.checkUsernameAvailability(testUser.getUsername()));
    }

    @Test
    public void testCheckUsernameAvailabilityReturnsFalseIfNotAvailable() {

        when(siteUserRepository.existsById(any(String.class)))
                .thenReturn(true);

        Assertions.assertFalse(siteUserService.checkUsernameAvailability(testUser.getUsername()));
    }

    @Test
    public void testCheckEmailAvailabilityReturnsTrueIfAvailable() {

        when(siteUserRepository.existsByEmail(any(String.class)))
                .thenReturn(false);

        Assertions.assertTrue(siteUserService.checkEmailAvailability(testUser.getEmail()));
    }

    @Test
    public void testCheckEmailAvailabilityReturnsFalseIfNotAvailable() {

        when(siteUserRepository.existsByEmail(any(String.class)))
                .thenReturn(true);

        Assertions.assertFalse(siteUserService.checkEmailAvailability(testUser.getEmail()));
    }
}
