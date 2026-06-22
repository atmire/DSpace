/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.api.token.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.dspace.AbstractIntegrationTestWithDatabase;
import org.dspace.api.token.ApiToken;
import org.dspace.api.token.ApiTokenServiceImpl;
import org.dspace.api.token.dao.ApiTokenDAO;
import org.dspace.api.token.dao.impl.ApiTokenDAOImpl;
import org.dspace.api.token.service.ApiTokenService;
import org.dspace.eperson.EPerson;
import org.dspace.utils.DSpace;
import org.junit.Before;
import org.junit.Test;

public class ApiTokenCLIIT extends AbstractIntegrationTestWithDatabase {

    private final ApiTokenService apiTokenService = new DSpace()
            .getServiceManager().getServiceByName(ApiTokenServiceImpl.class.getName(), ApiTokenServiceImpl.class);
    private final ApiTokenDAO apiTokenDAO = new DSpace()
            .getServiceManager().getServiceByName(ApiTokenDAOImpl.class.getName(), ApiTokenDAOImpl.class);

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        context.setCurrentUser(admin);
        apiTokenService.deleteAllByEPerson(context, admin);
    }

    @Test
    public void testCreateApiToken() throws Exception {
        String[] args = {"api-token", "--create", "--eperson", admin.getEmail()};

        int result = runDSpaceScript(args);
        assertEquals(0, result);

        assertTrue(isExpiryDateCorrectWithinASecond(
                getCreatedApiToken(admin), ApiTokenCLI.DEFAULT_EXPIRY_HOURS * 60 * 60));
    }

    @Test
    public void testDeleteApiToken() throws Exception {
        ApiToken apiToken = apiTokenService.create(context, admin, Date.from(Instant.now().plusSeconds(10)));

        String[] deleteArgs = {"api-token", "--delete", apiToken.getToken()};
        int result = runDSpaceScript(deleteArgs);
        assertEquals(0, result);
    }

    @Test
    public void testCreateApiTokenWithDifferentExpiryUnits() throws Exception {
        String[] mArgs = {"api-token", "--create", "--eperson", admin.getEmail(), "--expiry", "2m"};
        runDSpaceScript(mArgs);
        assertTrue(isExpiryDateCorrectWithinASecond(getCreatedApiToken(admin), 2 * 60));
        apiTokenService.deleteAllByEPerson(context, admin);

        String[] hArgs = {"api-token", "--create", "--eperson", admin.getEmail(), "--expiry", "3h"};
        runDSpaceScript(hArgs);
        assertTrue(isExpiryDateCorrectWithinASecond(getCreatedApiToken(admin), 3 * 60 * 60));
        apiTokenService.deleteAllByEPerson(context, admin);

        String[] dArgs = {"api-token", "--create", "--eperson", admin.getEmail(), "--expiry", "4d"};
        runDSpaceScript(dArgs);
        assertTrue(isExpiryDateCorrectWithinASecond(getCreatedApiToken(admin), 4 * 60 * 60 * 24));
        apiTokenService.deleteAllByEPerson(context, admin);
    }

    /**
     * Method to check if the ApiToken holds an expiry date within 1 second of the expected expiry date.
     * Using the script, the created and expiry date derive from different NOW timestamps.
     * This will cause the expiry date to not have the exact expected time.
     */
    private boolean isExpiryDateCorrectWithinASecond(ApiToken apiToken, int expiryTimeInSeconds) {
        Date expectedExpiry = Date.from(
                apiToken.getCreated().toInstant().plusSeconds(expiryTimeInSeconds));
        Date expiryPlusOneSecond = Date.from(
                apiToken.getExpiry().toInstant().plusSeconds(1));

        return apiToken.getExpiry().before(expectedExpiry) && expiryPlusOneSecond.after(apiToken.getExpiry());
    }

    private ApiToken getCreatedApiToken(EPerson ePerson) throws Exception {
        List<ApiToken> tokens = apiTokenDAO.findAllByEPerson(context, ePerson, -1, 0);
        assertEquals(1, tokens.size());
        return tokens.get(0);
    }
}
