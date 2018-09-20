package org.dspace.app.rest.model.hateoas;

import java.util.LinkedList;
import java.util.List;

import org.dspace.app.rest.model.ExportToCsvRest;
import org.dspace.app.rest.model.ExportToCsvRestWrapper;
import org.dspace.app.rest.utils.Utils;

public class ExportToCsvResourceWrapper extends HALResource<ExportToCsvRestWrapper> {

    public ExportToCsvResourceWrapper(ExportToCsvRestWrapper content, Utils utils, String... rels) {
        super(content);
        addEmbeds(content, utils);
    }

    private void addEmbeds(final ExportToCsvRestWrapper data, final Utils utils) {

        List<ExportToCsvResource> list = new LinkedList<>();
        for (ExportToCsvRest exportToCsvRest : data.getExportToCsvRestList()) {

            list.add(new ExportToCsvResource(exportToCsvRest, utils));
        }


        embedResource("exportToCsv", list);
    }
}