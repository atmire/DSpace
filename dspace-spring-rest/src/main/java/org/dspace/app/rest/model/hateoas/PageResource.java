/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.PageRest;
import org.dspace.app.rest.model.hateoas.annotations.RelNameDSpaceResource;
import org.dspace.app.rest.utils.Utils;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.springframework.hateoas.Link;

/**
 * Page Rest HAL Resource. The HAL Resource wraps the REST Resource
 * adding support for the links and embedded resources
 *
 */
@RelNameDSpaceResource(PageRest.NAME)
public class PageResource extends DSpaceResource<PageRest> {

    ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();

    public PageResource(PageRest data, Utils utils, String... rels) {
        super(data, utils, rels);
        add(new Link(configurationService.getProperty("dspace.restUrl") + "/api/" +
                         data.getCategory() + "/" + data.getTypePlural() +
                         "/" + data.getId() + "/content", "content"));
        add(new Link(configurationService.getProperty("dspace.restUrl") + "/api/" +
                         data.getCategory() + "/" + data.getTypePlural() +
                         "/" + data.getId() + "/languages", "languages"));
    }
}
