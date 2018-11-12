package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.model.MappingItemRestWrapper;
import org.dspace.app.rest.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import java.util.LinkedList;
import java.util.List;

public class MappingItemResourceWrapper extends HALResource<MappingItemRestWrapper> {
    private static final Logger log = LoggerFactory.getLogger(MappingItemResourceWrapper.class);

    public MappingItemResourceWrapper(MappingItemRestWrapper content, Utils utils, String... rels) {
        super(content);
        addEmbeds(content, utils);
    }

    private void addEmbeds(final MappingItemRestWrapper data, final Utils utils) {
        List<ItemResource> list = new LinkedList<>();
        for (ItemRest itemRest : data.getMappingItemRestList()) {

            list.add(new ItemResource(itemRest, utils));
        }

        embedResource("mappingItems", list);
    }

}
