package org.dspace.app.rest;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dspace.app.rest.converter.ItemConverter;
import org.dspace.app.rest.link.HalLinkService;
import org.dspace.app.rest.model.ItemRest;
import org.dspace.app.rest.model.MappingItemRestWrapper;
import org.dspace.app.rest.model.hateoas.MappingItemResourceWrapper;
import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.app.rest.utils.Utils;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core/collections/" +
    "{uuid:[0-9a-fxA-FX]{8}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{12}}/mappingItems")
public class MappingItemRestController {

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemConverter itemConverter;

    @Autowired
    Utils utils;

    @Autowired
    private HalLinkService halLinkService;

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.HEAD})
    public MappingItemResourceWrapper retrieve(@PathVariable UUID uuid, HttpServletResponse response,
                                               HttpServletRequest request) throws SQLException {
        Context context = ContextUtil.obtainContext(request);
        Collection collection = collectionService.find(context, uuid);
        Iterator<Item> itemIterator = itemService.findByCollection(context, collection);
        List<ItemRest> mappedItemRestList = new LinkedList<>();
        while (itemIterator.hasNext()) {
            Item item = itemIterator.next();
            if (item.getOwningCollection().getID() != uuid) {
                mappedItemRestList.add(itemConverter.fromModel(item));
            }
        }

        MappingItemRestWrapper mappingItemRestWrapper = new MappingItemRestWrapper();
        mappingItemRestWrapper.setMappingItemRestList(mappedItemRestList);
        MappingItemResourceWrapper mappingItemResourceWrapper = new MappingItemResourceWrapper(mappingItemRestWrapper, utils);

        halLinkService.addLinks(mappingItemResourceWrapper);

        return mappingItemResourceWrapper;



    }
}
