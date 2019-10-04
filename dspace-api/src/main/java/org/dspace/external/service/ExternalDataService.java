package org.dspace.external.service;

import java.util.List;
import java.util.Optional;

import org.dspace.external.model.ExternalDataObject;
import org.dspace.external.provider.ExternalDataProvider;

public interface ExternalDataService {

    public List<ExternalDataProvider> getExternalDataProviders();

    public ExternalDataProvider getExternalDataProvider(String sourceIdentifier);

    public Optional<ExternalDataObject> getExternalDataObject(String source, String identifier);

    public List<ExternalDataObject> searchExternalDataObjects(String source, String query, int start, int limit);

}
