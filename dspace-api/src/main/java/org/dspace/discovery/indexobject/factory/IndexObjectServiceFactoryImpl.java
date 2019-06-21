/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery.indexobject.factory;

import java.util.List;

import org.dspace.discovery.indexobject.service.IndexableObjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public class IndexObjectServiceFactoryImpl extends IndexObjectServiceFactory {

    @Autowired
    List<IndexableObjectService> indexableObjectServices;

    @Override
    public List<IndexableObjectService> getIndexableObjectServices() {
        return indexableObjectServices;
    }
}
