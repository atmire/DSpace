/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.api.token;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Logger;
import org.dspace.api.token.dao.ApiTokenDAO;
import org.dspace.api.token.service.ApiTokenService;
import org.dspace.authenticate.IPMatcher;
import org.dspace.authenticate.IPMatcherException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.PasswordHash;
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
    private AuthorizeService authorizeService;

    @Autowired
    private EPersonService epersonService;

    @Autowired
    private ClientInfoService clientInfoService;

    @Autowired
    private ApiTokenDAO apiTokenDAO;

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
    public ApiToken find(Context context, EPerson ePerson, String token) throws SQLException {
        List<ApiToken> userApiTokens = apiTokenDAO.findAllByEPerson(context, ePerson, -1, 0);
        for (ApiToken apiToken : userApiTokens) {
            PasswordHash apiTokenHash = null;
            try {
                apiTokenHash = new PasswordHash(
                        apiToken.getDigestAlgorithm(),
                        apiToken.getSalt(),
                        apiToken.getHash());
            } catch (DecoderException ex) {
                log.error(ex.getMessage());
            }

            if (apiTokenHash != null && apiTokenHash.matches(token)) {
                return apiToken;
            }
        }
        return null;
    }

    @Override
    public ApiToken create(Context context, EPerson ePerson, Instant expiry)
            throws SQLException {
        if (!this.authorizeWrite(context) || !isEPersonAllowed(ePerson)) {
            return null;
        }

        SecureRandom rng = new SecureRandom();
        byte [] salt = new byte[32];
        rng.nextBytes(salt);
        String randomApiToken = Base64.encodeBase64String(salt);
        PasswordHash hash = new PasswordHash(randomApiToken);

        ApiToken apiToken = new ApiToken(randomApiToken);

        apiToken.setHash(hash.getHashString());
        apiToken.setDigestAlgorithm(hash.getAlgorithm());
        apiToken.setSalt(hash.getSaltString());
        apiToken.setEPerson(ePerson);
        apiToken.setCreated(Instant.now());
        apiToken.setExpiry(expiry);

        return apiTokenDAO.create(context, apiToken);
    }

    @Override
    public void delete(Context context, ApiToken apiToken) throws SQLException {
        if (!this.authorizeWrite(context)) {
            return;
        }

        apiTokenDAO.delete(context, apiToken);
    }

    @Override
    public void deleteAllByEPerson(Context context, EPerson ePerson) throws SQLException {
        for (ApiToken apiToken: apiTokenDAO.findAllByEPerson(context, ePerson, -1, 0)) {
            this.delete(context, apiToken);
        }
    }

    @Override
    public EPerson authenticate(Context context, HttpServletRequest request) throws SQLException, AuthorizeException {
        String apiUser = request.getHeader(API_USER_HEADER);
        String apiToken = request.getHeader(API_TOKEN_HEADER);

        UUID apiUserId;
        try {
            if (!(apiUser == null || apiUser.isBlank())) {
                apiUserId = UUID.fromString(apiUser);
            } else {
                return null;
            }
        } catch (IllegalArgumentException e) {
            // This exception means UUID.fromString() was not able to parse the value inside the request header
            // Meaning the request header contained an invalid UUID
            return null;
        }

        if (!(apiToken == null || apiToken.isBlank())) {
            // Check if the given EPerson exists & the given is allowed to use an API token
            EPerson eperson = epersonService.find(context, apiUserId);
            if (eperson == null || !isEPersonAllowed(eperson)) {
                return null;
            }

            // Check if the request IP is allowed to use an API token
            if (!isIpAllowed(request)) {
                return null;
            }

            // Check if the given token exists for the given EPerson & the token is not expired
            ApiToken foundToken = this.find(context, eperson, apiToken);
            if (foundToken != null && foundToken.getExpiry().isAfter(Instant.now())) {
                return eperson;
            }
        }
        return null;
    }

    /**
     *
     * @param context
     * @return
     */
    protected boolean authorizeWrite(Context context) throws SQLException {
        return authorizeService.isAdmin(context);
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
