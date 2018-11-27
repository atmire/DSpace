package org.dspace.app.rest.link.export;

import java.util.LinkedList;

import org.dspace.app.rest.model.ExportToZipRest;
import org.dspace.app.rest.model.hateoas.ExportToZipResource;
import org.dspace.export.ExportStatus;
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
                                                                                   null, exportToZipRest.getType(), exportToZipRest.getCategory()));
            list.add(buildLink(Link.REL_SELF, uriBuilderSelfLink.build().toString()));

            if (exportToZipRest.getState().equals(ExportStatus.COMPLETED)) {
                UriComponentsBuilder uriBuilder = uriBuilder(getMethodOn()
                                                                 .downloadSpecific(exportToZipRest.getDsoUuid(),
                                                                                   exportToZipRest.getDate().toString()
                                                                                                  .replace(" ", "T"),
                                                                                   null,
                                                                                   null, exportToZipRest.getType(), exportToZipRest.getCategory()));
                list.add(buildLink("content", uriBuilder.build().toString()));
            }
        }
    }

    protected Class<ExportToZipResource> getResourceClass() {
        return ExportToZipResource.class;
    }
}
