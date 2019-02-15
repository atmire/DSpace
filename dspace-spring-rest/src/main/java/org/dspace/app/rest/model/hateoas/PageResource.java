package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.PageRest;
import org.dspace.app.rest.model.hateoas.annotations.RelNameDSpaceResource;
import org.dspace.app.rest.utils.Utils;

/**
 * Page Rest HAL Resource. The HAL Resource wraps the REST Resource
 * adding support for the links and embedded resources
 *
 */
@RelNameDSpaceResource(PageRest.NAME)
public class PageResource extends DSpaceResource<PageRest> {

    public PageResource(PageRest data, Utils utils, String... rels) {
        super(data, utils, rels);
    }
}
