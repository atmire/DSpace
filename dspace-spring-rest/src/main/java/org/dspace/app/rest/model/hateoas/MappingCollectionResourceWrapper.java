package org.dspace.app.rest.model.hateoas;

import java.util.LinkedList;
import java.util.List;

import org.dspace.app.rest.model.CollectionRest;
import org.dspace.app.rest.model.MappingCollectionRestWrapper;
import org.dspace.app.rest.utils.Utils;

public class MappingCollectionResourceWrapper extends HALResource<MappingCollectionRestWrapper> {

    public MappingCollectionResourceWrapper(MappingCollectionRestWrapper content, Utils utils, String... rels) {
        super(content);
        addEmbeds(content, utils);
    }

    private void addEmbeds(final MappingCollectionRestWrapper data, final Utils utils) {

        List<CollectionResource> list = new LinkedList<>();
        for (CollectionRest collectionRest : data.getMappingCollectionRestList()) {

            list.add(new CollectionResource(collectionRest, utils));
        }


        embedResource("mappingCollections", list);

    }
}