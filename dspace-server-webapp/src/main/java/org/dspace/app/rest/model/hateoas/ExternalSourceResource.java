package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.ExternalSourceRest;
import org.dspace.app.rest.model.hateoas.annotations.RelNameDSpaceResource;

@RelNameDSpaceResource(ExternalSourceRest.NAME)
public class ExternalSourceResource extends HALResource<ExternalSourceRest> {
    public ExternalSourceResource(ExternalSourceRest content) {
        super(content);
    }
}
