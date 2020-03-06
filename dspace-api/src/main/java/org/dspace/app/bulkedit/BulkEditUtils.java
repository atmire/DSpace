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

/**
 * This class contains util methods for BulkEdit functionality
 */
public class BulkEditUtils {

    private BulkEditUtils() {
    }

    protected static final ExternalDataService externalDataService = ExternalServiceFactory.getInstance()
                                                                                           .getExternalDataService();

    /**
     * This method will retrieve the ExternalDataProvider from the String given to it. It'll parse the ":" out of the
     * String and it'll try to find an ExternalDataProvider for it if it exists and return this
     * @param mdHeader  The String to be checked
     * @return          The Optional pair of metadataheader and ExternalDataProvider of it's found
     */
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

    /**
     * This method will return true or false depending on whether the String given to it contains an externalProvider
     * or not. This is typically found by the ":" in the String
     * @param md    the String to be checked
     * @return      A boolean value indicating whether an ExternalProvider is contained in the String or not
     */
    public static boolean containsExternalProvider(String md) {
        return StringUtils.contains(md, ":");

    }
}
