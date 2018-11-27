/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model.hateoas;

import java.util.LinkedList;
import java.util.List;

import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.model.MappingItemRestWrapper;
import org.dspace.app.rest.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class MappingItemResourceWrapper extends HALResource<MappingItemRestWrapper> {
    private static final Logger log = LoggerFactory.getLogger(MappingItemResourceWrapper.class);

    public MappingItemResourceWrapper(MappingItemRestWrapper content, Utils utils, Pageable pageable,
                                      Integer totalElements, String... rels) {
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
