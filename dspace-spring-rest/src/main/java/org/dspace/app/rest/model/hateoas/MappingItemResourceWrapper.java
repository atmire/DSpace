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

    public MappingItemResourceWrapper(MappingItemRestWrapper content, String... rels) {
        super(content);
    }

    public EmbeddedPage getEmbeddedPage(HalLinkService halLinkService, Utils utils, Pageable pageable, int totalElements)
            throws SQLException{
        List<ItemResource> list = new LinkedList<>();
        for (ItemRest itemRest : getContent().getMappingItemRestList()) {
            ItemResource itemResource = new ItemResource(itemRest, utils);
            halLinkService.addLinks(itemResource);
            list.add(itemResource);
        }

        Page page = new PageImpl<>(list, pageable, totalElements);

        MappingItemResourceWrapperHalLinkFactory linkFactory = new MappingItemResourceWrapperHalLinkFactory();
        return new EmbeddedPage(linkFactory.getSelfLink(getContent(), pageable), page, list, "mappingItems");
    }

}
