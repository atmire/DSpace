package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.RelationshipRest;
import org.dspace.app.rest.model.hateoas.annotations.RelNameDSpaceResource;
import org.dspace.app.rest.utils.Utils;

@RelNameDSpaceResource(RelationshipRest.NAME)
public class RelationshipResource extends DSpaceResource<RelationshipRest> {
    public RelationshipResource(RelationshipRest data, Utils utils, String... rels) {
        super(data, utils, rels);
    }
}
