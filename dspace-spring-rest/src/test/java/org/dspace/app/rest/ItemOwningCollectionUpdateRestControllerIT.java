/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.dspace.app.rest.builder.CollectionBuilder;
import org.dspace.app.rest.builder.CommunityBuilder;
import org.dspace.app.rest.builder.ItemBuilder;
import org.dspace.app.rest.matcher.CollectionMatcher;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.junit.Test;

public class ItemOwningCollectionUpdateRestControllerIT extends AbstractControllerIntegrationTest {

    @Test
    public void moveItemTestByAnonymous() throws Exception {

        //Turn off the authorization system, otherwise we can't make the objects
        context.turnOffAuthorisationSystem();

        //** GIVEN **
        //1. A community-collection structure with one parent community and two collections.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, parentCommunity).withName("Collection 2").build();

        //2. A public item that is readable by Anonymous
        Item publicItem1 = ItemBuilder.createItem(context, col1)
                                      .withTitle("Public item 1")
                                      .withIssueDate("2017-10-17")
                                      .withAuthor("Smith, Donald")
                                      .build();


        //When we call this owningCollection/move endpoint
        getClient().perform(post("/api/core/items/" + publicItem1.getID() + "/owningCollection/move/"
                                         + col2.getID()))

                   //We expect a 401 Unauthorized status when performed by anonymous
                   .andExpect(status().isUnauthorized());
    }

    @Test
    public void moveItemTestByAuthorizedUser() throws Exception {

        //Turn off the authorization system, otherwise we can't make the objects
        context.turnOffAuthorisationSystem();

        //** GIVEN **
        //1. A community-collection structure with one parent community and two collections.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, parentCommunity).withName("Collection 2").build();

        //2. A public item that is readable by Anonymous
        Item publicItem1 = ItemBuilder.createItem(context, col1)
                                      .withTitle("Public item 1")
                                      .withIssueDate("2017-10-17")
                                      .withAuthor("Smith, Donald")
                                      .build();

        String token = getAuthToken(admin.getEmail(), password);


        //When we call this owningCollection/move endpoint
        getClient(token)
                .perform(post("/api/core/items/" + publicItem1.getID() + "/owningCollection/move/"
                                      + col2.getID()))

                //We expect a 401 Unauthorized status when performed by anonymous
                .andExpect(status().isOk());
        getClient().perform(get("/api/core/items/" + publicItem1.getID() + "/owningCollection"))
                   .andExpect(jsonPath("$",
                           is(CollectionMatcher.matchCollectionEntry(col2.getName(), col2.getID(), col2.getHandle())
                )));
    }
}
