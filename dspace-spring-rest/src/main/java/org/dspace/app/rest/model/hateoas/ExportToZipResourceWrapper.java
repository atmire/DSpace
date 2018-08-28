package org.dspace.app.rest.model.hateoas;

import java.util.LinkedList;
import java.util.List;

import org.dspace.app.rest.model.ExportToZipRest;
import org.dspace.app.rest.model.ExportToZipRestWrapper;
import org.dspace.app.rest.utils.Utils;

public class ExportToZipResourceWrapper extends DSpaceResource<ExportToZipRestWrapper> {

    public ExportToZipResourceWrapper(ExportToZipRestWrapper content, Utils utils, String... rels) {
        super(content, utils, rels);
        addEmbeds(content, utils);
    }

    private void addEmbeds(final ExportToZipRestWrapper data, final Utils utils) {

        List<ExportToZipResource> list = new LinkedList<>();
        for (ExportToZipRest exportToZipRest : data.getExportToZipRestList()) {

            list.add(new ExportToZipResource(exportToZipRest, utils));
        }


        embedResource("exportToZip", list);
    }
}
