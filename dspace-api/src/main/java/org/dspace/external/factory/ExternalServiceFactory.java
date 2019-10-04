package org.dspace.external.factory;

import org.dspace.external.service.ExternalDataService;
import org.dspace.services.factory.DSpaceServicesFactory;

public abstract class ExternalServiceFactory {

    public abstract ExternalDataService getExternalDataService();


    public static ExternalServiceFactory getInstance() {
        return DSpaceServicesFactory.getInstance().getServiceManager()
                                    .getServiceByName("externalServiceFactory", ExternalServiceFactory.class);
    }
}
