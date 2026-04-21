/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.api.token.service;

import java.sql.SQLException;

import jakarta.servlet.http.HttpServletRequest;
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
     * This method will retrieve the configured API token
     * @param context   The relevant DSpace context
     * @return The configured API token
     */
    String getToken(Context context);

    /**
     * This method will compare the configured headers to the headers in the given request
     * @param context   The relevant DSpace context
     * @param request   The relevant request
     * @return The EPerson that matches the request headers
     * @throws SQLException When something goes wrong while retrieving the EPerson
     */
    EPerson authenticate(Context context, HttpServletRequest request) throws SQLException, AuthorizeException;
}
