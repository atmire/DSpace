package org.dspace.app.rest.link;

import java.util.LinkedList;
import java.util.UUID;

import org.dspace.app.rest.model.MappingItemRestWrapper;
import org.dspace.app.rest.model.hateoas.MappingItemResourceWrapper;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class MappingItemResourceWrapperHalLinkFactory
    extends MappingItemRestHalLinkFactory<MappingItemResourceWrapper> {
    protected void addLinks(MappingItemResourceWrapper halResource, Pageable pageable, LinkedList<Link> list)
        throws Exception {

        MappingItemRestWrapper mappingItemRestWrapper = halResource.getContent();
        if (mappingItemRestWrapper != null) {

            UriComponentsBuilder uriBuilderSelfLink = uriBuilder(getMethodOn()
                                                                     .retrieve(
                                                                         UUID.fromString(
                                                                             "c4779449-1772-4624-b216-674fa8f1518e"),
                                                                         null, null));
            list.add(buildLink(Link.REL_SELF, uriBuilderSelfLink.build().toString()));
        }

    }

    protected Class<MappingItemResourceWrapper> getResourceClass() {
        return MappingItemResourceWrapper.class;
    }
}
