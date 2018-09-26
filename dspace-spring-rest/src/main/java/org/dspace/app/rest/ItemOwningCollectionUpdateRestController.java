/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dspace.app.rest.utils.ContextUtil;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/core/items/" +
        "{itemUuid:[0-9a-fxA-FX]{8}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{4}-[0-9a-fxA-FX]{12" +
        "}}/owningCollection/move")
public class ItemOwningCollectionUpdateRestController {

    @Autowired
    ItemService itemService;

    @Autowired
    CollectionService collectionService;

    @RequestMapping(method = RequestMethod.POST, value = "/{targetUuid}")
    public void move(@PathVariable UUID itemUuid, HttpServletResponse response,
                     HttpServletRequest request, @PathVariable UUID targetUuid)
            throws SQLException, IOException, AuthorizeException {
        Context context = ContextUtil.obtainContext(request);

        setOwningCollection(context, itemUuid, targetUuid);

        context.commit();
        context.complete();
    }

    @PreAuthorize("hasPermission(#id, 'ITEM', 'WRITE')")
    public void setOwningCollection(Context context, UUID id, UUID collection)
            throws SQLException, IOException, AuthorizeException {
        Item item = itemService.find(context, id);

        Collection currentCollection = item.getOwningCollection();
        Collection targetCollection = collectionService.find(context, collection);

        itemService.move(context, item, currentCollection, targetCollection);
    }



}
