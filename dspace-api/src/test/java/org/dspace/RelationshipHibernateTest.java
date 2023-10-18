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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.EntityTypeBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.builder.RelationshipBuilder;
import org.dspace.builder.RelationshipTypeBuilder;
import org.dspace.content.Collection;
import org.dspace.content.DCDate;
import org.dspace.content.EntityType;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.Relationship;
import org.dspace.content.RelationshipType;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.core.Context;
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

        Context ctx = new Context(Context.Mode.READ_ONLY);
        ctx.turnOffAuthorisationSystem();

        List<UUID> itemIds = new ArrayList<>();
        Iterator<Item> it = itemService.findAllUnfiltered(ctx);
        it.forEachRemaining(item -> itemIds.add(item.getID()));

        List<String[]> results = new ArrayList<>();

        for (UUID id : itemIds) {
            Item item = itemService.find(ctx, id);
            List<MetadataValue> mdvs = itemService.getMetadata(item, "*", "*", "*", "*", true);
            for (MetadataValue mdv : mdvs) {
                results.add(itemCSV(item));
                results.add(metadataCSV(mdv, item));
            }
        }

        assertTrue(results.size() > 0);
    }

    private String[] itemCSV(Item item) {
        List<MetadataValue> entityTypeMV = itemService.getMetadata(item, "dspace", "entity", "type", Item.ANY, false);
        return new String[] {
            item.getID().toString(),
            item.getLegacyId() != null ? item.getLegacyId().toString() : null,
            item.getSubmitter() != null ? item.getSubmitter().getID().toString() : null,
            item.getSubmitter() != null && item.getSubmitter().getLegacyId() != null
                ? item.getSubmitter().getLegacyId().toString() : null,
            String.valueOf(item.isArchived()),
            String.valueOf(item.isWithdrawn()),
            item.getOwningCollection() != null ? item.getOwningCollection().getID().toString() : null,
            item.getOwningCollection() != null && item.getOwningCollection().getLegacyId() != null
                ? item.getOwningCollection().getLegacyId().toString() : null,
            new DCDate(item.getLastModified()).toString(),
            String.valueOf(item.isDiscoverable()),
            entityTypeMV.size() > 0 ? entityTypeMV.get(0).getValue() : null
        };
    }

    private String[] metadataCSV(MetadataValue mdv, Item item) {
        boolean isVirtual = mdv.getAuthority() != null && mdv.getAuthority().startsWith("virtual::");
        return new String[] {
            isVirtual ? "virtual metadata" : mdv.getID().toString(),
            item.getID().toString(),
            mdv.getMetadataField().getID().toString(),
            mdv.getValue(),
            mdv.getLanguage(),
            String.valueOf(mdv.getPlace()),
            mdv.getAuthority(),
            mdv.getMetadataField().getMetadataSchema().getName(),
            mdv.getMetadataField().getElement(),
            mdv.getMetadataField().getQualifier()
        };

    }
}
