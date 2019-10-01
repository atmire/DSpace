package org.dspace.external.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.dspace.external.model.ExternalDataObject;
import org.dspace.external.provider.ExternalDataProvider;
import org.dspace.external.service.ExternalDataService;
import org.springframework.beans.factory.annotation.Autowired;

public class ExternalDataServiceImpl implements ExternalDataService {

    @Autowired
    private List<ExternalDataProvider> externalDataProviders;

    @Override
    public ExternalDataObject getExternalDataObject(String source, String id) {
        ExternalDataProvider provider = getExternalDataProviderForSource(source);
        if (provider == null) {
            return null;
        }
        return provider.getExternalDataObject(id);
    }

    @Override
    public List<ExternalDataObject> searchExternalDataObjects(String source, String query, int start, int limit) {
        ExternalDataProvider provider = getExternalDataProviderForSource(source);
        if (provider == null) {
            return null;
        }
        return provider.searchExternalDataObjects(query, start, limit);
    }

    @Override
    public List<String> getExternalSources() {
        return getExternalDataProviders().stream()
                                         .map(externalDataProvider -> externalDataProvider.getSourceIdentifier())
                                         .collect(Collectors.toList());
    }

    private ExternalDataProvider getExternalDataProviderForSource(String source) {
        for (ExternalDataProvider externalDataProvider : externalDataProviders) {
            if (externalDataProvider.supports(source)) {
                return externalDataProvider;
            }
        }
        return null;
    }

    private List<ExternalDataProvider> getExternalDataProviders() {
        return externalDataProviders;
    }

}
