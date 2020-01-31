/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.requestitem.factory;

import org.dspace.app.requestitem.service.RequestItemService;
import org.dspace.services.factory.DSpaceServicesFactory;

/**
 * Abstract factory to get services for the requestitem package, use RequestItemServiceFactory.getInstance() to
 * retrieve an implementation
 *
 * @author kevinvandevelde at atmire.com
 */
public abstract class RequestItemServiceFactory {

    private static RequestItemServiceFactory requestItemServiceFactory;

    public abstract RequestItemService getRequestItemService();

    public static RequestItemServiceFactory getInstance() {
        if (requestItemServiceFactory == null) {
            requestItemServiceFactory = DSpaceServicesFactory.getInstance().getServiceManager()
                    .getServiceByName("requestItemServiceFactory", RequestItemServiceFactory.class);
        }
        return requestItemServiceFactory;
    }
}
