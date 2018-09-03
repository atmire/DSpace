package org.dspace.app.rest;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dspace.app.rest.converter.CollectionConverter;
import org.dspace.app.rest.link.HalLinkService;
import org.dspace.app.rest.model.CollectionRest;
import org.dspace.app.rest.model.MappingCollectionRestWrapper;
import org.dspace.app.rest.model.hateoas.CollectionResource;
import org.dspace.app.rest.model.hateoas.MappingCollectionResourceWrapper;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.Utils;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core/items/" +
    "{uuid:[0-9a-fxA-FX]{8}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{12}}/mappingCollections")
public class MappingCollectionRestController {


    @Autowired
    private ItemService itemService;

    @Autowired
    private CollectionConverter collectionConverter;

    @Autowired
    Utils utils;

    @Autowired
    private HalLinkService halLinkService;

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
    public MappingCollectionResourceWrapper retrieve(@PathVariable UUID uuid, HttpServletResponse response,
                                             HttpServletRequest request) throws SQLException {
        Context context = ContextUtil.obtainContext(request);
        Item item = itemService.find(context, uuid);
        List<Collection> collections = item.getCollections();
        UUID owningCollectionUuid = item.getOwningCollection().getID();
        List<CollectionRest> mappingCollectionRest = new LinkedList<>();
        for (Collection collection : collections) {
            if (collection.getID() != owningCollectionUuid) {
                mappingCollectionRest.add(collectionConverter.fromModel(collection));
            }
        }

        MappingCollectionRestWrapper mappingCollectionRestWrapper = new MappingCollectionRestWrapper();
        mappingCollectionRestWrapper.setMappingCollectionRestList(mappingCollectionRest);
        MappingCollectionResourceWrapper mappingCollectionResourceWrapper = new MappingCollectionResourceWrapper(
            mappingCollectionRestWrapper, utils);


        halLinkService.addLinks(mappingCollectionResourceWrapper);

        return mappingCollectionResourceWrapper;


    }
}
