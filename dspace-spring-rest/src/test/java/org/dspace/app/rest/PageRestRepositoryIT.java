/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dspace.app.rest.matcher.PageResourceMatcher;
import org.dspace.app.rest.model.PageRest;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.pages.Page;
import org.dspace.pages.service.PageService;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Test class to test {@link org.dspace.app.rest.repository.PageRestRepository}
 */
public class PageRestRepositoryIT extends AbstractControllerIntegrationTest {


    @Autowired
    private PageService pageService;

    /**
     * This is to ensure that our Page objects get deleted when created by REST calls to not have any conflicts
     * with still-existing Bitstreams
     */
    @After
    public void destroy() throws Exception {
        context.turnOffAuthorisationSystem();
        List<Page> list = pageService.findAll(context);
        Iterator<Page> iterator = list.iterator();
        while (iterator.hasNext()) {
            Page page = iterator.next();
            pageService.delete(context, page);
            iterator.remove();
        }
        super.destroy();
    }

    @Test
    public void create() throws Exception {


        //TODO There should be a bitstream here.
        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        MvcResult mvcResult = getClient(authToken).perform(post("/api/config/pages")
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .contentType(contentType))
                                      .andExpect(status().isCreated())
                                      .andExpect(content().contentType(contentType))
                                      .andReturn();


        String content = mvcResult.getResponse().getContentAsString();
        Map<String,Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient(authToken).perform(get("/api/config/pages/" + id)
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .contentType(contentType))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));

    }

    @Test
    public void createNonAuthorized() throws Exception {
        //TODO There should be a bitstream here.
        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        getClient().perform(post("/api/config/pages")
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .contentType(contentType))
                            .andExpect(status().isUnauthorized());
    }

    @Test
    public void createForbidden() throws Exception {
        //TODO There should be a bitstream here.
        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(eperson.getEmail(), password);
        getClient(authToken).perform(post("/api/config/pages")
                                .content(mapper.writeValueAsBytes(pageRest))
                                .contentType(contentType))
                   .andExpect(status().isForbidden());
    }

    @Test
    public void findAll() throws Exception {

    }

    @Test
    public void findOne() throws Exception {

    }

    @Test
    public void deleteTest() throws Exception {
        //TODO There should be a bitstream here.
        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        MvcResult mvcResult = getClient(authToken).perform(post("/api/config/pages")
                                                               .content(mapper.writeValueAsBytes(pageRest))
                                                               .contentType(contentType))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        Map<String,Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient(authToken).perform(delete("/api/config/pages/"+id))
                   .andExpect(status().isNoContent());

        getClient(authToken).perform(get("/api/config/pages/" + id))
                            .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTestNoAdmin() throws Exception {
        //TODO There should be a bitstream here.
        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        MvcResult mvcResult = getClient(authToken).perform(post("/api/config/pages")
                                                               .content(mapper.writeValueAsBytes(pageRest))
                                                               .contentType(contentType))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        Map<String,Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient().perform(delete("/api/config/pages/"+id))
                            .andExpect(status().isUnauthorized());
        getClient().perform(get("/api/config/pages/" + id))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));
    }

}
