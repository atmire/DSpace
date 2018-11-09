package org.dspace.app.rest.link.export;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.TimeZone;

import org.dspace.app.rest.model.ExportToCsvRest;
import org.dspace.app.rest.model.hateoas.ExportToCsvResource;
import org.dspace.export.ExportStatus;
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

            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));   // This line converts the given date into UTC time zone
            String dateString = sdf.format(exportToCsvRest.getDate());
            UriComponentsBuilder uriBuilderSelfLink = uriBuilder(getMethodOn()
                                                                     .viewSpecific(exportToCsvRest.getDsoUuid(),
                                                                                   dateString,
                                                                                   null,
                                                                                   null, exportToCsvRest.getType(),
                                                                                   exportToCsvRest.getCategory()));
            list.add(buildLink(Link.REL_SELF, uriBuilderSelfLink.build().toString()));

            if (exportToCsvRest.getState().equals(ExportStatus.COMPLETED)) {
                UriComponentsBuilder uriBuilder = uriBuilder(getMethodOn()
                                                                 .downloadSpecific(exportToCsvRest.getDsoUuid(),
                                                                                   dateString,
                                                                                   null,
                                                                                   null, exportToCsvRest.getType(), exportToCsvRest.getCategory()));
                list.add(buildLink("content", uriBuilder.build().toString()));
            }
        }
    }

    protected Class<ExportToCsvResource> getResourceClass() {
        return ExportToCsvResource.class;
    }
}