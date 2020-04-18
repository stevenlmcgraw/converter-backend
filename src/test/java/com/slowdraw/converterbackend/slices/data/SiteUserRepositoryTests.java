package com.slowdraw.converterbackend.slices.data;

import com.slowdraw.converterbackend.domain.Formula;
import com.slowdraw.converterbackend.domain.Role;
import com.slowdraw.converterbackend.domain.SiteUser;
import com.slowdraw.converterbackend.repository.SiteUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@AutoConfigureDataMongo
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SiteUserRepositoryTests {

    @Autowired
    private SiteUserRepository siteUserRepository;

    @Autowired
    private MongoOperations mongoOperations;

    private SiteUser testUser;

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

        mongoOperations.insert(areaCircle, "formulas");
        mongoOperations.insert(pythagoreanTheorem, "formulas");
        mongoOperations.insert(mhzToMeters, "formulas");

        List<Formula> formulaList = new ArrayList<>();
        formulaList.add(areaCircle);
        formulaList.add(pythagoreanTheorem);
        formulaList.add(mhzToMeters);

        Role userRole = new Role();
        userRole.setUsername("testUsername");
        userRole.setRoleName("ROLE_USER");

        Set<Role> roles = new HashSet<Role>();

        testUser = SiteUser.builder()
                .username("testUsername")
                .password("testPassword")
                .email("test@email.com")
                .favoritesList(formulaList)
                .roles(roles)
                .build();

        mongoOperations.dropCollection(SiteUser.class);
        mongoOperations.insert(testUser, "users");
    }

    @Test
    public void testCreateSiteUserWorks() {

        SiteUser testUser2 = SiteUser.builder()
                .username("testUsername2")
                .password("testPassword2")
                .email("test2@email.com")
                .favoritesList(new ArrayList<>())
                .roles(new HashSet<>())
                .build();

        SiteUser persistUser = siteUserRepository.save(testUser2);

        assertThat(persistUser)
                .isNotNull()
                .isInstanceOf(SiteUser.class)
                .hasFieldOrPropertyWithValue("username", "testUsername2")
                .hasFieldOrPropertyWithValue("password", "testPassword2")
                .hasFieldOrPropertyWithValue("email", "test2@email.com")
                .hasFieldOrProperty("favoritesList")
                .hasFieldOrProperty("roles");
    }

    @Test
    public void testFindByIdWorks() {

        SiteUser fetchedUser = siteUserRepository.findById(testUser.getUsername()).get();

        assertThat(fetchedUser)
                .isNotNull()
                .isInstanceOf(SiteUser.class)
                .isEqualTo(testUser);
    }

    @Test
    public void testFindByIdForInvalidUsernameThrowsException() {

        Assertions.assertThrows(NoSuchElementException.class, () -> {
            siteUserRepository.findById("unknown").get();
        });
    }

    @Test
    public void testUpdatePersistedSiteUserWorks() {

        //make a new user
        SiteUser testUser3 = SiteUser.builder()
                .username("testUsername3")
                .password("testPassword3")
                .email("test3@email.com")
                .favoritesList(new ArrayList<>())
                .roles(new HashSet<>())
                .build();

        //persist user
        siteUserRepository.save(testUser3);

        //fetch freshly persisted user
        SiteUser fetchedUser = siteUserRepository.findById(testUser3.getUsername()).get();

        //may as well make assertions about our new user while we are here
        assertThat(fetchedUser)
                .isNotNull()
                .isInstanceOf(SiteUser.class)
                .isEqualTo(testUser3);

        //create some formulas to add to favoritesList
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

        List<Formula> formulaList = new ArrayList<>();
        formulaList.add(areaCircle);
        formulaList.add(pythagoreanTheorem);

        //we will put a Role in here too
        Role userRole = new Role();
        userRole.setUsername("testUsername3");
        userRole.setRoleName("ROLE_USER");

        Set<Role> roles = new HashSet<Role>();

        //make a change to the user persisted at top of method
        testUser3.setFavoritesList(formulaList);
        testUser3.setRoles(roles);

        //persist the updates to user persisted at top of method and store in new SiteUser
        SiteUser fetchedUserAfterChanges = siteUserRepository.save(testUser3);

        //make sure the fetchedUser prior to changes is not same as fetched user after changes
        assertThat(fetchedUser).isNotEqualTo(fetchedUserAfterChanges);

        //make sure the changes we made are what we pulled out of the hat
        assertThat(fetchedUserAfterChanges)
                .hasFieldOrPropertyWithValue("favoritesList", formulaList)
                .hasFieldOrPropertyWithValue("roles", roles);
    }

    @Test
    public void testDeleteUserWorks() {

        //the lord giveth
        SiteUser testUser4 = SiteUser.builder()
                .username("testUsername4")
                .password("testPassword4")
                .email("test4@email.com")
                .favoritesList(new ArrayList<>())
                .roles(new HashSet<>())
                .build();

        //the lord double checketh her creation persists
        assertThat(siteUserRepository.existsById(testUser4.getUsername()));

        //the lord taketh away
        siteUserRepository.delete(testUser4);

        //the lord asserteth
        assertThat(!siteUserRepository.existsById(testUser4.getUsername()));
    }

    @Test
    public void testAttemptToDeleteInvalidUsernameDoesNothing() {

        Assertions.assertDoesNotThrow(() -> NoSuchElementException.class);
    }
}
