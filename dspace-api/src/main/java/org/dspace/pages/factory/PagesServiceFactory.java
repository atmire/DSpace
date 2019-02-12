/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.pages.factory;

import org.dspace.pages.service.PageService;
import org.dspace.services.factory.DSpaceServicesFactory;

/**
 * Abstract factory to get services for the Pages package, use PagesServiceFactory.getInstance() to retrieve an
 * implementation
 */
public abstract class PagesServiceFactory {

    /**
     * This method will return a proper PageService implementation object
     * @return  The PageService
     */
    public abstract PageService getPageService();

    /**
     * This method will return a PagesServiceFactory on which we can call the services
     * @return  The proper PagesServiceFactory
     */
    public static PagesServiceFactory getInstance() {
        return DSpaceServicesFactory.getInstance().getServiceManager()
                                    .getServiceByName("pagesServiceFactory", PagesServiceFactory.class);
    }
}
