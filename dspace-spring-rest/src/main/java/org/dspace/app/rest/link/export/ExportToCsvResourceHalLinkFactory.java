package org.dspace.app.rest.link.export;

import java.util.LinkedList;

import org.dspace.app.rest.model.ExportToCsvRest;
import org.dspace.app.rest.model.hateoas.ExportToCsvResource;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ExportToCsvResourceHalLinkFactory extends ExportToCsvRestHalLinkFactory<ExportToCsvResource> {


    protected void addLinks(ExportToCsvResource halResource, Pageable pageable, LinkedList<Link> list)
        throws Exception {

        ExportToCsvRest exportToCsvRest = halResource.getContent();

        if (exportToCsvRest != null) {

            UriComponentsBuilder uriBuilderSelfLink = uriBuilder(getMethodOn()
                                                                     .viewSpecific(exportToCsvRest.getDsoUuid(),
                                                                                   exportToCsvRest.getDate().toString()
                                                                                                  .replace(" ", "T"),
                                                                                   null,
                                                                                   null, exportToCsvRest.getType(),
                                                                                   exportToCsvRest.getCategory()));
            list.add(buildLink(Link.REL_SELF, uriBuilderSelfLink.build().toString()));

        }
    }

    protected Class<ExportToCsvResource> getResourceClass() {
        return ExportToCsvResource.class;
    }
}