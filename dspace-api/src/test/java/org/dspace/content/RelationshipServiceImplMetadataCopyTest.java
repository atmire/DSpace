/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.dspace.AbstractIntegrationTestWithDatabase;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.EntityTypeBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.builder.RelationshipBuilder;
import org.dspace.builder.RelationshipTypeBuilder;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.RelationshipService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.junit.Before;
import org.junit.Test;

public class RelationshipServiceImplMetadataCopyTest extends AbstractIntegrationTestWithDatabase {

    private ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();
    private RelationshipService relationshipService = ContentServiceFactory.getInstance().getRelationshipService();
    private ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    private Item leftItem;
    private Item rightItem;

    private Relationship leftRightRelationship;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        context.turnOffAuthorisationSystem();

        Community community = CommunityBuilder.createCommunity(context).build();

        Collection journalVolumeCollection = CollectionBuilder.createCollection(context, community)
                                                              .withEntityType("JournalVolume")
                                                              .build();
        Collection journalIssueCollection = CollectionBuilder.createCollection(context, community)
                                                             .withEntityType("JournalIssue")
                                                             .build();

        EntityType journalVolumeType = EntityTypeBuilder.createEntityTypeBuilder(context, "JournalVolume").build();
        EntityType journalIssueType = EntityTypeBuilder.createEntityTypeBuilder(context, "JournalIssue").build();

        RelationshipType issueJournalVolumeType =
            RelationshipTypeBuilder
                .createRelationshipTypeBuilder(context, journalIssueType, journalVolumeType,
                                               "isJournalVolumeOfIssue", "isIssueOfJournalVolume",
                                               null, null, null, null).build();

        leftItem = ItemBuilder.createItem(context, journalIssueCollection)
                              .withPublicationIssueNumber("2").build();
        rightItem = ItemBuilder.createItem(context, journalVolumeCollection)
                               .withPublicationVolumeNumber("30").build();

        leftRightRelationship =
            RelationshipBuilder.createRelationshipBuilder(context, leftItem, rightItem, issueJournalVolumeType).build();

        context.restoreAuthSystemState();
    }

    @Test
    public void testRelationMetadataNotCopiedWhenConfigDisabled() throws Exception {
        context.turnOffAuthorisationSystem();

        // Set the configuration to disable relation metadata copying
        configurationService.setProperty("relation.metadata.copy.on.delete", "false");

        // Assert the relationship exists before removal
        List<Relationship> relationships = relationshipService.findByItem(context, leftItem);
        assertEquals(1, relationships.size());

        // Verify the left item's relation.isJournalVolumeOfIssue metadata before removal
        List<MetadataValue> volumeList =
            itemService.getMetadata(leftItem, "relation", "isJournalVolumeOfIssue", null, Item.ANY);
        assertThat(volumeList.get(0).getValue(), equalTo(rightItem.getID().toString()));

        // Verify the right item's relation.isIssueOfJournalVolume metadata before removal
        List<MetadataValue> issueList =
            itemService.getMetadata(rightItem, "relation", "isIssueOfJournalVolume", null, Item.ANY);
        assertThat(issueList.get(0).getValue(), equalTo(leftItem.getID().toString()));

        // Remove the relationship
        relationshipService.delete(context, leftRightRelationship, true, true);

        // Assert the relationship is removed
        relationships = relationshipService.findByItem(context, leftItem);
        assertEquals(0, relationships.size());

        // Verify the left item's relation.isJournalVolumeOfIssue metadata should NOT be copied
        volumeList = itemService.getMetadata(leftItem, "relation", "isJournalVolumeOfIssue", null, Item.ANY);
        assertThat(volumeList.size(), equalTo(0));

        // Verify the right item's relation.isIssueOfJournalVolume metadata should NOT be copied
        issueList = itemService.getMetadata(rightItem, "relation", "isIssueOfJournalVolume", null, Item.ANY);
        assertThat(issueList.size(), equalTo(0));

        context.restoreAuthSystemState();
    }

    @Test
    public void testRelationMetadataCopiedWhenConfigEnabled() throws Exception {
        context.turnOffAuthorisationSystem();

        // Set the configuration to disable relation metadata copying
        configurationService.setProperty("relation.metadata.copy.on.delete", "true");

        // Assert the relationship exists before removal
        List<Relationship> relationships = relationshipService.findByItem(context, leftItem);
        assertEquals(1, relationships.size());

        // Verify the left item's relation.isJournalVolumeOfIssue metadata before removal
        List<MetadataValue> volumeList =
            itemService.getMetadata(leftItem, "relation", "isJournalVolumeOfIssue", null, Item.ANY);
        assertThat(volumeList.get(0).getValue(), equalTo(rightItem.getID().toString()));

        // Verify the right item's relation.isIssueOfJournalVolume metadata before removal
        List<MetadataValue> issueList =
            itemService.getMetadata(rightItem, "relation", "isIssueOfJournalVolume", null, Item.ANY);
        assertThat(issueList.get(0).getValue(), equalTo(leftItem.getID().toString()));

        // Remove the relationship
        relationshipService.delete(context, leftRightRelationship, true, true);

        // Assert the relationship is removed
        relationships = relationshipService.findByItem(context, leftItem);
        assertEquals(0, relationships.size());

        // Verify the left item's relation.isJournalVolumeOfIssue metadata copied after removal
        volumeList = itemService.getMetadata(leftItem, "relation", "isJournalVolumeOfIssue", null, Item.ANY);
        assertThat(volumeList.get(0).getValue(), equalTo(rightItem.getID().toString()));

        // Verify the right item's relation.isIssueOfJournalVolume metadata copied after removal
        issueList = itemService.getMetadata(rightItem, "relation", "isIssueOfJournalVolume", null, Item.ANY);
        assertThat(issueList.get(0).getValue(), equalTo(leftItem.getID().toString()));

        context.restoreAuthSystemState();
    }
}
