package org.dspace.app.rest.model.hateoas;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.dspace.app.rest.link.MappingItemResourceWrapperHalLinkFactory;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.model.MappingItemRestWrapper;
import org.dspace.app.rest.model.RestAddressableModel;
import org.dspace.app.rest.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class MappingItemResourceWrapper extends HALResource<MappingItemRestWrapper> {
    private static final Logger log = LoggerFactory.getLogger(MappingItemResourceWrapper.class);

    public MappingItemResourceWrapper(MappingItemRestWrapper content, Utils utils, Pageable pageable, String... rels) {
        super(content);
        addEmbeds(content, utils, pageable);
    }

    private void addEmbeds(MappingItemRestWrapper content, Utils utils, Pageable pageable) {
        List<ItemResource> list = new LinkedList<>();
        for (ItemRest itemRest : content.getMappingItemRestList()) {
            list.add(new ItemResource(itemRest, utils));
        }

        PageImpl<RestAddressableModel> page = new PageImpl(list);
        MappingItemResourceWrapperHalLinkFactory mappingItemResourceWrapperHalLinkFactory =
            new MappingItemResourceWrapperHalLinkFactory();

        EmbeddedPage embeddedPage = null;
        try {
            embeddedPage = new EmbeddedPage(mappingItemResourceWrapperHalLinkFactory.getSelfLink(content, pageable), page, list
                , "mappingItems");
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        embedResource("mappingItems", embeddedPage);
    }
}
