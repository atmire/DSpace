package org.dspace.app.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dspace.app.rest.matcher.PageResourceMatcher;
import org.dspace.app.rest.model.PageRest;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Test class to test {@link org.dspace.app.rest.repository.PageRestRepository}
 */
@Ignore
public class PageRestRepositoryIT extends AbstractControllerIntegrationTest {

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

        getClient(authToken).perform(get("/api/config/pages/"+id)
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
}
