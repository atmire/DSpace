package org.dspace.app.rest.link.externalsources;

import java.util.LinkedList;

import org.dspace.app.rest.ExternalSourcesRestController;
import org.dspace.app.rest.link.HalLinkFactory;
import org.dspace.app.rest.model.hateoas.ExternalSourceEntryResource;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

@Component
public class ExternalSourceEntryHalLinkFactory
    extends HalLinkFactory<ExternalSourceEntryResource, ExternalSourcesRestController> {
    protected void addLinks(ExternalSourceEntryResource halResource, Pageable pageable, LinkedList<Link> list)
        throws Exception {

        list.add(buildLink(Link.REL_SELF,
                           getMethodOn().getExternalSourceEntryValue(halResource.getContent().getExternalSource(),
                                                                     halResource.getContent().getId())));

    }

    protected Class<ExternalSourcesRestController> getControllerClass() {
        return ExternalSourcesRestController.class;
    }

    protected Class<ExternalSourceEntryResource> getResourceClass() {
        return ExternalSourceEntryResource.class;
    }
}
