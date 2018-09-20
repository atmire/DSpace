package org.dspace.app.rest.link.export;

import java.util.LinkedList;

import org.dspace.app.rest.model.ExportToCsvRestWrapper;
import org.dspace.app.rest.model.hateoas.ExportToCsvResourceWrapper;
import org.dspace.content.DSpaceObject;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ExportToCsvResourceWrapperHalLinkFactory
    extends ExportToCsvRestHalLinkFactory<ExportToCsvResourceWrapper> {


    protected void addLinks(ExportToCsvResourceWrapper halResource, Pageable pageable, LinkedList<Link> list)
        throws Exception {

        ExportToCsvRestWrapper exportToCsvRestWrapper = halResource.getContent();

        if (exportToCsvRestWrapper != null) {

            DSpaceObject itemToBeExported = exportToCsvRestWrapper.getItemToBeExported();
            if (itemToBeExported != null) {
                UriComponentsBuilder uriBuilderCreateLink = uriBuilder(getMethodOn()
                                                                           .create(
                                                                               itemToBeExported
                                                                                   .getID(), null, null,
                                                                               exportToCsvRestWrapper.getType(),
                                                                               exportToCsvRestWrapper.getCategory()));

                list.add(buildLink("create", uriBuilderCreateLink.build().toString()));

                UriComponentsBuilder uriBuilderSelfLink = uriBuilder(getMethodOn()
                                                                         .retrieve(itemToBeExported.getID(),
                                                                                   null, null,
                                                                                   exportToCsvRestWrapper.getType(),
                                                                                   exportToCsvRestWrapper
                                                                                       .getCategory()));
                list.add(buildLink(Link.REL_SELF, uriBuilderSelfLink.build().toString()));
            }
        }
    }

    protected Class<ExportToCsvResourceWrapper> getResourceClass() {
        return ExportToCsvResourceWrapper.class;
    }
}