/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.external.provider.impl;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.external.provider.ExternalDataProvider;

/**
 * @author Maria Verdonck (Atmire) on 12/05/2020
 */
public class BTEDataProvider implements ExternalDataProvider {

    private static final String METHOD_NOT_SUPPORTED_MESSAGE = "This method is not supported by the BTEDataProvider " +
                                                               "ExternalDataProvider";

    private String sourceIdentifier;

    @Override
    public String getSourceIdentifier() {
        return this.sourceIdentifier;
    }

    @Override
    public boolean supports(String source) {
        return StringUtils.equalsIgnoreCase(sourceIdentifier, source);
    }

    @Override
    public Optional<ExternalDataObject> getExternalDataObject(String id) {
        throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public List<ExternalDataObject> searchExternalDataObjects(String query, int start, int limit) {
        throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public int getNumberOfResults(String query) {
        throw new UnsupportedOperationException(METHOD_NOT_SUPPORTED_MESSAGE);
    }

    /**
     * Generic setter for the sourceIdentifier property
     *
     * @param sourceIdentifier the String sourceIdentifier to be set
     */
    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }
}
