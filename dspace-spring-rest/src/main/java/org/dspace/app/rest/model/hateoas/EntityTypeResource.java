package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.EntityTypeRest;
import org.dspace.app.rest.model.hateoas.annotations.RelNameDSpaceResource;
import org.dspace.app.rest.utils.Utils;

@RelNameDSpaceResource(EntityTypeRest.NAME)
public class EntityTypeResource extends DSpaceResource<EntityTypeRest> {
    public EntityTypeResource(EntityTypeRest data, Utils utils, String... rels) {
        super(data, utils, rels);
    }
}
