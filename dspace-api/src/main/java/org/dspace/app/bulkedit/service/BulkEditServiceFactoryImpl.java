/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.bulkedit.service;

import org.dspace.app.bulkedit.DSpaceCSV;
import org.dspace.services.factory.DSpaceServicesFactory;

public class BulkEditServiceFactoryImpl extends BulkEditServiceFactory {
    @Override
    public BulkEditParsingService<DSpaceCSV> getCSVBulkEditRegisterService() {
        return DSpaceServicesFactory.getInstance().getServiceManager()
            .getServiceByName("csvBulkEditRegisterService", CSVBulkEditParsingServiceImpl.class);
    }

    @Override
    public BulkEditService getBulkEditImportService() {
        return DSpaceServicesFactory.getInstance().getServiceManager()
            .getServiceByName("bulkEditImportService", BulkEditService.class);
    }
}
