/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tika.mime.MimeTypes;
import org.dspace.app.rest.builder.CommunityBuilder;
import org.dspace.app.rest.matcher.PageResourceMatcher;
import org.dspace.app.rest.model.PageRest;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.content.Community;
import org.dspace.pages.Page;
import org.dspace.pages.service.PageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Test class to test {@link org.dspace.app.rest.repository.PageRestRepository}
 */
public class PageRestRepositoryIT extends AbstractControllerIntegrationTest {


    @Autowired
    private PageService pageService;

    private String parentCommunityUUIDString = null;

    @Before
    public void setup() {
        context.turnOffAuthorisationSystem();
        parentCommunity = CommunityBuilder.createCommunity(context)
                                          .withName("Parent Community")
                                          .build();

        parentCommunityUUIDString = String.valueOf(parentCommunity.getID());
        context.restoreAuthSystemState();
    }
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


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();


        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(content, Map.class);
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
        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient().perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                        .file(file)
                                                                        .param("dspaceobject",
                                                                               parentCommunityUUIDString)
                                                                        .param("properties",
                                                                               mapper.writeValueAsString(pageRest)))
                                         .andExpect(status().isUnauthorized())
                                         .andReturn();
    }

    @Test
    public void createForbidden() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());
        String authToken = getAuthToken(eperson.getEmail(), password);

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isForbidden())
                                                  .andReturn();
    }

    @Test
    public void findAll() throws Exception {

    }

    @Test
    public void findOne() throws Exception {

    }

    @Test
    public void deleteTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);

        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient(authToken).perform(delete("/api/config/pages/" + id))
                            .andExpect(status().isNoContent());

        getClient(authToken).perform(get("/api/config/pages/" + id))
                            .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTestNoAdmin() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient().perform(delete("/api/config/pages/" + id))
                   .andExpect(status().isUnauthorized());
        getClient().perform(get("/api/config/pages/" + id))
                   .andExpect(status().isOk())
                   .andExpect(content().contentType(contentType))
                   .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                       UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                   )));
    }


    @Test
    public void createAndUploadFile() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();


        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient(authToken).perform(get("/api/config/pages/" + id))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));
    }

    @Test
    public void createAndUploadFileNoPermission() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient().perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                        .file(file)
                                                                        .param("dspaceobject",
                                                                               parentCommunityUUIDString)
                                                                        .param("properties",
                                                                               mapper.writeValueAsString(pageRest)))
                                         .andExpect(status().isUnauthorized())
                                         .andReturn();
    }

    // This test is on ignore because the search by name doesn't support the dspaceObject yet. Re-enable when fixed
    @Test
    @Ignore
    public void createAndfindLanguagesByNameTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();

        pageRest = new PageRest();
        pageRest.setTitle("testTitle2");
        pageRest.setName("testName2");
        pageRest.setLanguage("testLanguage2");

        input = "Hello, World!";
        file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                     input.getBytes());

        mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                       .file(file)
                                                                       .param("dspaceobject",
                                                                              parentCommunityUUIDString)
                                                                       .param("properties",
                                                                              mapper.writeValueAsString(pageRest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(content().contentType(contentType))
                                        .andReturn();
        pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage3");

        input = "Hello, World!";
        file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                     input.getBytes());

        mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                       .file(file)
                                                                       .param("dspaceobject",
                                                                              parentCommunityUUIDString)
                                                                       .param("properties",
                                                                              mapper.writeValueAsString(pageRest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(content().contentType(contentType))
                                        .andReturn();
        pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage4");

        input = "Hello, World!";
        file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                     input.getBytes());

        mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                       .file(file)
                                                                       .param("dspaceobject",
                                                                              parentCommunityUUIDString)
                                                                       .param("properties",
                                                                              mapper.writeValueAsString(pageRest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(content().contentType(contentType))
                                        .andReturn();

        getClient(authToken).perform(get("/api/config/pages/search/languages")
                                         .param("name", "testName")
                                         .param("dspaceobject", parentCommunityUUIDString))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.page.totalElements", is(3)));
    }

    @Test
    public void createAndUploadFileAndRetrieveContent() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();


        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient(authToken).perform(get("/api/config/pages/" + id)
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .contentType(contentType))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));
        getClient(authToken).perform(get("/api/config/pages/" + id)
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .contentType(contentType))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));

        getClient(authToken).perform(get("/api/config/pages/" + id + "/content"))
                            .andExpect(status().isOk())
                            .andExpect(content().bytes(input.getBytes()));
    }

    @Test
    public void replaceFileAndRetrieveContent() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();


        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient(authToken).perform(get("/api/config/pages/" + id)
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .contentType(contentType))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));

        getClient(authToken).perform(get("/api/config/pages/" + id + "/content"))
                            .andExpect(status().isOk())
                            .andExpect(content().bytes(input.getBytes()));

        String newInput = "Goodbye, World!";
        MockMultipartFile newFile = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                          newInput.getBytes());

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.fileUpload("/api/config/pages/" + id);
        builder.with(new RequestPostProcessor() {
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
                mockHttpServletRequest.setMethod("PUT");
                return mockHttpServletRequest;
            }
        });
        getClient(authToken).perform(builder.file(newFile).param("properties",
                                                                 mapper.writeValueAsString(pageRest))
                                                                       .param("dspaceobject",
                                                                              parentCommunityUUIDString))
                            .andExpect(status().isOk());

        getClient(authToken).perform(get("/api/config/pages/" + id + "/content"))
                            .andExpect(status().isOk())
                            .andExpect(content().bytes(newInput.getBytes()));
    }

    @Test
    public void putLanguageAndTitleTest() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();


        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient(authToken).perform(get("/api/config/pages/" + id)
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .contentType(contentType))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));

        pageRest.setLanguage("ThisIsANewLanguage");
        pageRest.setTitle("ThisIsANewTitle");

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.fileUpload("/api/config/pages/" + id);

        builder.with(new RequestPostProcessor() {
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
                mockHttpServletRequest.setMethod("PUT");
                return mockHttpServletRequest;
            }
        });
        getClient(authToken).perform(builder.file(file)
                                            .param("properties", mapper.writeValueAsString(pageRest))
                                            .param("dspaceobject", parentCommunityUUIDString));

        getClient(authToken).perform(get("/api/config/pages/" + id))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), "ThisIsANewTitle", "ThisIsANewLanguage"
                            )));
    }

    @Test
    public void putLanguageAndTitleTestNoAdminAccess() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();


        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient(authToken).perform(get("/api/config/pages/" + id)
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .param("dspaceobject",
                                                parentCommunityUUIDString)
                                         .contentType(contentType))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));

        pageRest.setLanguage("ThisIsANewLanguage");
        pageRest.setTitle("ThisIsANewTitle");
        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.fileUpload("/api/config/pages/" + id);

        builder.with(new RequestPostProcessor() {
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
                mockHttpServletRequest.setMethod("PUT");
                return mockHttpServletRequest;
            }
        });
        getClient().perform(builder.file(file).param("properties", mapper.writeValueAsString(pageRest)))
                   .andExpect(status().isUnauthorized());


        getClient(authToken).perform(get("/api/config/pages/" + id)
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .param("dspaceobject",
                                                parentCommunityUUIDString)
                                         .contentType(contentType))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), "testTitle", "testLanguage"
                            )));
    }

    @Test
    public void findByParametersTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();

        pageRest = new PageRest();
        pageRest.setTitle("testTitle2");
        pageRest.setName("testName2");
        pageRest.setLanguage("testLanguage2");

        input = "Hello, World!";
        file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                     input.getBytes());

        mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                       .file(file)
                                                                       .param("dspaceobject",
                                                                              parentCommunityUUIDString)
                                                                       .param("properties",
                                                                              mapper.writeValueAsString(pageRest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(content().contentType(contentType))
                                        .andReturn();
        pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage3");

        input = "Hello, World!";
        file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                     input.getBytes());

        mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                       .file(file)
                                                                       .param("dspaceobject",
                                                                              parentCommunityUUIDString)
                                                                       .param("properties",
                                                                              mapper.writeValueAsString(pageRest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(content().contentType(contentType))
                                        .andReturn();
        pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage4");

        input = "Hello, World!";
        file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                     input.getBytes());

        mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                       .file(file)
                                                                       .param("dspaceobject",
                                                                              parentCommunityUUIDString)
                                                                       .param("properties",
                                                                              mapper.writeValueAsString(pageRest)))
                                        .andExpect(status().isCreated())
                                        .andExpect(content().contentType(contentType))
                                        .andReturn();

        String parentCommunityUuidString = String.valueOf(parentCommunity.getID());
        getClient().perform(get("/api/config/pages/search/dso")
                            .param("uuid", parentCommunityUuidString))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.page.totalElements", is(4)));

        getClient().perform(get("/api/config/pages/search/dso")
                                .param("uuid", parentCommunityUuidString)
                                .param("name", "testName"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.page.totalElements", is(3)));

        getClient().perform(get("/api/config/pages/search/dso")
                                .param("uuid", parentCommunityUuidString)
                                .param("name", "testName")
                                .param("format", MimeTypes.PLAIN_TEXT))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.page.totalElements", is(3)));

        getClient().perform(get("/api/config/pages/search/dso")
                                .param("uuid", parentCommunityUuidString)
                                .param("name", "testName")
                                .param("format", MimeTypes.PLAIN_TEXT)
                                .param("language", "testLanguage3"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.page.totalElements", is(1)));

        getClient().perform(get("/api/config/pages/search/dso")
                                .param("uuid", parentCommunityUuidString)
                                .param("name", "testName")
                                .param("format", MimeTypes.PLAIN_TEXT)
                                .param("language", "ThisLanguageWontReturnAnyResults"))
                   .andExpect(status().isBadRequest());

        getClient().perform(get("/api/config/pages/search/dso"))
                   .andExpect(status().isUnprocessableEntity());

        getClient().perform(get("/api/config/pages/search/dso")
                            .param("uuid", String.valueOf(UUID.randomUUID())))
                   .andExpect(status().isNotFound());

    }

    @Test
    public void createNoDSpaceObjectParameterBadRequest() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isBadRequest());
    }

    @Test
    public void createNonExistingDSpaceObjectParameterBadRequest() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                           .file(file)
                                                           .param("dspaceobject",
                                                                  String.valueOf(UUID.randomUUID()))
                                                           .param("properties", mapper
                                                               .writeValueAsString(pageRest)))
                            .andExpect(status().isBadRequest());
    }

    @Test
    public void createInvalidDSpaceObjectParameterBadRequest() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                           .file(file)
                                                           .param("dspaceobject",
                                                                  "invalid uuid")
                                                           .param("properties", mapper
                                                               .writeValueAsString(pageRest)))
                            .andExpect(status().isBadRequest());
    }

    @Test
    public void createInvalidDSpaceObjectParameterWrongTypeBadRequest() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                           .file(file)
                                                           .param("dspaceobject",
                                                                  String.valueOf(eperson.getID()))
                                                           .param("properties", mapper
                                                               .writeValueAsString(pageRest)))
                            .andExpect(status().isBadRequest());
    }

    @Test
    public void putNoDSpaceObjectParameterBadRequest() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();


        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient(authToken).perform(get("/api/config/pages/" + id)
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .contentType(contentType))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));

        pageRest.setLanguage("ThisIsANewLanguage");
        pageRest.setTitle("ThisIsANewTitle");

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.fileUpload("/api/config/pages/" + id);

        builder.with(new RequestPostProcessor() {
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
                mockHttpServletRequest.setMethod("PUT");
                return mockHttpServletRequest;
            }
        });
        getClient(authToken).perform(builder.file(file)
                                            .param("properties", mapper.writeValueAsString(pageRest)))
                            .andExpect(status().isBadRequest());
    }

    @Test
    public void putNonExistingDSpaceObjectParameterBadRequest() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();


        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient(authToken).perform(get("/api/config/pages/" + id)
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .contentType(contentType))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));

        pageRest.setLanguage("ThisIsANewLanguage");
        pageRest.setTitle("ThisIsANewTitle");

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders
            .fileUpload("/api/config/pages/" + id);

        builder.with(new RequestPostProcessor() {
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
                mockHttpServletRequest.setMethod("PUT");
                return mockHttpServletRequest;
            }
        });
        getClient(authToken).perform(builder.file(file)
                                            .param("properties", mapper.writeValueAsString(pageRest))
                                            .param("dspaceobject", String.valueOf(UUID.randomUUID())))
                            .andExpect(status().isBadRequest());
    }

    @Test
    public void putInvalidDSpaceObjectParameterBadRequest() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();


        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient(authToken).perform(get("/api/config/pages/" + id)
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .contentType(contentType))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));

        pageRest.setLanguage("ThisIsANewLanguage");
        pageRest.setTitle("ThisIsANewTitle");

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders
            .fileUpload("/api/config/pages/" + id);

        builder.with(new RequestPostProcessor() {
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
                mockHttpServletRequest.setMethod("PUT");
                return mockHttpServletRequest;
            }
        });
        getClient(authToken).perform(builder.file(file)
                                            .param("properties", mapper.writeValueAsString(pageRest))
                                            .param("dspaceobject", "invalid uuid"))
                            .andExpect(status().isBadRequest());
    }

    @Test
    public void putInvalidDSpaceObjectParameterWrongTypeBadRequest() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();


        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient(authToken).perform(get("/api/config/pages/" + id)
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .contentType(contentType))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));

        pageRest.setLanguage("ThisIsANewLanguage");
        pageRest.setTitle("ThisIsANewTitle");

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders
            .fileUpload("/api/config/pages/" + id);

        builder.with(new RequestPostProcessor() {
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
                mockHttpServletRequest.setMethod("PUT");
                return mockHttpServletRequest;
            }
        });
        getClient(authToken).perform(builder.file(file)
                                            .param("properties", mapper.writeValueAsString(pageRest))
                                            .param("dspaceobject", String.valueOf(eperson.getID())))
                            .andExpect(status().isBadRequest());
    }


    @Test
    public void putChangeDSpaceObjectTest() throws Exception {


        ObjectMapper mapper = new ObjectMapper();
        PageRest pageRest = new PageRest();
        pageRest.setTitle("testTitle");
        pageRest.setName("testName");
        pageRest.setLanguage("testLanguage");

        String authToken = getAuthToken(admin.getEmail(), password);
        String input = "Hello, World!";
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
                                                       input.getBytes());

        MvcResult mvcResult = getClient(authToken).perform(MockMvcRequestBuilders.fileUpload("/api/config/pages")
                                                                                 .file(file)
                                                                                 .param("dspaceobject",
                                                                                        parentCommunityUUIDString)
                                                                                 .param("properties", mapper
                                                                                     .writeValueAsString(pageRest)))
                                                  .andExpect(status().isCreated())
                                                  .andExpect(content().contentType(contentType))
                                                  .andReturn();


        String content = mvcResult.getResponse().getContentAsString();
        Map<String, Object> map = mapper.readValue(content, Map.class);
        String id = String.valueOf(map.get("id"));

        getClient(authToken).perform(get("/api/config/pages/" + id)
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .contentType(contentType))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));

        pageRest.setLanguage("ThisIsANewLanguage");
        pageRest.setTitle("ThisIsANewTitle");

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders
            .fileUpload("/api/config/pages/" + id);

        builder.with(new RequestPostProcessor() {
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
                mockHttpServletRequest.setMethod("PUT");
                return mockHttpServletRequest;
            }
        });
        context.turnOffAuthorisationSystem();
        Community child1 = CommunityBuilder.createSubCommunity(context, parentCommunity)
                                           .withName("Sub Community")
                                           .build();
        context.restoreAuthSystemState();
        getClient(authToken).perform(builder.file(file)
                                            .param("properties", mapper.writeValueAsString(pageRest))
                                            .param("dspaceobject", String.valueOf(child1.getID())))
                            .andExpect(status().isOk());

        getClient(authToken).perform(get("/api/config/pages/" + id)
                                         .content(mapper.writeValueAsBytes(pageRest))
                                         .contentType(contentType))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", PageResourceMatcher.matchPageResource(
                                UUID.fromString(id), pageRest.getName(), pageRest.getTitle(), pageRest.getLanguage()
                            )));
        Page page = pageService.findByUuid(context, UUID.fromString(id));
        assertEquals(page.getdSpaceObject().getID(), child1.getID());
    }
}
