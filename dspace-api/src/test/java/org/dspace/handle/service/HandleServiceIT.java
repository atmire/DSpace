/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.handle.service;

import static org.dspace.builder.CollectionBuilder.createCollection;
import static org.dspace.builder.CommunityBuilder.createCommunity;
import static org.dspace.builder.ItemBuilder.createItem;
import static org.junit.Assert.assertEquals;

import org.dspace.AbstractIntegrationTestWithDatabase;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.junit.Before;
import org.junit.Test;

public class HandleServiceIT extends AbstractIntegrationTestWithDatabase {

    private HandleService handleService
            = HandleServiceFactory.getInstance().getHandleService();
    private ConfigurationService configurationService
            = DSpaceServicesFactory.getInstance().getConfigurationService();

    private Collection collection;

    @Before
    public void setup() {

        context.turnOffAuthorisationSystem();
        Community community = createCommunity(context).build();
        collection = createCollection(context, community).build();
    }

    @Test
    public void testEntity() throws Exception {

        Item item = createItem(context, collection)
                .withRelationshipType("test-type")
                .build();

        assertEquals(
                configurationService.getProperty("dspace.ui.url")
                        + "/entities/test-type/" + item.getID(),
                handleService.resolveToURL(context, item.getHandle())
        );
    }

    @Test
    public void testNonEntity() throws Exception {

        Item item = createItem(context, collection)
                .build();

        assertEquals(
                configurationService.getProperty("dspace.ui.url")
                        + "/items/" + item.getID(),
                handleService.resolveToURL(context, item.getHandle())
        );
    }
}
