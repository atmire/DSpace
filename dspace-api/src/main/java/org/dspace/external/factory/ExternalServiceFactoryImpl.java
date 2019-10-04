package org.dspace.external.factory;

import org.dspace.external.service.ExternalDataService;
import org.springframework.beans.factory.annotation.Autowired;

public class ExternalServiceFactoryImpl extends ExternalServiceFactory {

    @Autowired(required = true)
    private ExternalDataService externalDataService;

    public ExternalDataService getExternalDataService() {
        return externalDataService;
    }
}
