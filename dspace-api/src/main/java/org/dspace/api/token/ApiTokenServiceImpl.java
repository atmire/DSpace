/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.api.token;

import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import org.dspace.api.token.service.ApiTokenService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.dspace.services.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;

public class ApiTokenServiceImpl implements ApiTokenService {

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private EPersonService epersonService;

    @Override
    public String getToken(Context context) {
        return configurationService.getProperty("api.token");
    }

    @Override
    public EPerson authenticate(Context context, HttpServletRequest request) throws SQLException {
        try {
            String apiUser = request.getHeader(API_USER_HEADER);
            String apiToken = request.getHeader(API_TOKEN_HEADER);

            if (!(apiUser == null || apiUser.isEmpty()) && !(apiToken == null || apiToken.isEmpty())) {
                if (apiToken.equals(getToken(context))) {
                    return epersonService.find(context, UUID.fromString(apiUser));
                }
            }
        } catch (IllegalArgumentException e) {
            // This error means UUID.fromString() was not able to parse the value inside apiUser
            return null;
        }
        return null;
    }
}
