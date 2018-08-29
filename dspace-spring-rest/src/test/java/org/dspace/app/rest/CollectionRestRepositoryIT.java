/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.dspace.app.rest.builder.CollectionBuilder;
import org.dspace.app.rest.builder.CommunityBuilder;
import org.dspace.app.rest.matcher.CollectionMatcher;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class CollectionRestRepositoryIT extends AbstractControllerIntegrationTest {


    @Test
    public void findAllTest() throws Exception {

        //We turn off the authorization system in order to create the structure as defined below
        context.turnOffAuthorisationSystem();
        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and one collection.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Community child2 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community Two")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, child2).withName("Collection 2").build();


        getClient().perform(get("/api/core/collections"))
                   .andExpect(status().isOk())
                   .andExpect(content().contentType(contentType))
                   .andExpect(jsonPath("$._embedded.collections", Matchers.containsInAnyOrder(
                       CollectionMatcher.matchCollectionEntry(col1.getName(), col1.getID(), col1.getHandle()),
                       CollectionMatcher.matchCollectionEntry(col2.getName(), col2.getID(), col2.getHandle())
                   )));
    }

    @Test
    public void findAllPaginationTest() throws Exception {

        //We turn off the authorization system in order to create the structure as defined below
        context.turnOffAuthorisationSystem();
        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and one collection.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Community child2 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community Two")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, child2).withName("Collection 2").build();


        getClient().perform(get("/api/core/collections")
                                .param("size", "1"))
                   .andExpect(status().isOk())
                   .andExpect(content().contentType(contentType))
                   .andExpect(jsonPath("$._embedded.collections", Matchers.contains(
                       CollectionMatcher.matchCollectionEntry(col1.getName(), col1.getID(), col1.getHandle())
                   )))
                   .andExpect(jsonPath("$._embedded.collections", Matchers.not(
                       Matchers.contains(
                           CollectionMatcher.matchCollectionEntry(col2.getName(), col2.getID(), col2.getHandle())
                       )
                   )));

        getClient().perform(get("/api/core/collections")
                                .param("size", "1")
                                .param("page", "1"))
                   .andExpect(status().isOk())
                   .andExpect(content().contentType(contentType))
                   .andExpect(jsonPath("$._embedded.collections", Matchers.contains(
                       CollectionMatcher.matchCollectionEntry(col2.getName(), col2.getID(), col2.getHandle())
                   )))
                   .andExpect(jsonPath("$._embedded.collections", Matchers.not(
                       Matchers.contains(
                           CollectionMatcher.matchCollectionEntry(col1.getName(), col1.getID(), col1.getHandle())
                       )
                   )));
    }


    @Test
    public void findOneCollectionTest() throws Exception {

        //We turn off the authorization system in order to create the structure as defined below
        context.turnOffAuthorisationSystem();
        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and one collection.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Community child2 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community Two")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, child2).withName("Collection 2").build();


        getClient().perform(get("/api/core/collections/" + col1.getID()))
                   .andExpect(status().isOk())
                   .andExpect(content().contentType(contentType))
                   .andExpect(jsonPath("$", is(
                       CollectionMatcher.matchCollectionEntry(col1.getName(), col1.getID(), col1.getHandle())
                   )))
                   .andExpect(jsonPath("$", Matchers.not(
                       is(
                           CollectionMatcher.matchCollectionEntry(col2.getName(), col2.getID(), col2.getHandle())
                       ))));
    }

    @Test
    public void findOneCollectionRelsTest() throws Exception {

        //We turn off the authorization system in order to create the structure as defined below
        context.turnOffAuthorisationSystem();
        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and one collection.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Community child2 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community Two")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1")
                                           .withLogo("TestingContentForLogo").build();
        Collection col2 = CollectionBuilder.createCollection(context, child2).withName("Collection 2").build();

        getClient().perform(get("/api/core/collections/" + col1.getID()))
                   .andExpect(status().isOk())
                   .andExpect(content().contentType(contentType))
                   .andExpect(jsonPath("$", is(
                       CollectionMatcher.matchCollectionEntry(col1.getName(), col1.getID(), col1.getHandle())
                   )))
                   .andExpect(jsonPath("$", Matchers.not(
                       is(
                           CollectionMatcher.matchCollectionEntry(col2.getName(), col2.getID(), col2.getHandle())
                       )))
                   )
        ;

        getClient().perform(get("/api/core/collections/" + col1.getID() + "/logo"))
                   .andExpect(status().isOk())
                   .andExpect(content().contentType(contentType))
                   .andExpect(jsonPath("$._links.format.href", Matchers.containsString("/api/core/bitstreams")))
                   .andExpect(jsonPath("$._links.format.href", Matchers.containsString("/format")))
                   .andExpect(jsonPath("$._links.self.href", Matchers.containsString("/api/core/bitstreams")))
                   .andExpect(jsonPath("$._links.content.href", Matchers.containsString("/api/core/bitstreams")))
                   .andExpect(jsonPath("$._links.content.href", Matchers.containsString("/content")))
        ;

    }


    @Test
    public void findAuthorizedTest() throws Exception {

        //We turn off the authorization system in order to create the structure as defined below
        context.turnOffAuthorisationSystem();
        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and one collection.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Community child2 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community Two")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, child2).withName("Collection 2").build();

        getClient().perform(get("/api/core/collections/search/findAuthorized"))
                   .andExpect(status().isOk())
                   .andExpect(content().contentType(contentType))
                   .andExpect(jsonPath("$.page.totalElements", is(0)))
                   .andExpect(jsonPath("$._embedded").doesNotExist())
        ;

    }


    @Test
    public void findAuthorizedByCommunityTest() throws Exception {

        //We turn off the authorization system in order to create the structure as defined below
        context.turnOffAuthorisationSystem();
        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and one collection.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Community child2 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community Two")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, child2).withName("Collection 2").build();

        getClient().perform(get("/api/core/collections/search/findAuthorizedByCommunity")
                                .param("uuid", parentCommunity.getID().toString()))
                   .andExpect(status().isOk())
                   .andExpect(content().contentType(contentType))
                   .andExpect(jsonPath("$.page.totalElements", is(0)))
                   .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    public void findAuthorizedByCommunityWithoutUUIDTest() throws Exception {
        getClient().perform(get("/api/core/collections/search/findAuthorizedByCommunity"))
                   .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void findAuthorizedByCommunityWithUnexistentUUIDTest() throws Exception {
        getClient().perform(get("/api/core/collections/search/findAuthorizedByCommunity")
                                .param("uuid", UUID.randomUUID().toString()))
                   .andExpect(status().isNotFound());
    }

    @Test
    public void findOneCollectionTestWrongUUID() throws Exception {

        //We turn off the authorization system in order to create the structure as defined below
        context.turnOffAuthorisationSystem();
        //** GIVEN **
        //1. A community-collection structure with one parent community with sub-community and one collection.
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        Community child2 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community Two")
                                           .build();
        Collection col1 = CollectionBuilder.createCollection(context, child1).withName("Collection 1").build();
        Collection col2 = CollectionBuilder.createCollection(context, child2).withName("Collection 2").build();


        getClient().perform(get("/api/core/collections/" + UUID.randomUUID()))
                   .andExpect(status().isNotFound());
    }

    @Test
    public void createUnauthenticated() throws Exception {
        context.turnOffAuthorisationSystem();
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        context.restoreAuthSystemState();
        getClient().perform(post("/api/core/collections")
                                    .param("name", "test")
                                    .param("parent", parentCommunity.getID().toString()))
                   .andExpect(status().isUnauthorized());
    }

    @Test
    public void createUnauthorized() throws Exception {
        context.turnOffAuthorisationSystem();
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        context.restoreAuthSystemState();
        String nonAdminToken = getAuthToken(eperson.getEmail(), password);

        getClient(nonAdminToken).perform(post("/api/core/collections")
                                                 .param("name", "test")
                                                 .param("parent", parentCommunity.getID().toString()))
                                .andExpect(status().isForbidden());
    }

    @Test
    public void createWithNoName() throws Exception {
        context.turnOffAuthorisationSystem();
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();
        context.restoreAuthSystemState();
        String adminToken = getAuthToken(admin.getEmail(), password);

        getClient(adminToken).perform(post("/api/core/collections")
                                              .param("parent",parentCommunity.getID().toString()))
                             .andExpect(status().isBadRequest());

        getClient(adminToken).perform(post("/api/core/collections")
                                              .param("name", "")
                                              .param("parent",parentCommunity.getID().toString()))
                             .andExpect(status().isBadRequest());
    }

    @Test
    public void createWithParentMalformed() throws Exception {

        String adminToken = getAuthToken(admin.getEmail(), password);

        getClient(adminToken).perform(post("/api/core/collections")
                                              .param("name", "test")
                                              .param("parent", "malformed-uuid"))
                             .andExpect(status().isBadRequest());
    }

    @Test
    public void createWithParentNonExisting() throws Exception {

        String adminToken = getAuthToken(admin.getEmail(), password);

        getClient(adminToken).perform(post("/api/core/collections")
                                              .param("name", "test")
                                              .param("parent", UUID.randomUUID().toString()))
                             .andExpect(status().isNotFound());
    }

    @Test
    public void createWithoutParent() throws Exception {

        String adminToken = getAuthToken(admin.getEmail(), password);

        getClient(adminToken).perform(post("/api/core/collections")
                                              .param("name", "test"))
                             .andExpect(status().isBadRequest());
    }

    @Test
    public void createWithValidParentAndName() throws Exception {

        context.turnOffAuthorisationSystem();

        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("parent")
                                          .build();

        String adminToken = getAuthToken(admin.getEmail(), password);

        String collectionName = "test";
        Map<String, String> metadata = getMetadataMap();
        MockHttpServletRequestBuilder servletRequestBuilder = post("/api/core/collections/");
        servletRequestBuilder.param("name", collectionName);
        servletRequestBuilder.param("parent", parentCommunity.getID().toString());
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            servletRequestBuilder.param(entry.getKey(), entry.getValue());
        }
        ResultActions perform = getClient(adminToken).perform(servletRequestBuilder);
        perform.andExpect(status().isCreated());
        JSONObject json = new JSONObject(perform.andReturn().getResponse().getContentAsString());
        String createdID = String.valueOf(json.get("id"));
        String handle = String.valueOf(json.get("handle"));

        UUID uuid = UUID.fromString(createdID);
        getClient().perform(get("/api/core/collections/" + createdID)).andExpect(status().isOk()).andExpect(
                jsonPath("$",
                         CollectionMatcher.matchCollectionEntry(collectionName, uuid, handle, null, metadata)));

    }

    @Test
    public void deleteByUnauthenticated() throws Exception {
        Collection col1 = createSingleCollection();
        getClient().perform(delete("/api/core/collections/" + col1.getID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteByUnauthorizedNormalEperson() throws Exception {
        Collection col1 = createSingleCollection();
        String nonAdminToken = getAuthToken(eperson.getEmail(), password);
        getClient(nonAdminToken).perform(delete("/api/core/collections/" + col1.getID()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteByUnauthorizedCollectionAdmin() throws Exception {
        context.turnOffAuthorisationSystem();
        parentCommunity = CommunityBuilder.createCommunity(context)
                .withName("Parent Community")
                .build();
        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity).withAdmin(eperson).withName
                ("Collection 1").build();
        context.restoreAuthSystemState();

        String nonAdminToken = getAuthToken(eperson.getEmail(), password);
        getClient(nonAdminToken).perform(delete("/api/core/collections/" + col1.getID()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteByAuthorized() throws Exception {
        Collection col1 = createSingleCollection();

        String adminToken = getAuthToken(admin.getEmail(), password);
        getClient().perform(get("/api/core/collections/" + col1.getID()))
                .andExpect(status().isOk());
        getClient(adminToken).perform(delete("/api/core/collections/" + col1.getID()))
                .andExpect(status().isNoContent());
        getClient().perform(get("/api/core/collections/" + col1.getID()))
                .andExpect(status().isNotFound());
    }


    @Test
    public void updateByUnauthenticated() throws Exception {
        Collection col1 = createSingleCollection();

        getClient().perform(put("/api/core/collections/" + col1.getID())
                .param("name", "UpdatedName"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void updateByUnauthorizedNormalEperson() throws Exception {
        Collection col1 = createSingleCollection();

        String nonAdminToken = getAuthToken(eperson.getEmail(), password);
        getClient(nonAdminToken).perform(put("/api/core/collections/" + col1.getID())
                .param("name", "UpdatedName"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void updateByAuthorizedCollectionAdmin() throws Exception {
        context.turnOffAuthorisationSystem();
        parentCommunity = CommunityBuilder.createCommunity(context)
                .withName("Parent Community")
                .build();
        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity).withAdmin(eperson).withName
                ("Collection 1").build();
        context.restoreAuthSystemState();

        String collectionAdminToken = getAuthToken(eperson.getEmail(), password);
        String updatedName = "UpdatedName";
        Map<String, String> metadata = getMetadataMap();

        MockHttpServletRequestBuilder servletRequestBuilder = put("/api/core/collections/" + col1.getID());
        servletRequestBuilder.param("name", updatedName);
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            servletRequestBuilder.param(entry.getKey(), entry.getValue());
        }
        ResultActions perform = getClient(collectionAdminToken).perform(servletRequestBuilder);

        perform.andExpect(status().isOk());

        perform = getClient().perform(get("/api/core/collections/" + col1.getID()));

        JSONObject json = new JSONObject(perform.andReturn().getResponse().getContentAsString());
        String createdID = String.valueOf(json.get("id"));
        String handle = String.valueOf(json.get("handle"));

        getClient().perform(get("/api/core/collections/" + createdID)).andExpect(status().isOk()).andExpect(
                jsonPath("$", CollectionMatcher
                        .matchCollectionEntry(updatedName, UUID.fromString(createdID), handle, null, metadata)));
    }

    @Test
    public void updateByAuthorizedAdmin() throws Exception {
        Collection col1 = createSingleCollection();

        String adminToken = getAuthToken(admin.getEmail(), password);

        String updatedName = "UpdatedName";
        Map<String, String> metadata = getMetadataMap();
        MockHttpServletRequestBuilder servletRequestBuilder = put("/api/core/collections/" + col1.getID());
        servletRequestBuilder.param("name", updatedName);
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            servletRequestBuilder.param(entry.getKey(), entry.getValue());
        }
        ResultActions perform = getClient(adminToken).perform(servletRequestBuilder);
        perform.andExpect(status().isOk());

        perform = getClient().perform(get("/api/core/collections/" + col1.getID()));

        JSONObject json = new JSONObject(perform.andReturn().getResponse().getContentAsString());
        String createdID = String.valueOf(json.get("id"));
        String handle = String.valueOf(json.get("handle"));

        UUID uuid = UUID.fromString(createdID);
        getClient().perform(get("/api/core/collections/" + createdID)).andExpect(status().isOk())
                   .andExpect(jsonPath("$",
                        CollectionMatcher.matchCollectionEntry(updatedName, uuid, handle, null, metadata)));
    }

    private Collection createSingleCollection() {
        context.turnOffAuthorisationSystem();
        parentCommunity = CommunityBuilder.createCommunity(context)
                .withName("Parent Community")
                .build();
        Collection col1 = CollectionBuilder.createCollection(context, parentCommunity).withName("Collection 1").build();
        context.restoreAuthSystemState();
        return col1;
    }

    private Map<String, String> getMetadataMap() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("dc.rights", "RightsTest");
        metadata.put("dc.rights.license", "RightsLicense");
        metadata.put("dc.description.abstract", "AbstractValue");
        return metadata;
    }

}
