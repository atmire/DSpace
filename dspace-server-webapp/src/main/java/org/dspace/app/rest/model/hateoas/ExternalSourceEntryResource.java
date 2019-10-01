package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.ExternalSourceEntryRest;
import org.dspace.app.rest.model.hateoas.annotations.RelNameDSpaceResource;

@RelNameDSpaceResource(ExternalSourceEntryRest.NAME)
public class ExternalSourceEntryResource extends HALResource<ExternalSourceEntryRest> {
    public ExternalSourceEntryResource(ExternalSourceEntryRest content) {
        super(content);
    }
}
