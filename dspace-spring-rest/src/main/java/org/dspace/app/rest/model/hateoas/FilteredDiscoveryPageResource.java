package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.FilteredDiscoveryPageRest;
import org.dspace.app.rest.model.hateoas.annotations.RelNameDSpaceResource;
import org.dspace.app.rest.utils.Utils;

@RelNameDSpaceResource(FilteredDiscoveryPageRest.NAME)
public class FilteredDiscoveryPageResource extends DSpaceResource<FilteredDiscoveryPageRest> {
    public FilteredDiscoveryPageResource(FilteredDiscoveryPageRest data, Utils utils,
                                         String... rels) {
        super(data, utils, rels);
    }
}
