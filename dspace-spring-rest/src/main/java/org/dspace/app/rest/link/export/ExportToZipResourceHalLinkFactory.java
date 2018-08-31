package org.dspace.app.rest.link.export;

import java.util.LinkedList;

import org.apache.commons.lang.StringUtils;
import org.dspace.app.rest.model.ExportToZipRest;
import org.dspace.app.rest.model.hateoas.ExportToZipResource;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ExportToZipResourceHalLinkFactory extends ExportToZipRestHalLinkFactory<ExportToZipResource> {
    protected void addLinks(ExportToZipResource halResource, Pageable pageable, LinkedList<Link> list)
        throws Exception {

        ExportToZipRest exportToZipRest = halResource.getContent();

        if (exportToZipRest != null) {

            UriComponentsBuilder uriBuilderSelfLink = uriBuilder(getMethodOn()
                                                                     .viewSpecific(exportToZipRest.getDsoUuid(),
                                                                                   exportToZipRest.getDate().toString()
                                                                                                  .replace(" ", "T"),
                                                                                   null,
                                                                                   null));
            list.add(buildLink(Link.REL_SELF, uriBuilderSelfLink.build().toString()));

            if (StringUtils.equalsIgnoreCase(exportToZipRest.getState(), "completed")) {
                UriComponentsBuilder uriBuilder = uriBuilder(getMethodOn()
                                                                 .downloadSpecific(exportToZipRest.getDsoUuid(),
                                                                                   exportToZipRest.getDate().toString()
                                                                                                  .replace(" ", "T"),
                                                                                   null,
                                                                                   null));
                list.add(buildLink("content", uriBuilder.build().toString()));
            }
        }
    }

    protected Class<ExportToZipResource> getResourceClass() {
        return ExportToZipResource.class;
    }
}
