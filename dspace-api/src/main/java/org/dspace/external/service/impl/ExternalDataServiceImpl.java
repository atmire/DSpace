package org.dspace.external.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
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

    private ExternalDataProvider getExternalDataProviderForSource(String source) {
        for (ExternalDataProvider externalDataProvider : externalDataProviders) {
            if (externalDataProvider.supports(source)) {
                return externalDataProvider;
            }
        }
        return null;
    }

    public List<ExternalDataProvider> getExternalDataProviders() {
        return externalDataProviders;
    }

    public ExternalDataProvider getExternalDataProvider(String sourceIdentifier) {
        for (ExternalDataProvider externalDataProvider : externalDataProviders) {
            if (StringUtils.equalsIgnoreCase(externalDataProvider.getSourceIdentifier(), sourceIdentifier)) {
                return externalDataProvider;
            }
        }
        return null;
    }

}
