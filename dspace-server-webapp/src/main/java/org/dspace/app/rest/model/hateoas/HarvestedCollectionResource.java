/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.HarvestedCollectionRest;
import org.dspace.app.rest.model.HarvesterMetadataRest;
import org.dspace.app.rest.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * HarvestedCollection Rest HAL Resource. The HAL Resource wraps the REST Resource
 * adding support for the links and embedded resources
 *
 * @author Jelle Pelgrims (jelle.pelgrims at atmire.com)
 */
public class HarvestedCollectionResource extends HALResource<HarvestedCollectionRest> {

    @Autowired
    private Utils utils;

    public HarvestedCollectionResource(HarvestedCollectionRest data) {
        super(data);
        embedResource("metadata_configs", data.getMetadataConfigs());
    }

    private void embedResource(String relationship, HarvesterMetadataRest harvesterMetadataRest) {
        HarvesterMetadataResource harvesterMetadataResource =
            new HarvesterMetadataResource(harvesterMetadataRest, utils);
        embedResource(relationship, harvesterMetadataResource);
    }

}