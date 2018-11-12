package org.dspace.app.rest.link;

import java.sql.SQLException;
import java.util.LinkedList;

import org.dspace.app.rest.model.MappingItemRestWrapper;
import org.dspace.app.rest.model.hateoas.EmbeddedPageHeader;
import org.dspace.app.rest.model.hateoas.ItemResource;
import org.dspace.app.rest.model.hateoas.MappingItemResourceWrapper;
import org.springframework.data.domain.PageImpl;
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

            PageImpl<ItemResource> page = new PageImpl<>(halResource.getItemResources(), pageable, halResource.getTotalElements());

            halResource.setPageHeader(new EmbeddedPageHeader(getSelfLink(mappingItemRestWrapper, pageable), page, true));
        }

    }
    public String getSelfLink(MappingItemRestWrapper mappingItemRestWrapper, Pageable pageable) throws Exception {
        if (mappingItemRestWrapper != null) {
            UriComponentsBuilder uriBuilderSelfLink = uriBuilder(getMethodOn()
                                                                     .retrieve(
                                                                         mappingItemRestWrapper.getCollectionUuid(),
                                                                         null, null, pageable));
            return uriBuilderSelfLink.build().toString();
        }
        return null;

    }

    protected Class<MappingItemResourceWrapper> getResourceClass() {
        return MappingItemResourceWrapper.class;
    }
}
