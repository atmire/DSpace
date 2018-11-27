package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.link.HalLinkService;
import org.dspace.app.rest.link.MappingItemResourceWrapperHalLinkFactory;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.model.MappingItemRestWrapper;
import org.dspace.app.rest.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class MappingItemResourceWrapper extends HALResource<MappingItemRestWrapper> {
    private static final Logger log = LoggerFactory.getLogger(MappingItemResourceWrapper.class);

    public MappingItemResourceWrapper(MappingItemRestWrapper content, Utils utils, Pageable pageable, Integer totalElements, String... rels) {
        super(content);

        List<ItemResource> itemResources = new LinkedList<>();
        for (ItemRest itemRest : content.getMappingItemRestList()) {
            itemResources.add(new ItemResource(itemRest, utils));
        }
        embedResource("mappingItems", itemResources);
        PageImpl<ItemResource> page = new PageImpl<>(itemResources, pageable, totalElements);

        this.setPageHeader(new EmbeddedPageHeader("testing", page, true));

    }
}
