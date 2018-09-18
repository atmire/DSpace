/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.dspace.app.rest.builder.CollectionBuilder;
import org.dspace.app.rest.builder.CommunityBuilder;
import org.dspace.app.rest.builder.ItemBuilder;
import org.dspace.app.rest.matcher.CollectionMatcher;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.ItemService;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MappingCollectionRestRepositoryIT extends AbstractControllerIntegrationTest {

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private ItemService itemService;

    @Test
    public void itemHasNoExtraCollectionsTest() throws Exception {
        context.turnOffAuthorisationSystem();

        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and two collections.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, child1).withName("Collection 2").build();

        //2. Three public items that are readable by Anonymous with different subjects
        Item publicItem1 = ItemBuilder.createItem(context, col1)
                                      .withTitle("Public item 1")
                                      .withIssueDate("2017-10-17")
                                      .withAuthor("Smith, Donald").withAuthor("Doe, John")
                                      .withSubject("ExtraEntry")
                                      .build();

        Item publicItem2 = ItemBuilder.createItem(context, col2)
                                      .withTitle("Public item 2")
                                      .withIssueDate("2016-02-13")
                                      .withAuthor("Smith, Maria").withAuthor("Doe, Jane")
                                      .withSubject("TestingForMore").withSubject("ExtraEntry")
                                      .build();

        Item publicItem3 = ItemBuilder.createItem(context, col2)
                                      .withTitle("Public item 3")
                                      .withIssueDate("2016-02-13")
                                      .withAuthor("Smith, Maria").withAuthor("Doe, Jane")
                                      .withSubject("AnotherTest").withSubject("TestingForMore")
                                      .withSubject("ExtraEntry")
                                      .build();


//        collectionService.addItem(context, col2, publicItem1);
//        collectionService.update(context, col2);
//        itemService.update(context, publicItem1);

        getClient().perform(get("/api/core/items/" + publicItem1.getID() + "/mappingCollections"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$._embedded.mappingCollections", Matchers.not(Matchers.contains(
                       CollectionMatcher.matchCollectionEntry("Collection 1", col1.getID(), col1.getHandle()),
                       CollectionMatcher.matchCollectionEntry("Collection 3", col2.getID(), col2.getHandle()))
                   )))
                   .andExpect(jsonPath("$._links.self.href", Matchers.containsString("/api/core/items")))
        ;
    }

    @Test
    public void itemHasOneMappingCollectionTest() throws Exception {
        context.turnOffAuthorisationSystem();

        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and two collections.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, child1).withName("Collection 2").build();

        //2. Three public items that are readable by Anonymous with different subjects
        Item publicItem1 = ItemBuilder.createItem(context, col1)
                                      .withTitle("Public item 1")
                                      .withIssueDate("2017-10-17")
                                      .withAuthor("Smith, Donald").withAuthor("Doe, John")
                                      .withSubject("ExtraEntry")
                                      .build();

        Item publicItem2 = ItemBuilder.createItem(context, col2)
                                      .withTitle("Public item 2")
                                      .withIssueDate("2016-02-13")
                                      .withAuthor("Smith, Maria").withAuthor("Doe, Jane")
                                      .withSubject("TestingForMore").withSubject("ExtraEntry")
                                      .build();

        Item publicItem3 = ItemBuilder.createItem(context, col2)
                                      .withTitle("Public item 3")
                                      .withIssueDate("2016-02-13")
                                      .withAuthor("Smith, Maria").withAuthor("Doe, Jane")
                                      .withSubject("AnotherTest").withSubject("TestingForMore")
                                      .withSubject("ExtraEntry")
                                      .build();


        collectionService.addItem(context, col2, publicItem1);
        collectionService.update(context, col2);
        itemService.update(context, publicItem1);

        getClient().perform(get("/api/core/items/" + publicItem1.getID() + "/mappingCollections"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$._embedded.mappingCollections", Matchers.containsInAnyOrder(
                       CollectionMatcher.matchCollectionEntry("Collection 2", col2.getID(), col2.getHandle())
                   )))
                   .andExpect(jsonPath("$._links.self.href", Matchers.containsString("/api/core/items")))
        ;
    }

    @Test
    public void itemHasTwoMappingCollectionTest() throws Exception {
        context.turnOffAuthorisationSystem();

        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and two collections.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, child1).withName("Collection 2").build();
        Collection col3 = CollectionBuilder.createCollection(context, child1).withName("Collection 3").build();

        //2. Three public items that are readable by Anonymous with different subjects
        Item publicItem1 = ItemBuilder.createItem(context, col1)
                                      .withTitle("Public item 1")
                                      .withIssueDate("2017-10-17")
                                      .withAuthor("Smith, Donald").withAuthor("Doe, John")
                                      .withSubject("ExtraEntry")
                                      .build();

        Item publicItem2 = ItemBuilder.createItem(context, col2)
                                      .withTitle("Public item 2")
                                      .withIssueDate("2016-02-13")
                                      .withAuthor("Smith, Maria").withAuthor("Doe, Jane")
                                      .withSubject("TestingForMore").withSubject("ExtraEntry")
                                      .build();

        Item publicItem3 = ItemBuilder.createItem(context, col2)
                                      .withTitle("Public item 3")
                                      .withIssueDate("2016-02-13")
                                      .withAuthor("Smith, Maria").withAuthor("Doe, Jane")
                                      .withSubject("AnotherTest").withSubject("TestingForMore")
                                      .withSubject("ExtraEntry")
                                      .build();


        collectionService.addItem(context, col2, publicItem1);
        collectionService.addItem(context, col3, publicItem1);
        collectionService.update(context, col2);
        collectionService.update(context, col3);
        itemService.update(context, publicItem1);

        getClient().perform(get("/api/core/items/" + publicItem1.getID() + "/mappingCollections"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$._embedded.mappingCollections", Matchers.containsInAnyOrder(
                       CollectionMatcher.matchCollectionEntry("Collection 2", col2.getID(), col2.getHandle()),
                       CollectionMatcher.matchCollectionEntry("Collection 3", col3.getID(), col3.getHandle())
                   )))
                   .andExpect(jsonPath("$._links.self.href", Matchers.containsString("/api/core/items")))
        ;
    }

    @Test
    public void itemHasNoDuplicatesInMappingCollectionTest() throws Exception {
        context.turnOffAuthorisationSystem();

        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and two collections.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, child1).withName("Collection 2").build();
        Collection col3 = CollectionBuilder.createCollection(context, child1).withName("Collection 3").build();

        //2. Three public items that are readable by Anonymous with different subjects
        Item publicItem1 = ItemBuilder.createItem(context, col1)
                                      .withTitle("Public item 1")
                                      .withIssueDate("2017-10-17")
                                      .withAuthor("Smith, Donald").withAuthor("Doe, John")
                                      .withSubject("ExtraEntry")
                                      .build();

        Item publicItem2 = ItemBuilder.createItem(context, col2)
                                      .withTitle("Public item 2")
                                      .withIssueDate("2016-02-13")
                                      .withAuthor("Smith, Maria").withAuthor("Doe, Jane")
                                      .withSubject("TestingForMore").withSubject("ExtraEntry")
                                      .build();

        Item publicItem3 = ItemBuilder.createItem(context, col2)
                                      .withTitle("Public item 3")
                                      .withIssueDate("2016-02-13")
                                      .withAuthor("Smith, Maria").withAuthor("Doe, Jane")
                                      .withSubject("AnotherTest").withSubject("TestingForMore")
                                      .withSubject("ExtraEntry")
                                      .build();


        collectionService.addItem(context, col2, publicItem1);
        collectionService.addItem(context, col3, publicItem1);
        collectionService.addItem(context, col2, publicItem1);
        collectionService.update(context, col2);
        collectionService.update(context, col3);
        itemService.update(context, publicItem1);

        getClient().perform(get("/api/core/items/" + publicItem1.getID() + "/mappingCollections"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$._embedded.mappingCollections", Matchers.containsInAnyOrder(
                       CollectionMatcher.matchCollectionEntry("Collection 2", col2.getID(), col2.getHandle()),
                       CollectionMatcher.matchCollectionEntry("Collection 3", col3.getID(), col3.getHandle())
                   )))
                   .andExpect(jsonPath("$._links.self.href", Matchers.containsString("/api/core/items")))
        ;
    }

    @Test
    public void itemHasNoOriginalCollectionInMappingCollectionTest() throws Exception {
        context.turnOffAuthorisationSystem();

        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and two collections.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, child1).withName("Collection 2").build();
        Collection col3 = CollectionBuilder.createCollection(context, child1).withName("Collection 3").build();

        //2. Three public items that are readable by Anonymous with different subjects
        Item publicItem1 = ItemBuilder.createItem(context, col1)
                                      .withTitle("Public item 1")
                                      .withIssueDate("2017-10-17")
                                      .withAuthor("Smith, Donald").withAuthor("Doe, John")
                                      .withSubject("ExtraEntry")
                                      .build();

        Item publicItem2 = ItemBuilder.createItem(context, col2)
                                      .withTitle("Public item 2")
                                      .withIssueDate("2016-02-13")
                                      .withAuthor("Smith, Maria").withAuthor("Doe, Jane")
                                      .withSubject("TestingForMore").withSubject("ExtraEntry")
                                      .build();

        Item publicItem3 = ItemBuilder.createItem(context, col2)
                                      .withTitle("Public item 3")
                                      .withIssueDate("2016-02-13")
                                      .withAuthor("Smith, Maria").withAuthor("Doe, Jane")
                                      .withSubject("AnotherTest").withSubject("TestingForMore")
                                      .withSubject("ExtraEntry")
                                      .build();


        collectionService.addItem(context, col2, publicItem1);
        collectionService.addItem(context, col3, publicItem1);
        collectionService.addItem(context, col2, publicItem1);
        collectionService.addItem(context, col1, publicItem1);
        collectionService.update(context, col2);
        collectionService.update(context, col3);
        itemService.update(context, publicItem1);

        getClient().perform(get("/api/core/items/" + publicItem1.getID() + "/mappingCollections"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$._embedded.mappingCollections", Matchers.not(Matchers.contains(
                       CollectionMatcher.matchCollectionEntry("Collection 1", col1.getID(), col1.getHandle())
                   ))))
                   .andExpect(jsonPath("$._links.self.href", Matchers.containsString("/api/core/items")))
        ;
    }

    @Test
    public void removeMappingCollectionTest() throws Exception {
        context.turnOffAuthorisationSystem();

        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and two collections.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, child1).withName("Collection 2").build();
        Collection col3 = CollectionBuilder.createCollection(context, child1).withName("Collection 3").build();

        //2. Three public items that are readable by Anonymous with different subjects
        Item publicItem1 = ItemBuilder.createItem(context, col1)
                                      .withTitle("Public item 1")
                                      .withIssueDate("2017-10-17")
                                      .withAuthor("Smith, Donald").withAuthor("Doe, John")
                                      .withSubject("ExtraEntry")
                                      .build();

        Item publicItem2 = ItemBuilder.createItem(context, col2)
                                      .withTitle("Public item 2")
                                      .withIssueDate("2016-02-13")
                                      .withAuthor("Smith, Maria").withAuthor("Doe, Jane")
                                      .withSubject("TestingForMore").withSubject("ExtraEntry")
                                      .build();

        Item publicItem3 = ItemBuilder.createItem(context, col2)
                                      .withTitle("Public item 3")
                                      .withIssueDate("2016-02-13")
                                      .withAuthor("Smith, Maria").withAuthor("Doe, Jane")
                                      .withSubject("AnotherTest").withSubject("TestingForMore")
                                      .withSubject("ExtraEntry")
                                      .build();


        collectionService.addItem(context, col2, publicItem1);
        collectionService.addItem(context, col3, publicItem1);
        collectionService.addItem(context, col2, publicItem1);
        collectionService.addItem(context, col1, publicItem1);
        collectionService.update(context, col2);
        collectionService.update(context, col3);
        itemService.update(context, publicItem1);

        getClient().perform(get("/api/core/items/" + publicItem1.getID() + "/mappingCollections"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$._embedded.mappingCollections", Matchers.not(Matchers.contains(
                       CollectionMatcher.matchCollectionEntry("Collection 1", col1.getID(), col1.getHandle()))
                   )))
                   .andExpect(jsonPath("$._links.self.href", Matchers.containsString("/api/core/items")))
        ;

        collectionService.removeItem(context, col2, publicItem1);
        collectionService.update(context, col2);
        itemService.update(context, publicItem1);

        getClient().perform(get("/api/core/items/" + publicItem1.getID() + "/mappingCollections"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$._embedded.mappingCollections", Matchers.not(Matchers.containsInAnyOrder(
                       CollectionMatcher.matchCollectionEntry("Collection 2", col2.getID(), col2.getHandle()),
                       CollectionMatcher.matchCollectionEntry("Collection 1", col1.getID(), col1.getHandle())
                   ))))
                   .andExpect(jsonPath("$._links.self.href", Matchers.containsString("/api/core/items")))
        ;

        collectionService.removeItem(context, col1, publicItem1);
        collectionService.update(context, col1);
        itemService.update(context, publicItem1);

        getClient().perform(get("/api/core/items/" + publicItem1.getID() + "/mappingCollections"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$._embedded.mappingCollections", Matchers.not(Matchers.containsInAnyOrder(
                       CollectionMatcher.matchCollectionEntry("Collection 2", col2.getID(), col2.getHandle()),
                       CollectionMatcher.matchCollectionEntry("Collection 1", col1.getID(), col1.getHandle())
                   ))))
                   .andExpect(jsonPath("$._links.self.href", Matchers.containsString("/api/core/items")))
        ;
    }
}
