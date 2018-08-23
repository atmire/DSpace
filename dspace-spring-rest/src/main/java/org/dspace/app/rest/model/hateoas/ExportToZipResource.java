package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.ExportToZipRest;
import org.dspace.app.rest.utils.Utils;

public class ExportToZipResource extends HALResource<ExportToZipRest> {

    public ExportToZipResource(ExportToZipRest data, Utils utils, String... rels) {
        super(data);
    }
}
