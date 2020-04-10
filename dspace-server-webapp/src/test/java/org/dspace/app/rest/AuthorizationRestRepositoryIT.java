/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.Serializable;
import java.util.UUID;

import com.jayway.jsonpath.matchers.JsonPathMatchers;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.authorization.AlwaysFalseFeature;
import org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature;
import org.dspace.app.rest.authorization.AlwaysTrueFeature;
import org.dspace.app.rest.authorization.Authorization;
import org.dspace.app.rest.authorization.AuthorizationFeature;
import org.dspace.app.rest.authorization.AuthorizationFeatureService;
import org.dspace.app.rest.authorization.TrueForAdminsFeature;
import org.dspace.app.rest.authorization.TrueForLoggedUsersFeature;
import org.dspace.app.rest.authorization.TrueForTestUsersFeature;
import org.dspace.app.rest.authorization.TrueForUsersInGroupTestFeature;
import org.dspace.app.rest.authorization.impl.LoginOnBehalfOfFeature;
import org.dspace.app.rest.builder.CommunityBuilder;
import org.dspace.app.rest.builder.EPersonBuilder;
import org.dspace.app.rest.builder.GroupBuilder;
import org.dspace.app.rest.converter.CommunityConverter;
import org.dspace.app.rest.converter.EPersonConverter;
import org.dspace.app.rest.converter.SiteConverter;
import org.dspace.app.rest.matcher.AuthorizationMatcher;
import org.dspace.app.rest.model.BaseObjectRest;
import org.dspace.app.rest.model.CommunityRest;
import org.dspace.app.rest.model.EPersonRest;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.model.SiteRest;
import org.dspace.app.rest.projection.DefaultProjection;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.app.rest.utils.Utils;
import org.dspace.content.Community;
import org.dspace.content.Site;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.SiteService;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.services.ConfigurationService;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test suite for the Authorization endpoint
 * 
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 *
 */
public class AuthorizationRestRepositoryIT extends AbstractControllerIntegrationTest {

    private static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(AuthorizationRestRepositoryIT.class);

    @Autowired
    private AuthorizationFeatureService authorizationFeatureService;

    @Autowired
    private SiteConverter siteConverter;

    @Autowired
    private EPersonConverter ePersonConverter;

    @Autowired
    private CommunityConverter communityConverter;
    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private Utils utils;

    @Autowired
    private SiteConverter siteConverter;

    @Autowired
    private CommunityConverter communityConverter;

    private SiteService siteService;

    /** 
     * this hold a reference to the test feature {@link AlwaysTrueFeature}
     */
    private AuthorizationFeature alwaysTrue;

    /** 
     * this hold a reference to the test feature {@link AlwaysFalseFeature}
     */
    private AuthorizationFeature alwaysFalse;

    /** 
     * this hold a reference to the test feature {@link AlwaysThrowExceptionFeature}
     */
    private AuthorizationFeature alwaysException;

    /** 
     * this hold a reference to the test feature {@link TrueForAdminsFeature}
     */
    private AuthorizationFeature trueForAdmins;

    /** 
     * this hold a reference to the test feature {@link TrueForLoggedUsersFeature}
     */
    private AuthorizationFeature trueForLoggedUsers;

    /** 
     * this hold a reference to the test feature {@link TrueForTestFeature}
     */
    private AuthorizationFeature trueForTestUsers;

    /** 
     * this hold a reference to the test feature {@link TrueForUsersInGroupTestFeature}
     */
    private AuthorizationFeature trueForUsersInGroupTest;

