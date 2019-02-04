/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.csv;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dspace.app.rest.builder.CollectionBuilder;
import org.dspace.app.rest.builder.CommunityBuilder;
import org.dspace.app.rest.builder.ItemBuilder;
import org.dspace.app.rest.matcher.RelationshipMatcher;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.EntityType;
import org.dspace.content.Item;
import org.dspace.content.Relationship;
import org.dspace.content.RelationshipType;
import org.dspace.content.service.EntityTypeService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.RelationshipService;
import org.dspace.content.service.RelationshipTypeService;
import org.dspace.services.ConfigurationService;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Ignore
public class CsvImportIT extends AbstractControllerIntegrationTest {

    @Autowired
    private RelationshipTypeService relationshipTypeService;

    @Autowired
    private EntityTypeService entityTypeService;

    @Autowired
    private RelationshipService relationshipService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ConfigurationService configurationService;

    @Before
    public void setup() throws Exception {

        //Set up the database for the next test
        String pathToFile = configurationService.getProperty("dspace.dir") +
            File.separator + "config" + File.separator + "entities" + File.separator + "relationship-types.xml";
        runDSpaceScript("initialize-entities", "-f", pathToFile);
    }

    @After
    public void destroy() throws Exception {
        //Clean up the database for the next test
        context.turnOffAuthorisationSystem();
        List<RelationshipType> relationshipTypeList = relationshipTypeService.findAll(context);
        List<EntityType> entityTypeList = entityTypeService.findAll(context);
        List<Relationship> relationships = relationshipService.findAll(context);
        Iterator<Item> itemIterator = itemService.findAll(context);

        Iterator<Relationship> relationshipIterator = relationships.iterator();
        while (relationshipIterator.hasNext()) {
            Relationship relationship = relationshipIterator.next();
            relationshipIterator.remove();
            relationshipService.delete(context, relationship);
        }

        Iterator<RelationshipType> relationshipTypeIterator = relationshipTypeList.iterator();
        while (relationshipTypeIterator.hasNext()) {
            RelationshipType relationshipType = relationshipTypeIterator.next();
            relationshipTypeIterator.remove();
            relationshipTypeService.delete(context, relationshipType);
        }

        Iterator<EntityType> entityTypeIterator = entityTypeList.iterator();
        while (entityTypeIterator.hasNext()) {
            EntityType entityType = entityTypeIterator.next();
            entityTypeIterator.remove();
            entityTypeService.delete(context, entityType);
        }

        while (itemIterator.hasNext()) {
            Item item = itemIterator.next();
            itemIterator.remove();
            itemService.delete(context, item);
        }

        super.destroy();
    }

    @Test
    public void createRelationshipsWithCsvImportTest() throws Exception {
        context.turnOffAuthorisationSystem();

        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, child1).withName("Collection 2").build();
        Collection col3 = CollectionBuilder.createCollection(context, child1).withName("OrgUnits").build();

        Item article = ItemBuilder.createItem(context, col1)
                                  .withTitle("Article")
                                  .withIssueDate("2017-10-17")
                                  .withRelationshipType("Publication")
                                  .build();

        Item itemB = validateSpecificItemRelationCreationCsvImport(col1, article, "TestItemB", "Person",
                                                                   "isPublicationOfAuthor",
                                                                   "Relationship list size is 1", 1, 1, 1);
        Item itemC = validateSpecificItemRelationCreationCsvImport(col1, article, "TestItemC", "Person",
                                                                   "isPublicationOfAuthor",
                                                                   "Relationship list size is 1", 1, 2, 1);
        Item itemD = validateSpecificItemRelationCreationCsvImport(col1, article, "TestItemD", "Project",
                                                                   "isPublicationOfProject",
                                                                   "Relationship list size is 1", 1, 1, 1);
        Item itemE = validateSpecificItemRelationCreationCsvImportMultiple(col1, "TestItemE", "Publication",
                                                                           "isAuthorOfPublication",
                                                                           "Relationship list size is 2", 2, 1, 2,
                                                                           itemC, itemB);

        List<Relationship> relationships = relationshipService.findByItem(context, itemE);
        getClient().perform(get("/api/core/relationships/" + relationships.get(0).getID()))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.leftPlace", is(1)))
                   .andExpect(jsonPath("$.rightId", is(itemC.getID().toString())))
                   .andExpect(jsonPath("$.rightPlace", is(2)))
                   .andExpect(jsonPath("$", Matchers.is(RelationshipMatcher.matchRelationship(relationships.get(0)))));
        getClient().perform(get("/api/core/relationships/" + relationships.get(1).getID().toString()))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.leftPlace", is(2)))
                   .andExpect(jsonPath("$.rightId", is(itemB.getID().toString())))
                   .andExpect(jsonPath("$.rightPlace", is(2)))
                   .andExpect(jsonPath("$", Matchers.is(RelationshipMatcher.matchRelationship(relationships.get(1)))));

        Item itemF = validateSpecificItemRelationCreationCsvImport(col1, itemE, "TestItemF", "Person",
                                                                   "isPublicationOfAuthor",
                                                                   "Relationship list size is 1", 1, 3, 1);

        UpdateItemEToDeleteRelationshipToC(itemE, itemB, itemF, col1, "TestItemE");

        getClient().perform(get("/api/core/items/" + itemE.getID())).andExpect(status().isOk());

        assertItemERelationships(itemB, itemE, itemF);

        updateArticleItemToAddAnotherRelationship(col1, article, itemB, itemC, itemF);

        getClient().perform(get("/api/core/items/" + article.getID())).andExpect(status().isOk());

