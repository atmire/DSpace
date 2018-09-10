package org.dspace.app.rest.link.export;

import java.util.LinkedList;

import org.dspace.app.rest.model.ExportToZipRestWrapper;
import org.dspace.app.rest.model.hateoas.ExportToZipResourceWrapper;
import org.dspace.content.DSpaceObject;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ExportToZipResourceWrapperHalLinkFactory
    extends ExportToZipRestHalLinkFactory<ExportToZipResourceWrapper> {


    protected void addLinks(ExportToZipResourceWrapper halResource, Pageable pageable, LinkedList<Link> list)
        throws Exception {

        ExportToZipRestWrapper exportToZipRestWrapper = halResource.getContent();

        if (exportToZipRestWrapper != null) {

            DSpaceObject itemToBeExported = exportToZipRestWrapper.getItemToBeExported();
            if(itemToBeExported != null) {
                UriComponentsBuilder uriBuilderCreateLink = uriBuilder(getMethodOn()
                                                                         .create(
                                                                             itemToBeExported
                                                                                                   .getID(), null, null,
                                                                             exportToZipRestWrapper.getType(),
                                                                             exportToZipRestWrapper.getCategory()));

                list.add(buildLink("create", uriBuilderCreateLink.build().toString()));
            }
        }
    }

    protected Class<ExportToZipResourceWrapper> getResourceClass() {
        return ExportToZipResourceWrapper.class;
    }
}