    /**
     * This holds a reference to the feature {@link LoginOnBehalfOfFeature}
     */
    private AuthorizationFeature loginOnBehalfOf;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        siteService = ContentServiceFactory.getInstance().getSiteService();
        alwaysTrue = authorizationFeatureService.find(AlwaysTrueFeature.NAME);
        alwaysFalse = authorizationFeatureService.find(AlwaysFalseFeature.NAME);
        alwaysException = authorizationFeatureService.find(AlwaysThrowExceptionFeature.NAME);
        trueForAdmins = authorizationFeatureService.find(TrueForAdminsFeature.NAME);
        trueForLoggedUsers = authorizationFeatureService.find(TrueForLoggedUsersFeature.NAME);
        trueForTestUsers = authorizationFeatureService.find(TrueForTestUsersFeature.NAME);
        trueForUsersInGroupTest = authorizationFeatureService.find(TrueForUsersInGroupTestFeature.NAME);
        loginOnBehalfOf = authorizationFeatureService.find(LoginOnBehalfOfFeature.NAME);
    }

    @Test
    /**
     * This method is not implemented
     *
     * @throws Exception
     */
    public void findAllTest() throws Exception {
        String adminToken = getAuthToken(admin.getEmail(), password);
        getClient(adminToken).perform(get("/api/authz/authorizations"))
                    .andExpect(status().isMethodNotAllowed());
        getClient().perform(get("/api/authz/authorizations"))
                    .andExpect(status().isMethodNotAllowed());
    }

    @Test
    /**
     * Verify that an user can access a specific authorization
     *
     * @throws Exception
     */
    public void findOneTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);

        // define three authorizations that we know must exists
        Authorization authAdminSite = new Authorization(admin, trueForAdmins, siteRest);
        Authorization authNormalUserSite = new Authorization(eperson, trueForLoggedUsers, siteRest);
        Authorization authAnonymousUserSite = new Authorization(null, alwaysTrue, siteRest);

        // access the authorization for the admin user
        String adminToken = getAuthToken(admin.getEmail(), password);
        getClient(adminToken).perform(get("/api/authz/authorizations/" + authAdminSite.getID()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$",
                            Matchers.is(AuthorizationMatcher.matchAuthorization(authAdminSite))));

        // access the authorization for a normal user
        String epersonToken = getAuthToken(eperson.getEmail(), password);
        getClient(epersonToken).perform(get("/api/authz/authorizations/" + authNormalUserSite.getID()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$",
                            Matchers.is(AuthorizationMatcher.matchAuthorization(authNormalUserSite))));

        // access the authorization for a normal user as administrator
        getClient(adminToken).perform(get("/api/authz/authorizations/" + authNormalUserSite.getID()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$",
                            Matchers.is(AuthorizationMatcher.matchAuthorization(authNormalUserSite))));

        // access the authorization for an anonymous user
        getClient().perform(get("/api/authz/authorizations/" + authAnonymousUserSite.getID()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$",
                            Matchers.is(AuthorizationMatcher.matchAuthorization(authAnonymousUserSite))));
    }

    @Test
    /**
     * Verify that the unauthorized return code is used in the appropriate scenarios
     *
     * @throws Exception
     */
    public void findOneUnauthorizedTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);

        // define two authorizations that we know must exists
        Authorization authAdminSite = new Authorization(admin, alwaysTrue, siteRest);
        Authorization authNormalUserSite = new Authorization(eperson, alwaysTrue, siteRest);

        // try anonymous access to the authorization for the admin user
        getClient().perform(get("/api/authz/authorizations/" + authAdminSite.getID()))
                    .andExpect(status().isUnauthorized());

        // try anonymous access to the authorization for a normal user
        getClient().perform(get("/api/authz/authorizations/" + authNormalUserSite.getID()))
                    .andExpect(status().isUnauthorized());
    }

    @Test
    /**
     * Verify that the forbidden return code is used in the appropriate scenarios
     *
     * @throws Exception
     */
    public void findOneForbiddenTest() throws Exception {
        context.turnOffAuthorisationSystem();
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        EPerson testEPerson = EPersonBuilder.createEPerson(context)
                .withEmail("test-authorization@example.com")
                .withPassword(password).build();
        context.restoreAuthSystemState();

        // define three authorizations that we know must exists
        Authorization authAdminSite = new Authorization(admin, alwaysTrue, siteRest);
        Authorization authNormalUserSite = new Authorization(eperson, alwaysTrue, siteRest);

        String testToken = getAuthToken(testEPerson.getEmail(), password);

        // try to access the authorization for the admin user with another user
        getClient(testToken).perform(get("/api/authz/authorizations/" + authAdminSite.getID()))
                    .andExpect(status().isForbidden());

        // try to access the authorization of a normal user with another user
        getClient(testToken).perform(get("/api/authz/authorizations/" + authNormalUserSite.getID()))
                    .andExpect(status().isForbidden());

        // check access as a test user to a not existing authorization for another
        // eperson (but existing for the test user)
        Authorization noTestAuthForNormalUserSite  = new Authorization(eperson, trueForTestUsers, siteRest);
        getClient(testToken).perform(get("/api/authz/authorizations/" + noTestAuthForNormalUserSite.getID()))
                    .andExpect(status().isForbidden());
    }

    @Test
    /**
     * Verify that the not found return code is used in the appropriate scenarios
     *
     * @throws Exception
     */
    public void findOneNotFoundTest() throws Exception {
        context.turnOffAuthorisationSystem();
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        EPersonRest epersonRest = ePersonConverter.convert(eperson, DefaultProjection.DEFAULT);
        context.restoreAuthSystemState();

        String epersonToken = getAuthToken(eperson.getEmail(), password);
        String adminToken = getAuthToken(admin.getEmail(), password);

        // define three authorizations that we know will be no granted
        Authorization authAdminSite = new Authorization(admin, alwaysFalse, siteRest);
        Authorization authNormalUserSite = new Authorization(eperson, alwaysFalse, siteRest);
        Authorization authAnonymousUserSite = new Authorization(null, alwaysFalse, siteRest);

        getClient(adminToken).perform(get("/api/authz/authorizations/" + authAdminSite.getID()))
                    .andExpect(status().isNotFound());

        getClient(epersonToken).perform(get("/api/authz/authorizations/" + authNormalUserSite.getID()))
                    .andExpect(status().isNotFound());
        // also the admin cannot retrieve a not existing authorization for the normal user
        getClient(epersonToken).perform(get("/api/authz/authorizations/" + authNormalUserSite.getID()))
                .andExpect(status().isNotFound());

        getClient().perform(get("/api/authz/authorizations/" + authAnonymousUserSite.getID()))
                    .andExpect(status().isNotFound());
        // also the admin cannot retrieve a not existing authorization for the anonymous user
        getClient(adminToken).perform(get("/api/authz/authorizations/" + authAnonymousUserSite.getID()))
                    .andExpect(status().isNotFound());

        // build a couple of IDs that look good but are related to not existing authorizations
        // the trueForAdmins feature is not defined for eperson
        String authInvalidType = getAuthorizationID(admin, trueForAdmins, epersonRest);
        getClient(adminToken).perform(get("/api/authz/authorizations/" + authInvalidType))
                    .andExpect(status().isNotFound());

        // the specified item doesn't exist
        String authNotExistingObject = getAuthorizationID(admin, alwaysTrue,
                ItemRest.CATEGORY + "." + ItemRest.NAME, UUID.randomUUID());
        getClient(adminToken).perform(get("/api/authz/authorizations/" + authNotExistingObject))
                    .andExpect(status().isNotFound());

        // the specified eperson doesn't exist
        String authNotExistingEPerson = getAuthorizationID(UUID.randomUUID(), alwaysTrue, siteRest);
        getClient(adminToken).perform(get("/api/authz/authorizations/" + authNotExistingEPerson))
                    .andExpect(status().isNotFound());

        // the specified feature doesn't exist
        String authNotExistingFeature = getAuthorizationID(admin, "notexistingfeature", siteRest);
        getClient(adminToken).perform(get("/api/authz/authorizations/" + authNotExistingFeature))
                    .andExpect(status().isNotFound());

        // check access as admin to a not existing authorization for another eperson (but existing for the admin)
        Authorization noAdminAuthForNormalUserSite  = new Authorization(eperson, trueForAdmins, siteRest);
        getClient(adminToken).perform(get("/api/authz/authorizations/" + noAdminAuthForNormalUserSite.getID()))
                    .andExpect(status().isNotFound());

        // check a couple of completely wrong IDs
        String notValidID = "notvalidID";
        getClient(adminToken).perform(get("/api/authz/authorizations/" + notValidID))
                    .andExpect(status().isNotFound());

        String notValidIDWithWrongEpersonPart = getAuthorizationID("1", alwaysTrue.getName(),
                SiteRest.CATEGORY + "." + SiteRest.NAME, site.getID().toString());
        // use the admin token otherwise it would result in a forbidden (attempt to access authorization of other users)
        getClient(adminToken).perform(get("/api/authz/authorizations/" + notValidIDWithWrongEpersonPart))
                    .andExpect(status().isNotFound());

        String notValidIDWithWrongObjectTypePart = getAuthorizationID(eperson.getID().toString(), alwaysTrue.getName(),
                "SITE", site.getID().toString());
        getClient(epersonToken).perform(get("/api/authz/authorizations/" + notValidIDWithWrongObjectTypePart))
                    .andExpect(status().isNotFound());

        String notValidIDWithUnknownObjectTypePart =
                getAuthorizationID(eperson.getID().toString(), alwaysTrue.getName(),
                        "core.unknown", "1");
        getClient(epersonToken).perform(get("/api/authz/authorizations/" + notValidIDWithUnknownObjectTypePart))
                    .andExpect(status().isNotFound());

    }

    @Test
    /**
     * Verify that an exception in the feature check will be reported back
     *
     * @throws Exception
     */
    public void findOneInternalServerErrorTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        // define two authorizations that we know will throw exceptions
        Authorization authAdminSite = new Authorization(admin, alwaysException, siteRest);
        Authorization authNormalUserSite = new Authorization(eperson, alwaysException, siteRest);

        String adminToken = getAuthToken(admin.getEmail(), password);
        String epersonToken = getAuthToken(eperson.getEmail(), password);

        getClient(adminToken).perform(get("/api/authz/authorizations/" + authAdminSite.getID()))
                    .andExpect(status().isInternalServerError());

        getClient(epersonToken).perform(get("/api/authz/authorizations/" + authNormalUserSite.getID()))
                    .andExpect(status().isInternalServerError());
    }

    @Test
    /**
     * Verify that the search by object works properly in allowed scenarios:
     * - for an administrator
     * - for an administrator that want to inspect permission of the anonymous users or another user
     * - for a logged-in "normal" user
     * - for anonymous
     * 
     * @throws Exception
     */
    public void findByObjectTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();

        // disarm the alwaysThrowExceptionFeature
        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);
        // verify that it works for administrators
        String adminToken = getAuthToken(admin.getEmail(), password);
        getClient(adminToken).perform(get("/api/authz/authorizations/search/object")
                .param("projection", "full")
                .param("uri", siteUri)
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isOk())
            // there are at least 3: alwaysTrue, trueForAdministrators and trueForLoggedUsers
            .andExpect(jsonPath("$._embedded.authorizations", Matchers.hasSize(greaterThanOrEqualTo(3))))
            .andExpect(jsonPath("$._embedded.authorizations", Matchers.everyItem(
                    Matchers.anyOf(
                            JsonPathMatchers.hasJsonPath("$.type", is("authorization")),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature",
                                    Matchers.allOf(
                                                is(alwaysTrue.getName()),
                                                is(trueForAdmins.getName()),
                                                is(trueForLoggedUsers.getName())
                                    )),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature",
                                    Matchers.not(Matchers.anyOf(
                                                is(alwaysFalse.getName()),
                                                is(alwaysException.getName()),
                                                is(trueForTestUsers.getName())
                                            )
                                    )),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature.resourcetypes",
                                    Matchers.hasItem(is("authorization"))),
                            JsonPathMatchers.hasJsonPath("$.id",
                                    Matchers.anyOf(
                                            Matchers.startsWith(admin.getID().toString()),
                                            Matchers.endsWith(siteRest.getUniqueType() + "_" + siteRest.getId()))))
                                    )
                    )
            )
            .andExpect(jsonPath("$._links.self.href",
                    Matchers.containsString("/api/authz/authorizations/search/object")))
            .andExpect(jsonPath("$.page.size", is(20)))
            .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(3)));

        // verify that it works for normal loggedin users
        String epersonToken = getAuthToken(eperson.getEmail(), password);
        getClient(epersonToken).perform(get("/api/authz/authorizations/search/object")
                .param("projection", "full")
                .param("uri", siteUri)
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isOk())
            // there are at least 2: alwaysTrue and trueForLoggedUsers
            .andExpect(jsonPath("$._embedded.authorizations", Matchers.hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$._embedded.authorizations", Matchers.everyItem(
                    Matchers.anyOf(
                            JsonPathMatchers.hasJsonPath("$.type", is("authorization")),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature",
                                    Matchers.allOf(
                                                is(alwaysTrue.getName()),
                                                is(trueForLoggedUsers.getName())
                                    )),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature",
                                    Matchers.not(Matchers.anyOf(
                                                is(alwaysFalse.getName()),
                                                is(alwaysException.getName()),
                                                is(trueForTestUsers.getName()),
                                                is(trueForAdmins.getName())
                                            )
                                    )),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature.resourcetypes",
                                    Matchers.hasItem(is("authorization"))),
                            JsonPathMatchers.hasJsonPath("$.id",
                                    Matchers.anyOf(
                                            Matchers.startsWith(eperson.getID().toString()),
                                            Matchers.endsWith(siteRest.getUniqueType() + "_" + siteRest.getId()))))
                                    )
                    )
            )
            .andExpect(jsonPath("$._links.self.href",
                    Matchers.containsString("/api/authz/authorizations/search/object")))
            .andExpect(jsonPath("$.page.size", is(20)))
            .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(2)));

        // verify that it works for administators inspecting other users
        getClient(adminToken).perform(get("/api/authz/authorizations/search/object")
                .param("projection", "full")
                .param("uri", siteUri)
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isOk())
            // there are at least 2: alwaysTrue and trueForLoggedUsers
            .andExpect(jsonPath("$._embedded.authorizations", Matchers.hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$._embedded.authorizations", Matchers.everyItem(
                    Matchers.anyOf(
                            JsonPathMatchers.hasJsonPath("$.type", is("authorization")),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature",
                                    Matchers.allOf(
                                                is(alwaysTrue.getName()),
                                                is(trueForLoggedUsers.getName())
                                    )),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature",
                                    Matchers.not(Matchers.anyOf(
                                                is(alwaysFalse.getName()),
                                                is(alwaysException.getName()),
                                                is(trueForTestUsers.getName()),
                                                // this guarantee that we are looking to the eperson
                                                // authz and not to the admin ones
                                                is(trueForAdmins.getName())
                                            )
                                    )),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature.resourcetypes",
                                    Matchers.hasItem(is("authorization"))),
                            JsonPathMatchers.hasJsonPath("$.id",
                                    Matchers.anyOf(
                                            // this guarantee that we are looking to the eperson
                                            // authz and not to the admin ones
                                            Matchers.startsWith(eperson.getID().toString()),
                                            Matchers.endsWith(siteRest.getUniqueType() + "_" + siteRest.getId()))))
                                    )
                    )
            )
            .andExpect(jsonPath("$._links.self.href",
                    Matchers.containsString("/api/authz/authorizations/search/object")))
            .andExpect(jsonPath("$.page.size", is(20)))
            .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(2)));

        // verify that it works for anonymous users
        getClient().perform(get("/api/authz/authorizations/search/object")
                .param("projection", "full")
                .param("uri", siteUri))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.authorizations", Matchers.hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$._embedded.authorizations", Matchers.everyItem(
                    Matchers.anyOf(
                            JsonPathMatchers.hasJsonPath("$.type", is("authorization")),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature",
                                    Matchers.allOf(
                                                is(alwaysTrue.getName())
                                    )),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature",
                                    Matchers.not(Matchers.anyOf(
                                                is(alwaysFalse.getName()),
                                                is(alwaysException.getName()),
                                                is(trueForTestUsers.getName()),
                                                is(trueForAdmins.getName())
                                            )
                                    )),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature.resourcetypes",
                                    Matchers.hasItem(is("authorization"))),
                            JsonPathMatchers.hasJsonPath("$.id",
                                    Matchers.anyOf(
                                            Matchers.startsWith(eperson.getID().toString()),
                                            Matchers.endsWith(siteRest.getUniqueType() + "_" + siteRest.getId()))))
                                    )
                    )
            )
            .andExpect(jsonPath("$._links.self.href",
                    Matchers.containsString("/api/authz/authorizations/search/object")))
            .andExpect(jsonPath("$.page.size", is(20)))
            .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(1)));

        // verify that it works for administrators inspecting anonymous users
        getClient(adminToken).perform(get("/api/authz/authorizations/search/object")
                .param("projection", "full")
                .param("uri", siteUri))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.authorizations", Matchers.hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$._embedded.authorizations", Matchers.everyItem(
                    Matchers.anyOf(
                            JsonPathMatchers.hasJsonPath("$.type", is("authorization")),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature",
                                    Matchers.allOf(
                                                is(alwaysTrue.getName())
                                    )),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature",
                                    Matchers.not(Matchers.anyOf(
                                                is(alwaysFalse.getName()),
                                                is(alwaysException.getName()),
                                                is(trueForTestUsers.getName()),
                                                is(trueForAdmins.getName())
                                            )
                                    )),
                            JsonPathMatchers.hasJsonPath("$._embedded.feature.resourcetypes",
                                    Matchers.hasItem(is("authorization"))),
                            JsonPathMatchers.hasJsonPath("$.id",
                                    Matchers.anyOf(
                                            Matchers.startsWith(eperson.getID().toString()),
                                            Matchers.endsWith(siteRest.getUniqueType() + "_" + siteRest.getId()))))
                                    )
                    )
            )
            .andExpect(jsonPath("$._links.self.href",
                    Matchers.containsString("/api/authz/authorizations/search/object")))
            .andExpect(jsonPath("$.page.size", is(20)))
            .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    /**
     * Verify that the findByObject return an empty page when the requested object doesn't exist but the uri is
     * potentially valid (i.e. deleted object)
     * 
     * @throws Exception
     */
    public void findByNotExistingObjectTest() throws Exception {
        String wrongSiteUri = "http://localhost/api/core/sites/" + UUID.randomUUID();

        // disarm the alwaysThrowExceptionFeature
        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);
        // verify that it works for administrators, no result
        String adminToken = getAuthToken(admin.getEmail(), password);
        getClient(adminToken).perform(get("/api/authz/authorizations/search/object")
                .param("uri", wrongSiteUri)
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", JsonPathMatchers.hasNoJsonPath("$._embedded.authorizations")))
            .andExpect(jsonPath("$._links.self.href",
                    Matchers.containsString("/api/authz/authorizations/search/object")))
            .andExpect(jsonPath("$.page.size", is(20)))
            .andExpect(jsonPath("$.page.totalElements", is(0)));

        // verify that it works for normal loggedin users
        String epersonToken = getAuthToken(eperson.getEmail(), password);
        getClient(epersonToken).perform(get("/api/authz/authorizations/search/object")
                .param("uri", wrongSiteUri)
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", JsonPathMatchers.hasNoJsonPath("$._embedded.authorizations")))
            .andExpect(jsonPath("$._links.self.href",
                    Matchers.containsString("/api/authz/authorizations/search/object")))
            .andExpect(jsonPath("$.page.size", is(20)))
            .andExpect(jsonPath("$.page.totalElements", is(0)));

        // verify that it works for administators inspecting other users
        getClient(adminToken).perform(get("/api/authz/authorizations/search/object")
                .param("uri", wrongSiteUri)
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", JsonPathMatchers.hasNoJsonPath("$._embedded.authorizations")))
            .andExpect(jsonPath("$._links.self.href",
                    Matchers.containsString("/api/authz/authorizations/search/object")))
            .andExpect(jsonPath("$.page.size", is(20)))
            .andExpect(jsonPath("$.page.totalElements", is(0)));

        // verify that it works for anonymous users
        getClient().perform(get("/api/authz/authorizations/search/object")
                .param("uri", wrongSiteUri))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", JsonPathMatchers.hasNoJsonPath("$._embedded.authorizations")))
            .andExpect(jsonPath("$._links.self.href",
                    Matchers.containsString("/api/authz/authorizations/search/object")))
            .andExpect(jsonPath("$.page.size", is(20)))
            .andExpect(jsonPath("$.page.totalElements", is(0)));

        // verify that it works for administrators inspecting anonymous users
        getClient(adminToken).perform(get("/api/authz/authorizations/search/object")
                .param("uri", wrongSiteUri))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", JsonPathMatchers.hasNoJsonPath("$._embedded.authorizations")))
            .andExpect(jsonPath("$._links.self.href",
                    Matchers.containsString("/api/authz/authorizations/search/object")))
            .andExpect(jsonPath("$.page.size", is(20)))
            .andExpect(jsonPath("$.page.totalElements", is(0)));
    }

    @Test
    /**
     * Verify that the findByObject return the 400 Bad Request response for invalid or missing URI (required parameter)
     * 
     * @throws Exception
     */
    public void findByObjectBadRequestTest() throws Exception {
        String[] invalidUris = new String[] {
                "invalid-uri",
                "",
                "http://localhost/api/wrongcategory/wrongmodel/1",
                "http://localhost/api/core/sites/this-is-not-an-uuid"
        };

        // disarm the alwaysThrowExceptionFeature
        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);

        String adminToken = getAuthToken(admin.getEmail(), password);
        String epersonToken = getAuthToken(eperson.getEmail(), password);
        for (String invalidUri : invalidUris) {
            log.debug("findByObjectBadRequestTest - Testing the URI: " + invalidUri);
            // verify that it works for administrators with an invalid or missing uri
            getClient(adminToken).perform(get("/api/authz/authorizations/search/object")
                    .param("uri", invalidUri)
                    .param("eperson", admin.getID().toString()))
                .andExpect(status().isBadRequest());

            // verify that it works for normal loggedin users with an invalid or missing uri
            getClient(epersonToken).perform(get("/api/authz/authorizations/search/object")
                    .param("uri", invalidUri)
                    .param("eperson", eperson.getID().toString()))
                .andExpect(status().isBadRequest());

            // verify that it works for administators inspecting other users with an invalid or missing uri
            getClient(adminToken).perform(get("/api/authz/authorizations/search/object")
                    .param("uri", invalidUri)
                    .param("eperson", eperson.getID().toString()))
                .andExpect(status().isBadRequest());

            // verify that it works for anonymous users with an invalid or missing uri
            getClient().perform(get("/api/authz/authorizations/search/object")
                    .param("uri", invalidUri))
                .andExpect(status().isBadRequest());

            // verify that it works for administrators inspecting anonymous users with an invalid or missing uri
            getClient(adminToken).perform(get("/api/authz/authorizations/search/object")
                    .param("uri", invalidUri))
                .andExpect(status().isBadRequest());
        }
        getClient(adminToken).perform(get("/api/authz/authorizations/search/object")
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isBadRequest());
        getClient(epersonToken).perform(get("/api/authz/authorizations/search/object")
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isBadRequest());
        getClient(adminToken).perform(get("/api/authz/authorizations/search/object")
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isBadRequest());
        getClient().perform(get("/api/authz/authorizations/search/object"))
            .andExpect(status().isBadRequest());
        getClient(adminToken).perform(get("/api/authz/authorizations/search/object"))
            .andExpect(status().isBadRequest());
    }

    @Test
    /**
     * Verify that the findByObject return the 401 Unauthorized response when an eperson is involved
     * 
     * @throws Exception
     */
    public void findByObjectUnauthorizedTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();

        // disarm the alwaysThrowExceptionFeature
        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);

        getClient().perform(get("/api/authz/authorizations/search/object")
                .param("uri", siteUri)
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isUnauthorized());

        // verify that it works for normal loggedin users with an invalid or missing uri
        getClient().perform(get("/api/authz/authorizations/search/object")
                .param("uri", siteUri)
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    /**
     * Verify that the findByObject return the 403 Forbidden response when a non-admin eperson try to search the
     * authorization of another eperson
     * 
     * @throws Exception
     */
    public void findByObjectForbiddenTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();
        context.turnOffAuthorisationSystem();
        EPerson anotherEperson = EPersonBuilder.createEPerson(context).withEmail("another@example.com")
                .withPassword(password).build();
        context.restoreAuthSystemState();
        // disarm the alwaysThrowExceptionFeature
        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);
        String anotherToken = getAuthToken(anotherEperson.getEmail(), password);
        // verify that he cannot search the admin authorizations
        getClient(anotherToken).perform(get("/api/authz/authorizations/search/object")
                .param("uri", siteUri)
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isForbidden());

        // verify that he cannot search the authorizations of another "normal" eperson
        getClient(anotherToken).perform(get("/api/authz/authorizations/search/object")
                .param("uri", siteUri)
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isForbidden());
    }

    @Test
    /**
     * Verify that an exception in the feature check will be reported back
     * @throws Exception
     */
    public void findByObjectInternalServerErrorTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();

        // verify that it works for administrators
        String adminToken = getAuthToken(admin.getEmail(), password);
        getClient(adminToken).perform(get("/api/authz/authorizations/search/object")
                .param("uri", siteUri)
                // use a large page so that the alwaysThrowExceptionFeature is invoked
                // this could become insufficient at some point
                .param("size", "100")
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isInternalServerError());

        // verify that it works for normal loggedin users
        String epersonToken = getAuthToken(eperson.getEmail(), password);
        getClient(epersonToken).perform(get("/api/authz/authorizations/search/object")
                .param("uri", siteUri)
                // use a large page so that the alwaysThrowExceptionFeature is invoked
                // this could become insufficient at some point
                .param("size", "100")
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isInternalServerError());

        // verify that it works for anonymous users
        getClient().perform(get("/api/authz/authorizations/search/object")
                .param("uri", siteUri)
                // use a large page so that the alwaysThrowExceptionFeature is invoked
                // this could become insufficient at some point
                .param("size", "100"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    /**
     * Verify that the search by object and feature works properly in allowed scenarios:
     * - for an administrator
     * - for an administrator that want to inspect permission of the anonymous users or another user
     * - for a logged-in "normal" user
     * - for anonymous
     * 
     * @throws Exception
     */
    public void findByObjectAndFeatureTest() throws Exception {
        context.turnOffAuthorisationSystem();
        Community com = CommunityBuilder.createCommunity(context).withName("A test community").build();
        CommunityRest comRest = communityConverter.convert(com, DefaultProjection.DEFAULT);
        String comUri = utils.linkToSingleResource(comRest, "self").getHref();
        context.restoreAuthSystemState();

        // verify that it works for administrators
        String adminToken = getAuthToken(admin.getEmail(), password);
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", comUri)
                .param("projection", "level")
                .param("embedLevelDepth", "1")
                .param("feature", alwaysTrue.getName())
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("authorization")))
            .andExpect(jsonPath("$._embedded.feature.id", is(alwaysTrue.getName())))
            .andExpect(jsonPath("$.id", Matchers.is(admin.getID().toString() + "_" + alwaysTrue.getName() + "_"
                    + comRest.getUniqueType() + "_" + comRest.getId())));

        // verify that it works for normal loggedin users
        String epersonToken = getAuthToken(eperson.getEmail(), password);
        getClient(epersonToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", comUri)
                .param("projection", "level")
                .param("embedLevelDepth", "1")
                .param("feature", alwaysTrue.getName())
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("authorization")))
            .andExpect(jsonPath("$._embedded.feature.id", is(alwaysTrue.getName())))
            .andExpect(jsonPath("$.id", Matchers.is(eperson.getID().toString() + "_" + alwaysTrue.getName() + "_"
                    + comRest.getUniqueType() + "_" + comRest.getId())));

        // verify that it works for administators inspecting other users
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", comUri)
                .param("projection", "level")
                .param("embedLevelDepth", "1")
                .param("feature", alwaysTrue.getName())
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("authorization")))
            .andExpect(jsonPath("$._embedded.feature.id", is(alwaysTrue.getName())))
            .andExpect(jsonPath("$.id", Matchers.is(eperson.getID().toString() + "_" + alwaysTrue.getName() + "_"
                    + comRest.getUniqueType() + "_" + comRest.getId())));

        // verify that it works for anonymous users
        getClient().perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", comUri)
                .param("projection", "level")
                .param("embedLevelDepth", "1")
                .param("feature", alwaysTrue.getName()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("authorization")))
            .andExpect(jsonPath("$._embedded.feature.id", is(alwaysTrue.getName())))
            .andExpect(jsonPath("$.id",Matchers.is(alwaysTrue.getName() + "_"
                    + comRest.getUniqueType() + "_" + comRest.getId())));

        // verify that it works for administrators inspecting anonymous users
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", comUri)
                .param("projection", "level")
                .param("embedLevelDepth", "1")
                .param("feature", alwaysTrue.getName()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("authorization")))
            .andExpect(jsonPath("$._embedded.feature.id", is(alwaysTrue.getName())))
            .andExpect(jsonPath("$.id",Matchers.is(alwaysTrue.getName() + "_"
                    + comRest.getUniqueType() + "_" + comRest.getId())));
    }

    @Test
    /**
     * Verify that the search by object and feature works return 204 No Content when a feature is not granted
     * 
     * @throws Exception
     */
    public void findByObjectAndFeatureNotGrantedTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();

        // verify that it works for administrators
        String adminToken = getAuthToken(admin.getEmail(), password);
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", alwaysFalse.getName())
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isNoContent());

        // verify that it works for normal loggedin users
        String epersonToken = getAuthToken(eperson.getEmail(), password);
        getClient(epersonToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForAdmins.getName())
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isNoContent());

        // verify that it works for administators inspecting other users
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForAdmins.getName())
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isNoContent());

        // verify that it works for anonymous users
        getClient().perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForLoggedUsers.getName()))
            .andExpect(status().isNoContent());

        // verify that it works for administrators inspecting anonymous users
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForLoggedUsers.getName()))
            .andExpect(status().isNoContent());
    }

    @Test
    /**
     * Verify that the findByObject return the 204 No Content code when the requested object doesn't exist but the uri
     * is potentially valid (i.e. deleted object) or the feature doesn't exist
     * 
     * @throws Exception
     */
    public void findByNotExistingObjectAndFeatureTest() throws Exception {
        String wrongSiteUri = "http://localhost/api/core/sites/" + UUID.randomUUID();
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();

        // disarm the alwaysThrowExceptionFeature
        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);
        // verify that it works for administrators, no result
        String adminToken = getAuthToken(admin.getEmail(), password);
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", wrongSiteUri)
                .param("feature", alwaysTrue.getName())
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isNoContent());

        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", "not-existing-feature")
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isNoContent());

        // verify that it works for normal loggedin users
        String epersonToken = getAuthToken(eperson.getEmail(), password);
        getClient(epersonToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", wrongSiteUri)
                .param("feature", alwaysTrue.getName())
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isNoContent());

        getClient(epersonToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", "not-existing-feature")
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isNoContent());

        // verify that it works for administators inspecting other users
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", wrongSiteUri)
                .param("feature", alwaysTrue.getName())
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isNoContent());

        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", "not-existing-feature")
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isNoContent());

        // verify that it works for anonymous users
        getClient().perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", wrongSiteUri)
                .param("feature", alwaysTrue.getName()))
            .andExpect(status().isNoContent());

        getClient().perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", "not-existing-feature"))
            .andExpect(status().isNoContent());

        // verify that it works for administrators inspecting anonymous users
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", wrongSiteUri)
                .param("feature", alwaysTrue.getName()))
            .andExpect(status().isNoContent());

        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", "not-existing-feature"))
            .andExpect(status().isNoContent());
    }

    @Test
    /**
     * Verify that the findByObject return the 400 Bad Request response for invalid or missing URI or feature (required
     * parameters)
     * 
     * @throws Exception
     */
    public void findByObjectAndFeatureBadRequestTest() throws Exception {
        String[] invalidUris = new String[] {
                "invalid-uri",
                "",
                "http://localhost/api/wrongcategory/wrongmodel/1",
                "http://localhost/api/core/sites/this-is-not-an-uuid"
        };
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();
        // disarm the alwaysThrowExceptionFeature
        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);

        String adminToken = getAuthToken(admin.getEmail(), password);
        String epersonToken = getAuthToken(eperson.getEmail(), password);
        for (String invalidUri : invalidUris) {
            log.debug("findByObjectAndFeatureBadRequestTest - Testing the URI: " + invalidUri);
            // verify that it works for administrators with an invalid or missing uri
            getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                    .param("uri", invalidUri)
                    .param("feature", alwaysTrue.getName())
                    .param("eperson", admin.getID().toString()))
                .andExpect(status().isBadRequest());

            // verify that it works for normal loggedin users with an invalid or missing uri
            getClient(epersonToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                    .param("uri", invalidUri)
                    .param("feature", alwaysTrue.getName())
                    .param("eperson", eperson.getID().toString()))
                .andExpect(status().isBadRequest());

            // verify that it works for administators inspecting other users with an invalid or missing uri
            getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                    .param("uri", invalidUri)
                    .param("feature", alwaysTrue.getName())
                    .param("eperson", eperson.getID().toString()))
                .andExpect(status().isBadRequest());

            // verify that it works for anonymous users with an invalid or missing uri
            getClient().perform(get("/api/authz/authorizations/search/objectAndFeature")
                    .param("uri", invalidUri)
                    .param("feature", alwaysTrue.getName()))
                .andExpect(status().isBadRequest());

            // verify that it works for administrators inspecting anonymous users with an invalid or missing uri
            getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                    .param("uri", invalidUri)
                    .param("feature", alwaysTrue.getName()))
                .andExpect(status().isBadRequest());
        }

        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isBadRequest());
        getClient(epersonToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isBadRequest());
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isBadRequest());
        getClient().perform(get("/api/authz/authorizations/search/objectAndFeature"))
            .andExpect(status().isBadRequest());
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature"))
            .andExpect(status().isBadRequest());

        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isBadRequest());
        getClient(epersonToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isBadRequest());
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isBadRequest());
        getClient().perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri))
            .andExpect(status().isBadRequest());
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri.toString()))
            .andExpect(status().isBadRequest());
    }

    @Test
    /**
     * Verify that the findByObjectAndFeature return the 401 Unauthorized response when an eperson is involved
     * 
     * @throws Exception
     */
    public void findByObjectAndFeatureUnauthorizedTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();

        // disarm the alwaysThrowExceptionFeature
        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);

        getClient().perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", alwaysTrue.getName())
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isUnauthorized());

        // verify that it works for normal loggedin users with an invalid or missing uri
        getClient().perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", alwaysTrue.getName())
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    /**
     * Verify that the findByObjectAndFeature return the 403 Forbidden response when a non-admin eperson try to search
     * the authorization of another eperson
     * 
     * @throws Exception
     */
    public void findByObjectAndFeatureForbiddenTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();
        context.turnOffAuthorisationSystem();
        EPerson anotherEperson = EPersonBuilder.createEPerson(context).withEmail("another@example.com")
                .withPassword(password).build();
        context.restoreAuthSystemState();
        // disarm the alwaysThrowExceptionFeature
        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);
        String anotherToken = getAuthToken(anotherEperson.getEmail(), password);
        // verify that he cannot search the admin authorizations
        getClient(anotherToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", alwaysTrue.getName())
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isForbidden());

        // verify that he cannot search the authorizations of another "normal" eperson
        getClient(anotherToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", alwaysTrue.getName())
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isForbidden());
    }

    @Test
    /**
     * Verify that an exception in the feature check will be reported back
     * @throws Exception
     */
    public void findByObjectAndFeatureInternalServerErrorTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();

        // verify that it works for administrators
        String adminToken = getAuthToken(admin.getEmail(), password);
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", alwaysException.getName())
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isInternalServerError());

        // verify that it works for normal loggedin users
        String epersonToken = getAuthToken(eperson.getEmail(), password);
        getClient(epersonToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", alwaysException.getName())
                .param("eperson", eperson.getID().toString()))
            .andExpect(status().isInternalServerError());

        // verify that it works for anonymous users
        getClient().perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", alwaysException.getName()))
            .andExpect(status().isInternalServerError());
    }

    @Test
    /**
     * This test will check that special group are correctly used to verify
     * authorization for the current loggedin user but not inherited from the
     * Administrators login when they look to authorization of third users
     * 
     * @throws Exception
     */
    public void verifySpecialGroupMembershipTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, DefaultProjection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();
        context.turnOffAuthorisationSystem();
        // create two normal users and put one in the test group directly
        EPerson memberOfTestGroup = EPersonBuilder.createEPerson(context).withEmail("memberGroupTest@example.com")
                .withPassword(password).build();
        EPerson normalUser = EPersonBuilder.createEPerson(context).withEmail("normal@example.com")
                .withPassword(password).build();
        Group testGroup = GroupBuilder.createGroup(context).withName(TrueForUsersInGroupTestFeature.GROUP_NAME)
                .addMember(memberOfTestGroup).build();
        context.restoreAuthSystemState();

        Authorization authAdminSite = new Authorization(admin, trueForUsersInGroupTest, siteRest);
        Authorization authMemberSite = new Authorization(memberOfTestGroup, trueForUsersInGroupTest, siteRest);
        Authorization authNormalUserSite = new Authorization(normalUser, trueForUsersInGroupTest, siteRest);

        String adminToken = getAuthToken(admin.getEmail(), password);
        String normalUserToken = getAuthToken(normalUser.getEmail(), password);
        String memberToken = getAuthToken(memberOfTestGroup.getEmail(), password);

        // proof that our admin doesn't have the special trueForUsersInGroupTest feature
        // check both via direct access than via a search method
        getClient(adminToken).perform(get("/api/authz/authorizations/" + authAdminSite.getID()))
            .andExpect(status().isNotFound());
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForUsersInGroupTest.getName())
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isNoContent());
        // nor the normal user both directly than if checked by the admin
        getClient(adminToken).perform(get("/api/authz/authorizations/" + authNormalUserSite.getID()))
            .andExpect(status().isNotFound());
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForUsersInGroupTest.getName())
                .param("eperson", normalUser.getID().toString()))
            .andExpect(status().isNoContent());
        getClient(normalUserToken).perform(get("/api/authz/authorizations/" + authNormalUserSite.getID()))
            .andExpect(status().isNotFound());
        getClient(normalUserToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForUsersInGroupTest.getName())
                .param("eperson", normalUser.getID().toString()))
            .andExpect(status().isNoContent());

        // instead the member user has
        getClient(adminToken).perform(get("/api/authz/authorizations/" + authMemberSite.getID()))
            .andExpect(status().isOk());
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForUsersInGroupTest.getName())
                .param("eperson", memberOfTestGroup.getID().toString()))
            .andExpect(status().isOk());
        // so it can also check itself the permission
        getClient(memberToken).perform(get("/api/authz/authorizations/" + authMemberSite.getID()))
            .andExpect(status().isOk());
        getClient(memberToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForUsersInGroupTest.getName())
                .param("eperson", memberOfTestGroup.getID().toString()))
            .andExpect(status().isOk());

        // now configure the password login to grant special membership to our test group and login again our users
        configurationService.setProperty("authentication-password.login.specialgroup",
                TrueForUsersInGroupTestFeature.GROUP_NAME);
        adminToken = getAuthToken(admin.getEmail(), password);
        normalUserToken = getAuthToken(normalUser.getEmail(), password);
        memberToken = getAuthToken(memberOfTestGroup.getEmail(), password);

        // our admin now should have the authorization
        getClient(adminToken).perform(get("/api/authz/authorizations/" + authAdminSite.getID()))
            .andExpect(status().isOk());
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForUsersInGroupTest.getName())
                .param("eperson", admin.getID().toString()))
            .andExpect(status().isOk());
        // our normal user when checked via the admin should still not have the authorization
        getClient(adminToken).perform(get("/api/authz/authorizations/" + authNormalUserSite.getID()))
            .andExpect(status().isNotFound());
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForUsersInGroupTest.getName())
                .param("eperson", normalUser.getID().toString()))
            .andExpect(status().isNoContent());
        // but he should have the authorization if loggedin directly
        getClient(normalUserToken).perform(get("/api/authz/authorizations/" + authNormalUserSite.getID()))
            .andExpect(status().isOk());
        getClient(normalUserToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForUsersInGroupTest.getName())
                .param("eperson", normalUser.getID().toString()))
            .andExpect(status().isOk());
        // for our direct member user we don't expect differences
        getClient(adminToken).perform(get("/api/authz/authorizations/" + authMemberSite.getID()))
            .andExpect(status().isOk());
        getClient(adminToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForUsersInGroupTest.getName())
                .param("eperson", memberOfTestGroup.getID().toString()))
            .andExpect(status().isOk());
        getClient(memberToken).perform(get("/api/authz/authorizations/" + authMemberSite.getID()))
            .andExpect(status().isOk());
        getClient(memberToken).perform(get("/api/authz/authorizations/search/objectAndFeature")
                .param("uri", siteUri)
                .param("feature", trueForUsersInGroupTest.getName())
                .param("eperson", memberOfTestGroup.getID().toString()))
            .andExpect(status().isOk());
    }

    // utility methods to build authorization ID without having an authorization object
    private String getAuthorizationID(EPerson eperson, AuthorizationFeature feature, BaseObjectRest obj) {
        return getAuthorizationID(eperson != null ? eperson.getID().toString() : null, feature.getName(),
                obj.getUniqueType(), obj.getId());
    }

    private String getAuthorizationID(UUID epersonUuid, AuthorizationFeature feature, BaseObjectRest obj) {
        return getAuthorizationID(epersonUuid != null ? epersonUuid.toString() : null, feature.getName(),
                obj.getUniqueType(), obj.getId());
    }

    private String getAuthorizationID(EPerson eperson, String featureName, BaseObjectRest obj) {
        return getAuthorizationID(eperson != null ? eperson.getID().toString() : null, featureName,
                obj.getUniqueType(), obj.getId());
    }

    private String getAuthorizationID(EPerson eperson, AuthorizationFeature feature, String objUniqueType,
            Serializable objID) {
        return getAuthorizationID(eperson != null ? eperson.getID().toString() : null, feature.getName(),
                objUniqueType, objID);
    }

    private String getAuthorizationID(String epersonUuid, String featureName, String type, Serializable id) {
        return (epersonUuid != null ? epersonUuid + "_" : "") + featureName + "_" + type + "_"
                + id.toString();
    }

    @Test
    public void loginOnBehalfOfTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, Projection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();

        String token = getAuthToken(admin.getEmail(), password);

        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);
        configurationService.setProperty("webui.user.assumelogin", true);

        Authorization loginOnBehalfOfAuthorization = new Authorization(admin, loginOnBehalfOf, siteRest);
        getClient(token).perform(get("/api/authz/authorizations/search/object")
                                .param("uri", siteUri)
                                .param("eperson", String.valueOf(admin.getID()))
                                .param("feature", loginOnBehalfOf.getName()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$._embedded.authorizations", Matchers.hasItem(
                        AuthorizationMatcher.matchAuthorization(loginOnBehalfOfAuthorization))));
    }

    @Test
    public void loginOnBehalfNonSiteObjectOfTest() throws Exception {
        context.turnOffAuthorisationSystem();
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        context.restoreAuthSystemState();

        CommunityRest communityRest = communityConverter.convert(parentCommunity, Projection.DEFAULT);
        String communityUri = utils.linkToSingleResource(communityRest, "self").getHref();

        String token = getAuthToken(admin.getEmail(), password);

        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);
        configurationService.setProperty("webui.user.assumelogin", true);

        Authorization loginOnBehalfOfAuthorization = new Authorization(admin, loginOnBehalfOf, communityRest);
        getClient(token).perform(get("/api/authz/authorizations/search/object")
                                     .param("uri", communityUri)
                                     .param("eperson", String.valueOf(admin.getID()))
                                     .param("feature", loginOnBehalfOf.getName()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.authorizations", Matchers.not(Matchers.hasItem(
                            AuthorizationMatcher.matchAuthorization(loginOnBehalfOfAuthorization)))));
    }

    @Test
    public void loginOnBehalfOfNonAdminUserNotFoundTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, Projection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();

        String token = getAuthToken(admin.getEmail(), password);

        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);
        configurationService.setProperty("webui.user.assumelogin", true);

        Authorization loginOnBehalfOfAuthorization = new Authorization(eperson, loginOnBehalfOf, siteRest);
        getClient(token).perform(get("/api/authz/authorizations/search/object")
                                     .param("uri", siteUri)
                                     .param("eperson", String.valueOf(eperson.getID()))
                                     .param("feature", loginOnBehalfOf.getName()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.authorizations", Matchers.not(
                            Matchers.hasItem(AuthorizationMatcher.matchAuthorization(loginOnBehalfOfAuthorization)))));
    }

    @Test
    public void loginOnBehalfOfNonAdminUserAssumeLoginPropertyFalseNotFoundTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, Projection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();

        String token = getAuthToken(admin.getEmail(), password);

        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);
        configurationService.setProperty("webui.user.assumelogin", false);

        Authorization loginOnBehalfOfAuthorization = new Authorization(eperson, loginOnBehalfOf, siteRest);
        getClient(token).perform(get("/api/authz/authorizations/search/object")
                                     .param("uri", siteUri)
                                     .param("eperson", String.valueOf(eperson.getID()))
                                     .param("feature", loginOnBehalfOf.getName()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.authorizations", Matchers.not(
                            Matchers.hasItem(AuthorizationMatcher.matchAuthorization(loginOnBehalfOfAuthorization)))));
    }

    @Test
    public void loginOnBehalfOfAssumeLoginPropertyFalseNotFoundTest() throws Exception {
        Site site = siteService.findSite(context);
        SiteRest siteRest = siteConverter.convert(site, Projection.DEFAULT);
        String siteUri = utils.linkToSingleResource(siteRest, "self").getHref();

        String token = getAuthToken(admin.getEmail(), password);

        configurationService.setProperty("org.dspace.app.rest.authorization.AlwaysThrowExceptionFeature.turnoff", true);
        configurationService.setProperty("webui.user.assumelogin", false);

        Authorization loginOnBehalfOfAuthorization = new Authorization(admin, loginOnBehalfOf, siteRest);
        getClient(token).perform(get("/api/authz/authorizations/search/object")
                                     .param("uri", siteUri)
                                     .param("eperson", String.valueOf(admin.getID()))
                                     .param("feature", loginOnBehalfOf.getName()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.authorizations", Matchers.not(
                            Matchers.hasItem(AuthorizationMatcher.matchAuthorization(loginOnBehalfOfAuthorization)))));
    }
}
