/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.bulkedit;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.dspace.external.factory.ExternalServiceFactory;
import org.dspace.external.provider.ExternalDataProvider;
import org.dspace.external.service.ExternalDataService;

public class BulkEditUtils {

    private BulkEditUtils() {
    }

    protected static final ExternalDataService externalDataService = ExternalServiceFactory.getInstance()
                                                                                           .getExternalDataService();

    public static Optional<Pair<String, ExternalDataProvider>> getExternalDataProviderFromMdHeader(String mdHeader) {
        if (containsExternalProvider(mdHeader)) {
            String metadataHeader = StringUtils.substringAfter(mdHeader, ":");
            String sourceIdentifier = StringUtils.substringBefore(mdHeader, ":");
            Optional<ExternalDataProvider> providerOptional = externalDataService
                .getExternalDataProvider(sourceIdentifier);
            if (providerOptional.isPresent()) {
                return Optional.of(Pair.of(metadataHeader, providerOptional.get()));
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public static boolean containsExternalProvider(String md) {
        return StringUtils.contains(md, ":");

    }
}