        assertArticleRelationships(article, itemB, itemC, itemF);

    }

    private void assertItemERelationships(Item itemB, Item itemE, Item itemF) throws SQLException {
        List<Relationship> relationshipsForItemE = relationshipService.findByItem(context, itemE);
        assertThat(relationshipsForItemE.size(), is(2));
        assertThat(relationshipsForItemE.get(0).getRightItem(), is(itemF));
        assertThat(relationshipsForItemE.get(1).getRightItem(), is(itemB));
    }

    private void assertArticleRelationships(Item article, Item itemB, Item itemC, Item itemF) throws SQLException {
        List<Relationship> relationshipsForArticle = relationshipService
            .findByItemAndRelationshipType(context, article, relationshipTypeService
                .findByLeftOrRightLabel(context,"isPublicationOfAuthor").get(0));
        assertThat(relationshipsForArticle.size(), is(3));
        List<Item> expectedRelationshipsItemsForArticle = new ArrayList<>();
        expectedRelationshipsItemsForArticle.add(itemC);
        expectedRelationshipsItemsForArticle.add(itemF);
        expectedRelationshipsItemsForArticle.add(itemB);

        List<Item> actualRelationshipsItemsForArticle = new ArrayList<>();
        for (Relationship relationship : relationshipsForArticle) {
            if (relationship.getLeftItem().getID() == article.getID()) {
                actualRelationshipsItemsForArticle.add(relationship.getLeftItem());
            } else {
                actualRelationshipsItemsForArticle.add(relationship.getRightItem());
            }
        }
        assertThat(true, Matchers.is(actualRelationshipsItemsForArticle
                                            .containsAll(expectedRelationshipsItemsForArticle)));
    }

    private void updateArticleItemToAddAnotherRelationship(Collection col1, Item article, Item itemB, Item itemC,
                                                           Item itemF) throws Exception {
        String csvLineString = article.getID().toString() + "," + col1
            .getHandle() + "," + "Article" + "," + "Publication" + "," +
            itemB.getID().toString() + "||" + itemC.getID().toString() + "||" + itemF
            .getID().toString();
        String[] csv = {"id,collection,dc.title,relationship.type,relation." + "isAuthorOfPublication", csvLineString};
        performImportScript(csv);
    }

    private void UpdateItemEToDeleteRelationshipToC(Item itemE, Item itemB, Item itemF, Collection owningCollection,
                                                    String title) throws Exception {
        String csvLineString = itemE.getID().toString() + "," + owningCollection
            .getHandle() + "," + title + "," + "Person" + "," + itemB.getID().toString() + "||" + itemF.getID()
                                                                                                       .toString();
        String[] csv = {"id,collection,dc.title,relationship.type,relation." + "isAuthorOfPublication", csvLineString};
        performImportScript(csv);

    }

    private Item validateSpecificItemRelationCreationCsvImport(Collection col1, Item relatedItem, String itemTitle,
                                                               String relationshipType,
                                                               String relationshipTypeLabel,
                                                               String reasonAssertCheck, Integer sizeToCheck,
                                                               Integer leftPlaceToCheck,
                                                               Integer rightPlaceToCheck) throws Exception {
        String csvLineString = "+," + col1.getHandle() + "," + itemTitle + "," + relationshipType + "," + relatedItem
            .getID().toString();
        String[] csv = {"id,collection,dc.title,relationship.type,relation." + relationshipTypeLabel, csvLineString};
        performImportScript(csv);
        Iterator<Item> itemIteratorItem = itemService.findByMetadataField(context, "dc", "title", null, itemTitle);
        Item item = itemIteratorItem.next();

        List<Relationship> relationships = relationshipService.findByItem(context, item);
        assertThat(reasonAssertCheck, relationships.size(), equalTo(sizeToCheck));
        getClient().perform(get("/api/core/items/" + item.getID())).andExpect(status().isOk());
        getClient().perform(get("/api/core/relationships/" + relationships.get(0).getID()))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.leftPlace", is(leftPlaceToCheck)))
                   .andExpect(jsonPath("$.rightPlace", is(rightPlaceToCheck)))
                   .andExpect(jsonPath("$", Matchers.is(RelationshipMatcher.matchRelationship(relationships.get(0)))));

        return item;
    }

    private Item validateSpecificItemRelationCreationCsvImportMultiple(Collection col1, String itemTitle,
                                                                       String relationshipType,
                                                                       String relationshipTypeLabel,
                                                                       String reasonAssertCheck, Integer sizeToCheck,
                                                                       Integer leftPlaceToCheck,
                                                                       Integer rightPlaceToCheck, Item... relatedItem)
        throws Exception {
        String idStringRelatedItems = "";
        for (Item item : relatedItem) {
            idStringRelatedItems += item.getID().toString();
            idStringRelatedItems += "||";
        }
        idStringRelatedItems = idStringRelatedItems.substring(0, idStringRelatedItems.length() - 2);
        String csvLineString = "+," + col1
            .getHandle() + "," + itemTitle + "," + relationshipType + "," + idStringRelatedItems;
        String[] csv = {"id,collection,dc.title,relationship.type,relation." + relationshipTypeLabel, csvLineString};
        performImportScript(csv);
        Iterator<Item> itemIteratorItem = itemService.findByMetadataField(context, "dc", "title", null, itemTitle);
        Item item = itemIteratorItem.next();


        return item;
    }

    private void performImportScript(String[] csv) throws Exception {
        String filename = "test.csv";
        BufferedWriter out = new BufferedWriter(
            new OutputStreamWriter(
                new FileOutputStream(filename), "UTF-8"));
        for (String csvLine : csv) {
            out.write(csvLine + "\n");
        }
        out.flush();
        out.close();
        out = null;

        runDSpaceScript("metadata-import", "-f", "test.csv", "-e", "admin@email.com", "-s");
    }
}
