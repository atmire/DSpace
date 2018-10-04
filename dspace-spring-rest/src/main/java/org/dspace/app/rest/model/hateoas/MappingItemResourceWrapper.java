package org.dspace.app.rest.model.hateoas;

import java.util.LinkedList;
import java.util.List;

import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.model.MappingItemRestWrapper;
import org.dspace.app.rest.utils.Utils;

public class MappingItemResourceWrapper extends HALResource<MappingItemRestWrapper> {
    public MappingItemResourceWrapper(MappingItemRestWrapper content, Utils utils, String... rels) {
        super(content);
        addEmbeds(content, utils);
    }

    private void addEmbeds(MappingItemRestWrapper content, Utils utils) {
        List<ItemResource> list = new LinkedList<>();
        for (ItemRest itemRest : content.getMappingItemRestList()) {
            list.add(new ItemResource(itemRest, utils));
        }

        embedResource("mappingItems", list);
    }
}
