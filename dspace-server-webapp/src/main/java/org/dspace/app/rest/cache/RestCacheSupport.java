/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.app.rest.cache;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.dspace.services.ConfigurationService;
import org.dspace.services.RequestService;
import org.dspace.utils.DSpace;
import org.springframework.cache.interceptor.SimpleKey;

public class RestCacheSupport {
    private static final Logger log = Logger.getLogger(RestCacheSupport.class);

    private static final RequestService requestService = new DSpace().getRequestService();
    private static final boolean enabled = new DSpace().getConfigurationService()
                                                       .getBooleanProperty("rest.cache.enabled");

    private RestCacheSupport() {
    }

    public static Object getKey(Class<?> targetClass, String method, Object arguments) {
        try {
            return new SimpleKey(
                targetClass.getCanonicalName(), method, new ObjectMapper().writeValueAsString(arguments)
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to generate cache key from arguments passed to " + method + ". ", e);
            // we can't really "cancel" caching at this point, so ensure that we don't hit it later on 😬
            return UUID.randomUUID();
        }
    }

    public static boolean doCache() {
        return enabled && requestService.getCurrentRequest()
                                        .getHttpServletRequest()
                                        .getHeader("Authorization") == null;
    }
}
