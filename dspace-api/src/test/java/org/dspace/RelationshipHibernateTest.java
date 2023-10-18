/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace;

import static org.junit.Assert.assertTrue;

import java.io.File;

import java.util.Iterator;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.EntityTypeBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.builder.RelationshipBuilder;
import org.dspace.builder.RelationshipTypeBuilder;
import org.dspace.content.Collection;
import org.dspace.content.EntityType;
import org.dspace.content.Item;
import org.dspace.content.Relationship;
import org.dspace.content.RelationshipType;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.junit.Before;
import org.junit.Test;

public class RelationshipHibernateTest extends AbstractIntegrationTestWithDatabase {
    private ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    private MetadataFieldService metadataFieldService = ContentServiceFactory.getInstance().getMetadataFieldService();
    Collection collection;
    Item publication;
    Item author;
    String dirPath = DSpaceServicesFactory.getInstance().getConfigurationService()
        .getProperty("dspace.dir") + File.separator + "reports";
    private EntityType publicationType;
    private EntityType personType;
    private RelationshipType isAuthorOfPublication;
    private Relationship rel;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        context.turnOffAuthorisationSystem();

        parentCommunity = CommunityBuilder.createCommunity(context).build();
        collection = CollectionBuilder.createCollection(context, parentCommunity).build();

        // Create a publication<->author relationship to test virtual metadata.
        publicationType = EntityTypeBuilder.createEntityTypeBuilder(context, "Publication").build();
        personType = EntityTypeBuilder.createEntityTypeBuilder(context, "Person").build();
        isAuthorOfPublication = RelationshipTypeBuilder.createRelationshipTypeBuilder(
                context, publicationType, personType, "isAuthorOfPublication",
                "isPublicationOfAuthor", 0, 10, 0, 10
            )
            .withCopyToLeft(false)
            .withCopyToRight(false)
            .build();


        publication = ItemBuilder
            .createItem(context, collection)
            .withMetadata("dspace", "entity", "type", "Publication")
            .build();

        author = ItemBuilder
            .createItem(context, collection)
            .withPersonIdentifierLastName("Doe")
            .withPersonIdentifierFirstName("Jane")
            .withMetadata("dspace", "entity", "type", "Person")
            .build();

        rel = RelationshipBuilder
            .createRelationshipBuilder(context, publication, author, isAuthorOfPublication).build();


        context.restoreAuthSystemState();
    }

    @Test
    public void test() throws Exception {
        int count = 0;
        Iterator<Item> it = itemService.findAllUnfiltered(context);
        while (it.hasNext()) {
            Item item = it.next();
            count += itemService.getMetadata(item, "*", "*", "*", "*", true).size();
        }

        assertTrue(count > 0);
    }
}
