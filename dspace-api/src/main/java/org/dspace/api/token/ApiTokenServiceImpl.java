/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.api.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.Logger;
import org.dspace.api.token.service.ApiTokenService;
import org.dspace.authenticate.IPMatcher;
import org.dspace.authenticate.IPMatcherException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.dspace.service.ClientInfoService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ApiTokenServiceImpl implements ApiTokenService {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(ApiTokenServiceImpl.class);

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private EPersonService epersonService;

    @Autowired
    private ClientInfoService clientInfoService;

    /**
     * All the IP matchers
     */
    protected List<IPMatcher> ipMatchers;

    /**
     * All the negative IP matchers
     */
    protected List<IPMatcher> ipNegativeMatchers;

    protected ApiTokenServiceImpl() {
        this.parseApiTokenIpMatchers();
    }

    @Override
    public String getToken(Context context) {
        return configurationService.getProperty("api.token");
    }

    @Override
    public EPerson authenticate(Context context, HttpServletRequest request) throws SQLException, AuthorizeException {
        try {
            String apiUser = request.getHeader(API_USER_HEADER);
            String apiToken = request.getHeader(API_TOKEN_HEADER);

            if (!(apiUser == null || apiUser.isEmpty()) && !(apiToken == null || apiToken.isEmpty())) {
                if (MessageDigest.isEqual(apiToken.getBytes(StandardCharsets.UTF_8),
                                          getToken(context).getBytes(StandardCharsets.UTF_8))) {
                    if (!isIpAllowed(request)) {
                        return null;
                    }
                    EPerson eperson = epersonService.find(context, UUID.fromString(apiUser));
                    if (eperson != null && isEPersonAllowed(eperson)) {
                        return eperson;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            // This error means UUID.fromString() was not able to parse the value inside apiUser
            return null;
        }
        return null;
    }

    /**
     * This method calls the private parseApiTokenIpMatchers method
     * Is used to read new IP ranges from the configuration and parse them into {@link IPMatcher}s
     */
    public void resetIpMatchersCache() {
        this.parseApiTokenIpMatchers();
    }

    private void parseApiTokenIpMatchers() {
        String[] ipRanges = this.getIpRanges();

        if (ipRanges != null) {
            this.ipMatchers = new ArrayList<>();
            this.ipNegativeMatchers = new ArrayList<>();

            for (String ipRange : ipRanges) {
                IPMatcher ipm;
                try {
                    if (ipRange.startsWith("-")) {
                        ipm = new IPMatcher(ipRange.substring(1));
                        ipNegativeMatchers.add(ipm);
                    } else {
                        ipm = new IPMatcher(ipRange);
                        ipMatchers.add(ipm);
                    }
                } catch (IPMatcherException ipme) {
                    log.warn("Malformed API token IP range", ipme);
                }
            }
        } else  {
            this.ipMatchers = null;
            this.ipNegativeMatchers = null;
        }
    }

    private String[] getIpRanges() {
        ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();
        String[] ipRanges = configurationService.getArrayProperty("api.token.ipranges");

        // Array properties return an empty list when not configured
        // When this happens check if it is configured by retrieving the configuration property as a regular property
        if (ipRanges.length == 0 && configurationService.getProperty("api.token.ipranges") == null) {
            ipRanges = null;
        }
        return ipRanges;
    }

    private boolean isIpAllowed(HttpServletRequest request) throws AuthorizeException {
        if (this.ipMatchers == null && this.ipNegativeMatchers == null) {
            return true;
        } else if (
                (this.ipMatchers == null || this.ipMatchers.isEmpty()) &&
                (this.ipNegativeMatchers == null || this.ipNegativeMatchers.isEmpty())
        ) {
            return false;
        }

        String clientIP = clientInfoService.getClientIp(request);
        try {
            for (IPMatcher ipm : this.ipMatchers) {
                if (ipm.match(clientIP)) {
                    return true;
                }
            }
            for (IPMatcher ipm : this.ipNegativeMatchers) {
                if (ipm.match(clientIP)) {
                    return false;
                }
            }
        } catch (IPMatcherException ipme) {
            throw new AuthorizeException("Malformed IP address found attached to request: " + clientIP);
        }

        return false;
    }

    private boolean isEPersonAllowed(EPerson eperson) {
        String[] allowedEPersons = configurationService.getArrayProperty("api.token.email");
        if (allowedEPersons == null || allowedEPersons.length == 0) {
            return true;
        }
        return Arrays.stream(allowedEPersons)
                     .anyMatch(allowedEPersonEmail -> allowedEPersonEmail.equals(eperson.getEmail()));
    }
}
