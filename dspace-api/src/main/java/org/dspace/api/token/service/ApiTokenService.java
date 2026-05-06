/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.api.token.service;

import java.sql.SQLException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import org.dspace.api.token.ApiToken;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

/**
 * An interface for the ApiTokenService with methods regarding the ApiToken workload
 */
public interface ApiTokenService {

    String API_USER_HEADER = "X-Api-User";
    String API_TOKEN_HEADER = "X-Api-Token";

    /**
     * This method finds all API tokens based the given user
     * and returns the first token that matches with the given token
     * @param context   The relevant DSpace context
     * @param ePerson   The EPerson for which to look for a linked matching token
     * @param token     The token that should match with a token linked to the given EPerson
     * @return The first matching API token linked to the given eperson or null
     */
    ApiToken find(Context context, EPerson ePerson, String token) throws SQLException;

    /**
     * This method creates an ApiToken for a given EPerson with a given expiry date
     * @param context   The relevant DSpace context
     * @param ePerson   The EPerson for which to create the API token
     * @param expiry    The date when the token should no longer be usable
     * @return  The newly created API token
     */
    ApiToken create(Context context, EPerson ePerson, Instant expiry) throws SQLException;

    /**
     * This method deletes a given token from the database
     * @param context   The relevant DSpace context
     * @param apiToken  The API token to delete
     */
    void delete(Context context, ApiToken apiToken) throws SQLException;

    /**
     * This method deletes all API tokens linked to a certain EPerson
     * @param context   The relevant DSpace context
     * @param ePerson   The EPerson for which all API token need to be deleted
     */
    void deleteAllByEPerson(Context context, EPerson ePerson) throws SQLException;

    /**
     * This method will compare the configured headers to the headers in the given request
     * @param context   The relevant DSpace context
     * @param request   The relevant request
     * @return The EPerson that matches the request headers
     */
    EPerson authenticate(Context context, HttpServletRequest request) throws SQLException, AuthorizeException;
}
