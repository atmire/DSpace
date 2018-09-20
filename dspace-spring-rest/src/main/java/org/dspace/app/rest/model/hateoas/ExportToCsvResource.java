package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.ExportToCsvRest;
import org.dspace.app.rest.utils.Utils;

public class ExportToCsvResource extends HALResource<ExportToCsvRest> {

    public ExportToCsvResource(ExportToCsvRest data, Utils utils, String... rels) {
        super(data);
    }
}