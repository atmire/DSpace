/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.sfx.factory;

import org.dspace.app.sfx.service.SFXFileReaderService;
import org.dspace.utils.DSpace;

/**
 * Factory implementation to get services for the sfx package, use SfxServiceFactory.getInstance() to retrieve an
 * implementation
 *
 * @author kevinvandevelde at atmire.com
 */
public class SfxServiceFactoryImpl extends SfxServiceFactory {

    @Override
    public SFXFileReaderService getSfxFileReaderService() {
        return new DSpace().getServiceManager().getServiceByName("SFXFileReaderService", SFXFileReaderService.class);
    }
}
