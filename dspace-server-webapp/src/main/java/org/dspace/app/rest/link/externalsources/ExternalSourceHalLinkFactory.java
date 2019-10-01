package org.dspace.app.rest.link.externalsources;

import java.util.LinkedList;

import org.dspace.app.rest.ExternalSourcesRestController;
import org.dspace.app.rest.link.HalLinkFactory;
import org.dspace.app.rest.model.hateoas.ExternalSourceResource;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

@Component
public class ExternalSourceHalLinkFactory extends
                                          HalLinkFactory<ExternalSourceResource, ExternalSourcesRestController> {

    protected void addLinks(ExternalSourceResource halResource, Pageable pageable, LinkedList<Link> list)
        throws Exception {

        list.add(buildLink(Link.REL_SELF, getMethodOn().getExternalSource(halResource.getContent().getName())));
        list.add(buildLink("entries", getMethodOn()
            .getExternalSourceEntries(halResource.getContent().getName(), "", null, null, null)));

    }

    protected Class<ExternalSourcesRestController> getControllerClass() {
        return ExternalSourcesRestController.class;
    }

    protected Class<ExternalSourceResource> getResourceClass() {
        return ExternalSourceResource.class;
    }
}
