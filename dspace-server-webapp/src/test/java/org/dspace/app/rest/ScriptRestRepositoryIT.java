/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import static com.jayway.jsonpath.JsonPath.read;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.dspace.app.rest.converter.DSpaceRunnableParameterConverter;
import org.dspace.app.rest.matcher.BitstreamMatcher;
import org.dspace.app.rest.matcher.ProcessMatcher;
import org.dspace.app.rest.matcher.ScriptMatcher;
import org.dspace.app.rest.model.ParameterValueRest;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.model.patch.ReplaceOperation;
import org.dspace.app.rest.projection.Projection;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.EPersonBuilder;
import org.dspace.builder.GroupBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.builder.ProcessBuilder;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.ProcessStatus;
import org.dspace.content.service.BitstreamService;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.scripts.DSpaceCommandLineParameter;
import org.dspace.scripts.Process;
import org.dspace.scripts.configuration.ScriptConfiguration;
import org.dspace.scripts.service.ProcessService;
import org.dspace.services.ConfigurationService;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

public class ScriptRestRepositoryIT extends AbstractControllerIntegrationTest {

    @Autowired
    private ProcessService processService;

    @Autowired
    private BitstreamService bitstreamService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private List<ScriptConfiguration> scriptConfigurations;

    @Autowired
    private DSpaceRunnableParameterConverter dSpaceRunnableParameterConverter;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void findAllScriptsTest() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        getClient(token).perform(get("/api/system/scripts"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.scripts", containsInAnyOrder(
                            scriptConfigurations
                                .stream()
                                .map(scriptConfiguration -> ScriptMatcher.matchScript(
                                    scriptConfiguration.getName(),
                                    scriptConfiguration.getDescription()
                                ))
                                .collect(Collectors.toList())
                        )));
    }

    @Test
    public void findAllScriptsSortedAlphabeticallyTest() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        getClient(token).perform(get("/api/system/scripts")
                        .param("size", String.valueOf(scriptConfigurations.size())))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.scripts", contains(
                            scriptConfigurations
                                .stream()
                                .sorted(Comparator.comparing(ScriptConfiguration::getName))
                                .map(scriptConfiguration -> ScriptMatcher.matchScript(
                                    scriptConfiguration.getName(),
                                    scriptConfiguration.getDescription()
                                ))
                                .collect(Collectors.toList())
                        )));
    }


    @Test
    public void findAllScriptsGenericLoggedInUserTest() throws Exception {
        String token = getAuthToken(eperson.getEmail(), password);

        getClient(token).perform(get("/api/system/scripts"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.page.totalElements", is(0)));
    }

    @Test
    public void findAllScriptsAnonymousUserTest() throws Exception {
        // this should be changed once we allow anonymous user to execute some scripts
        getClient().perform(get("/api/system/scripts"))
                   .andExpect(status().isUnauthorized());
    }

    @Test
    public void findAllScriptsLocalAdminsTest() throws Exception {
        context.turnOffAuthorisationSystem();
        EPerson comAdmin = EPersonBuilder.createEPerson(context)
                .withEmail("comAdmin@example.com")
                .withPassword(password).build();
        EPerson colAdmin = EPersonBuilder.createEPerson(context)
                .withEmail("colAdmin@example.com")
                .withPassword(password).build();
        EPerson itemAdmin = EPersonBuilder.createEPerson(context)
                .withEmail("itemAdmin@example.com")
                .withPassword(password).build();
        Community community = CommunityBuilder.createCommunity(context)
                                          .withName("Community")
                                          .withAdminGroup(comAdmin)
                                          .build();
        Collection collection = CollectionBuilder.createCollection(context, community)
                                                .withName("Collection")
                                                .withAdminGroup(colAdmin)
                                                .build();
        ItemBuilder.createItem(context, collection).withAdminUser(itemAdmin)
                .withTitle("Test item to curate").build();
        context.restoreAuthSystemState();
        ScriptConfiguration curateScriptConfiguration =
                scriptConfigurations.stream().filter(scriptConfiguration
                        -> scriptConfiguration.getName().equals("curate"))
            .findAny().get();

        // the local admins have at least access to the curate script
        // and not access to process-cleaner script
        String comAdminToken = getAuthToken(comAdmin.getEmail(), password);
        getClient(comAdminToken).perform(get("/api/system/scripts").param("size", "100"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.scripts", Matchers.hasItem(
                                ScriptMatcher.matchScript(curateScriptConfiguration.getName(),
                                        curateScriptConfiguration.getDescription()))))
                        .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(1)));
        String colAdminToken = getAuthToken(colAdmin.getEmail(), password);
        getClient(colAdminToken).perform(get("/api/system/scripts").param("size", "100"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.scripts", Matchers.hasItem(
                                ScriptMatcher.matchScript(curateScriptConfiguration.getName(),
                                        curateScriptConfiguration.getDescription()))))
                        .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(1)));
        String itemAdminToken = getAuthToken(itemAdmin.getEmail(), password);
        getClient(itemAdminToken).perform(get("/api/system/scripts").param("size", "100"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.scripts", Matchers.hasItem(
                                ScriptMatcher.matchScript(curateScriptConfiguration.getName(),
                                        curateScriptConfiguration.getDescription()))))
                        .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    public void findAllScriptsPaginationTest() throws Exception {
        List<ScriptConfiguration> alphabeticScripts =
            scriptConfigurations.stream()
                                .sorted(Comparator.comparing(ScriptConfiguration::getName))
                                .collect(Collectors.toList());

        int totalPages = scriptConfigurations.size();
        int lastPage = totalPages - 1;

        String token = getAuthToken(admin.getEmail(), password);

        // NOTE: the scripts are always returned in alphabetical order by fully qualified class name.
        getClient(token).perform(get("/api/system/scripts").param("size", "1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.scripts", Matchers.not(Matchers.hasItem(
                            ScriptMatcher.matchScript(alphabeticScripts.get(1).getName(),
                                                      alphabeticScripts.get(1).getDescription())
                        ))))
                        .andExpect(jsonPath("$._embedded.scripts", hasItem(
                            ScriptMatcher.matchScript(alphabeticScripts.get(0).getName(),
                                                      alphabeticScripts.get(0).getDescription())
                        )))
                        .andExpect(jsonPath("$._links.first.href", Matchers.allOf(
                            Matchers.containsString("/api/system/scripts?"),
                            Matchers.containsString("page=0"), Matchers.containsString("size=1"))))
                        .andExpect(jsonPath("$._links.self.href", Matchers.allOf(
                            Matchers.containsString("/api/system/scripts?"),
                            Matchers.containsString("size=1"))))
                        .andExpect(jsonPath("$._links.next.href", Matchers.allOf(
                            Matchers.containsString("/api/system/scripts?"),
                            Matchers.containsString("page=1"), Matchers.containsString("size=1"))))
                        .andExpect(jsonPath("$._links.last.href", Matchers.allOf(
                            Matchers.containsString("/api/system/scripts?"),
                            Matchers.containsString("page=" + lastPage), Matchers.containsString("size=1"))))
                        .andExpect(jsonPath("$.page.size", is(1)))
                        .andExpect(jsonPath("$.page.number", is(0)))
                        .andExpect(jsonPath("$.page.totalPages", is(totalPages)))
                        .andExpect(jsonPath("$.page.totalElements", is(totalPages)));


        getClient(token).perform(get("/api/system/scripts").param("size", "1").param("page", "1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$._embedded.scripts", hasItem(
                            ScriptMatcher.matchScript(alphabeticScripts.get(1).getName(),
                                                      alphabeticScripts.get(1).getDescription())
                        )))
                        .andExpect(jsonPath("$._embedded.scripts", Matchers.not(hasItem(
                            ScriptMatcher.matchScript(alphabeticScripts.get(0).getName(),
                                                      alphabeticScripts.get(0).getDescription())
                        ))))
                        .andExpect(jsonPath("$._links.first.href", Matchers.allOf(
                            Matchers.containsString("/api/system/scripts?"),
                            Matchers.containsString("page=0"), Matchers.containsString("size=1"))))
                        .andExpect(jsonPath("$._links.prev.href", Matchers.allOf(
                            Matchers.containsString("/api/system/scripts?"),
                            Matchers.containsString("page=0"), Matchers.containsString("size=1"))))
                        .andExpect(jsonPath("$._links.self.href", Matchers.allOf(
                            Matchers.containsString("/api/system/scripts?"),
                            Matchers.containsString("page=1"), Matchers.containsString("size=1"))))
                        .andExpect(jsonPath("$._links.next.href", Matchers.allOf(
                            Matchers.containsString("/api/system/scripts?"),
                            Matchers.containsString("page=2"), Matchers.containsString("size=1"))))
                        .andExpect(jsonPath("$._links.last.href", Matchers.allOf(
                            Matchers.containsString("/api/system/scripts?"),
                            Matchers.containsString("page=" + lastPage), Matchers.containsString("size=1"))))
                        .andExpect(jsonPath("$.page.size", is(1)))
                        .andExpect(jsonPath("$.page.number", is(1)))
                        .andExpect(jsonPath("$.page.totalPages", is(totalPages)))
                        .andExpect(jsonPath("$.page.totalElements", is(totalPages)));
    }

    @Test
    public void findOneScriptByNameTest() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        getClient(token).perform(get("/api/system/scripts/mock-script"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", ScriptMatcher
                            .matchMockScript(
                                scriptConfigurations
                                    .stream()
                                    .filter(scriptConfiguration
                                                -> scriptConfiguration.getName().equals("mock-script"))
                                    .findAny()
                                    .orElseThrow()
                                    .getOptions()
                            )
                        ));
    }

    @Test
    public void findOneScriptByNameLocalAdminsTest() throws Exception {
        context.turnOffAuthorisationSystem();
        EPerson comAdmin = EPersonBuilder.createEPerson(context)
                .withEmail("comAdmin@example.com")
                .withPassword(password).build();
        EPerson colAdmin = EPersonBuilder.createEPerson(context)
                .withEmail("colAdmin@example.com")
                .withPassword(password).build();
        EPerson itemAdmin = EPersonBuilder.createEPerson(context)
                .withEmail("itemAdmin@example.com")
                .withPassword(password).build();
        Community community = CommunityBuilder.createCommunity(context)
                                          .withName("Community")
                                          .withAdminGroup(comAdmin)
                                          .build();
        Collection collection = CollectionBuilder.createCollection(context, community)
                                                .withName("Collection")
                                                .withAdminGroup(colAdmin)
                                                .build();
        ItemBuilder.createItem(context, collection).withAdminUser(itemAdmin)
                .withTitle("Test item to curate").build();
        context.restoreAuthSystemState();
        ScriptConfiguration curateScriptConfiguration =
                scriptConfigurations.stream().filter(scriptConfiguration
                        -> scriptConfiguration.getName().equals("curate"))
            .findAny().get();

        String comAdminToken = getAuthToken(comAdmin.getEmail(), password);
        String colAdminToken = getAuthToken(colAdmin.getEmail(), password);
        String itemAdminToken = getAuthToken(itemAdmin.getEmail(), password);
        getClient(comAdminToken).perform(get("/api/system/scripts/" + curateScriptConfiguration.getName()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", ScriptMatcher
                .matchScript(
                        curateScriptConfiguration.getName(),
                        curateScriptConfiguration.getDescription())));
        getClient(colAdminToken).perform(get("/api/system/scripts/" + curateScriptConfiguration.getName()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", ScriptMatcher
                .matchScript(
                        curateScriptConfiguration.getName(),
                        curateScriptConfiguration.getDescription())));
        getClient(itemAdminToken).perform(get("/api/system/scripts/" + curateScriptConfiguration.getName()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", ScriptMatcher
                .matchScript(
                        curateScriptConfiguration.getName(),
                        curateScriptConfiguration.getDescription())));
    }

    @Test
    public void findOneScriptByNameNotAuthenticatedTest() throws Exception {
        getClient().perform(get("/api/system/scripts/mock-script"))
                        .andExpect(status().isUnauthorized());
    }

    @Test
    public void findOneScriptByNameTestAccessDenied() throws Exception {
        String token = getAuthToken(eperson.getEmail(), password);

        getClient(token).perform(get("/api/system/scripts/mock-script"))
                        .andExpect(status().isForbidden());
    }

    @Test
    public void findOneScriptByInvalidNameBadRequestExceptionTest() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        getClient(token).perform(get("/api/system/scripts/mock-script-invalid"))
                   .andExpect(status().isNotFound());
    }

    /**
     * This test will create a basic structure of communities, collections and items with some local admins at each
     * level and verify that the local admins, nor generic users can run scripts reserved to administrator
     * (i.e. default one that don't override the default
     * {@link ScriptConfiguration#isAllowedToExecute(org.dspace.core.Context, List)} method implementation
     */
    @Test
    public void postProcessNonAdminAuthorizeException() throws Exception {
        context.turnOffAuthorisationSystem();
        EPerson comAdmin = EPersonBuilder.createEPerson(context)
                .withEmail("comAdmin@example.com")
                .withPassword(password).build();
        EPerson colAdmin = EPersonBuilder.createEPerson(context)
                .withEmail("colAdmin@example.com")
                .withPassword(password).build();
        EPerson itemAdmin = EPersonBuilder.createEPerson(context)
                .withEmail("itemAdmin@example.com")
                .withPassword(password).build();
        Community community = CommunityBuilder.createCommunity(context)
                                          .withName("Community")
                                          .withAdminGroup(comAdmin)
                                          .build();
        Collection collection = CollectionBuilder.createCollection(context, community)
                                                .withName("Collection")
                                                .withAdminGroup(colAdmin)
                                                .build();
        Item item = ItemBuilder.createItem(context, collection).withAdminUser(itemAdmin)
                                .withTitle("Test item to curate").build();
        context.restoreAuthSystemState();

        String token = getAuthToken(eperson.getEmail(), password);
        String comAdmin_token = getAuthToken(eperson.getEmail(), password);
        String colAdmin_token = getAuthToken(eperson.getEmail(), password);
        String itemAdmin_token = getAuthToken(eperson.getEmail(), password);
        getClient(token).perform(multipart("/api/system/scripts/mock-script/processes"))
                        .andExpect(status().isForbidden());
        getClient(comAdmin_token).perform(multipart("/api/system/scripts/mock-script/processes"))
                        .andExpect(status().isForbidden());
        getClient(colAdmin_token).perform(multipart("/api/system/scripts/mock-script/processes"))
                        .andExpect(status().isForbidden());
        getClient(itemAdmin_token).perform(multipart("/api/system/scripts/mock-script/processes"))
                        .andExpect(status().isForbidden());
    }

    @Test
    public void postProcessAnonymousAuthorizeException() throws Exception {
        getClient().perform(multipart("/api/system/scripts/mock-script/processes"))
                   .andExpect(status().isUnauthorized());
    }

    @Test
    public void postProcessAdminWrongOptionsException() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);
        AtomicReference<Integer> idRef = new AtomicReference<>();

        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes"))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$", is(
                            ProcessMatcher.matchProcess("mock-script",
                                                        String.valueOf(admin.getID()), new LinkedList<>(),
                                                        ProcessStatus.FAILED))))
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }


    }

    @Test
    public void postProcessAdminNoOptionsFailedStatus() throws Exception {

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();

        parameters.add(new DSpaceCommandLineParameter("-z", "test"));
        parameters.add(new DSpaceCommandLineParameter("-q", null));

        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        String token = getAuthToken(admin.getEmail(), password);

        AtomicReference<Integer> idRef = new AtomicReference<>();

        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                 .param("properties", mapper.writeValueAsString(list)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$", is(
                            ProcessMatcher.matchProcess("mock-script",
                                                        String.valueOf(admin.getID()), parameters,
                                                        ProcessStatus.FAILED))))
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void postProcessNonExistingScriptNameException() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        getClient(token).perform(multipart("/api/system/scripts/mock-script-invalid/processes"))
                        .andExpect(status().isNotFound());
    }

    @Test
    public void postProcessAdminWithOptionsSuccess() throws Exception {
        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();

        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));

        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        String token = getAuthToken(admin.getEmail(), password);
        List<ProcessStatus> acceptableProcessStatuses = new LinkedList<>();
        acceptableProcessStatuses.addAll(Arrays.asList(ProcessStatus.SCHEDULED,
                                                       ProcessStatus.RUNNING,
                                                       ProcessStatus.COMPLETED));

        AtomicReference<Integer> idRef = new AtomicReference<>();

        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                 .param("properties", mapper.writeValueAsString(list)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$", is(
                            ProcessMatcher.matchProcess("mock-script",
                                                        String.valueOf(admin.getID()),
                                                        parameters,
                                                        acceptableProcessStatuses))))
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void postProcessAndVerifyOutput() throws Exception {
        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();

        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));

        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        String token = getAuthToken(admin.getEmail(), password);
        List<ProcessStatus> acceptableProcessStatuses = new LinkedList<>();
        acceptableProcessStatuses.addAll(Arrays.asList(ProcessStatus.SCHEDULED,
                                                       ProcessStatus.RUNNING,
                                                       ProcessStatus.COMPLETED));

        AtomicReference<Integer> idRef = new AtomicReference<>();

        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                 .param("properties", mapper.writeValueAsString(list)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$", is(
                            ProcessMatcher.matchProcess("mock-script",
                                                        String.valueOf(admin.getID()),
                                                        parameters,
                                                        acceptableProcessStatuses))))
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));


            Process process = processService.find(context, idRef.get());
            Bitstream bitstream = processService.getBitstream(context, process, Process.OUTPUT_TYPE);


            getClient(token).perform(get("/api/system/processes/" + idRef.get() + "/output"))
                            .andExpect(status().isOk())
                            .andExpect(content().contentType(contentType))
                            .andExpect(jsonPath("$", BitstreamMatcher
                                    .matchBitstreamEntryWithoutEmbed(bitstream.getID(), bitstream.getSizeBytes())));


            MvcResult mvcResult = getClient(token)
                    .perform(get("/api/core/bitstreams/" + bitstream.getID() + "/content")).andReturn();
            String content = mvcResult.getResponse().getContentAsString();

            assertThat(content, CoreMatchers
                    .containsString("INFO mock-script - " + process.getID() + " @ The script has started"));
            assertThat(content,
                       CoreMatchers.containsString(
                               "INFO mock-script - " + process.getID() + " @ Logging INFO for Mock DSpace Script"));
            assertThat(content,
                       CoreMatchers.containsString(
                               "ERROR mock-script - " + process.getID() + " @ Logging ERROR for Mock DSpace Script"));
            assertThat(content,
                       CoreMatchers.containsString("WARNING mock-script - " + process
                               .getID() + " @ Logging WARNING for Mock DSpace Script"));
            assertThat(content, CoreMatchers
                    .containsString("INFO mock-script - " + process.getID() + " @ The script has completed"));




        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }




    @Test
    public void postProcessAdminWithWrongContentTypeBadRequestException() throws Exception {

        String token = getAuthToken(admin.getEmail(), password);

        getClient(token)
                .perform(post("/api/system/scripts/mock-script/processes"))
                .andExpect(status().isBadRequest());

        getClient(token).perform(post("/api/system/scripts/mock-script-invalid/processes"))
                        .andExpect(status().isNotFound());
    }

    @Test
    public void postProcessAdminWithFileSuccess() throws Exception {
        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();

        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));


        context.turnOffAuthorisationSystem();

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

        String bitstreamContent = "Hello, World!";
        MockMultipartFile bitstreamFile = new MockMultipartFile("file",
                                                                "helloProcessFile.txt", MediaType.TEXT_PLAIN_VALUE,
                                                                bitstreamContent.getBytes());
        parameters.add(new DSpaceCommandLineParameter("-f", "helloProcessFile.txt"));

        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        String token = getAuthToken(admin.getEmail(), password);
        List<ProcessStatus> acceptableProcessStatuses = new LinkedList<>();
        acceptableProcessStatuses.addAll(Arrays.asList(ProcessStatus.SCHEDULED,
                                                       ProcessStatus.RUNNING,
                                                       ProcessStatus.COMPLETED));

        AtomicReference<Integer> idRef = new AtomicReference<>();

        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                 .file(bitstreamFile)
                                 .characterEncoding("UTF-8")
                                 .param("properties", mapper.writeValueAsString(list)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$", is(
                            ProcessMatcher.matchProcess("mock-script",
                                                        String.valueOf(admin.getID()),
                                                        parameters,
                                                        acceptableProcessStatuses))))
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void scriptTypeConversionTest() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        getClient(token).perform(get("/api/system/scripts/type-conversion-test"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$", ScriptMatcher
                                .matchScript("type-conversion-test",
                                             "Test the type conversion different option types")))
                        .andExpect(jsonPath("$.parameters", containsInAnyOrder(
                                allOf(
                                        hasJsonPath("$.name", is("-b")),
                                        hasJsonPath("$.description", is("option set to the boolean class")),
                                        hasJsonPath("$.type", is("boolean")),
                                        hasJsonPath("$.mandatory", is(false)),
                                        hasJsonPath("$.nameLong", is("--boolean"))
                                ),
                                allOf(
                                        hasJsonPath("$.name", is("-s")),
                                        hasJsonPath("$.description", is("string option with an argument")),
                                        hasJsonPath("$.type", is("String")),
                                        hasJsonPath("$.mandatory", is(false)),
                                        hasJsonPath("$.nameLong", is("--string"))
                                ),
                                allOf(
                                        hasJsonPath("$.name", is("-n")),
                                        hasJsonPath("$.description", is("string option without an argument")),
                                        hasJsonPath("$.type", is("boolean")),
                                        hasJsonPath("$.mandatory", is(false)),
                                        hasJsonPath("$.nameLong", is("--noargument"))
                                ),
                                allOf(
                                        hasJsonPath("$.name", is("-f")),
                                        hasJsonPath("$.description", is("file option with an argument")),
                                        hasJsonPath("$.type", is("InputStream")),
                                        hasJsonPath("$.mandatory", is(false)),
                                        hasJsonPath("$.nameLong", is("--file"))
                                )
                        ) ));
    }

    @Test
    public void TrackSpecialGroupduringprocessSchedulingTest() throws Exception {
        context.turnOffAuthorisationSystem();

        Group specialGroup = GroupBuilder.createGroup(context)
            .withName("Special Group")
            .addMember(admin)
            .build();

        context.restoreAuthSystemState();

        configurationService.setProperty("authentication-password.login.specialgroup", specialGroup.getName());

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();

        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));

        List<ParameterValueRest> list = parameters.stream()
            .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
            .collect(Collectors.toList());



        String token = getAuthToken(admin.getEmail(), password);
        List<ProcessStatus> acceptableProcessStatuses = new LinkedList<>();
        acceptableProcessStatuses.addAll(Arrays.asList(ProcessStatus.SCHEDULED,
                                                       ProcessStatus.RUNNING,
                                                       ProcessStatus.COMPLETED));

        AtomicReference<Integer> idRef = new AtomicReference<>();

        try {
            getClient(token).perform(post("/api/system/scripts/mock-script/processes")
                                         .contentType("multipart/form-data")
                                         .param("properties", mapper.writeValueAsString(list)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$", is(ProcessMatcher.matchProcess("mock-script",
                                                                        String.valueOf(admin.getID()),
                                                                        parameters, acceptableProcessStatuses))))
                .andDo(result -> idRef.set(read(result.getResponse().getContentAsString(), "$.processId")));

            Process process = processService.find(context, idRef.get());
            List<Group> groups = process.getGroups();
            boolean isPresent = groups.stream().anyMatch(g -> g.getID().equals(specialGroup.getID()));
            assertTrue(isPresent);

        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void processShouldBeCreatedWithPendingStatus() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));
        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        AtomicReference<Integer> idRef = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));


            Process process = processService.find(context, idRef.get());
            assertEquals(ProcessStatus.PENDING, process.getProcessStatus());

        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void processShouldRunImmediatelyWhenStartIsTrue() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));
        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        List<ProcessStatus> acceptableProcessStatuses = new LinkedList<>(
                Arrays.asList(ProcessStatus.SCHEDULED, ProcessStatus.RUNNING, ProcessStatus.COMPLETED)
        );

        AtomicReference<Integer> idRef = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "true"))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$", is(
                            ProcessMatcher.matchProcess("mock-script",
                                                        String.valueOf(admin.getID()),
                                                        parameters,
                                                        acceptableProcessStatuses))))
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));


            Process process = processService.find(context, idRef.get());
            assertNotEquals(ProcessStatus.PENDING, process.getProcessStatus());

            getClient(token).perform(get("/api/system/processes/" + idRef.get() + "/output"))
                            .andExpect(status().isOk());
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void processShouldNotRunImmediatelyWhenStartIsFalse() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));
        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        AtomicReference<Integer> idRef = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));


            Process process = processService.find(context, idRef.get());
            assertEquals(ProcessStatus.PENDING, process.getProcessStatus());

            getClient(token).perform(get("/api/system/processes/" + idRef.get() + "/output"))
                            .andExpect(status().isNoContent());
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void processShouldUseScriptConfigurationStartPropertyWhenStartIsNotGiven() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));
        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        AtomicReference<Integer> idRef1 = new AtomicReference<>();
        AtomicReference<Integer> idRef2 = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list)))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef1
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            Process process1 = processService.find(context, idRef1.get());
            assertNotEquals(ProcessStatus.PENDING, process1.getProcessStatus());

            getClient(token)
                    .perform(multipart("/api/system/scripts/false-start-mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list)))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef2
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            Process process2 = processService.find(context, idRef2.get());
            assertEquals(ProcessStatus.PENDING, process2.getProcessStatus());


        } finally {
            ProcessBuilder.deleteProcess(idRef1.get());
            ProcessBuilder.deleteProcess(idRef2.get());
        }
    }

    @Test
    public void processShouldStartAndCompleteAfterSchedulingViaPatch() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));
        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        AtomicReference<Integer> idRef = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));


            Process process = processService.find(context, idRef.get());
            assertEquals(ProcessStatus.PENDING, process.getProcessStatus());

            List<Operation> operations = List.of(new ReplaceOperation("/processStatus", "SCHEDULED"));
            List<ProcessStatus> acceptableProcessStatuses = new LinkedList<>(
                    Arrays.asList(ProcessStatus.SCHEDULED, ProcessStatus.RUNNING, ProcessStatus.COMPLETED)
            );

            getClient(token).perform(patch("/api/system/processes/" + idRef.get())
                                                 .content(getPatchContent(operations))
                                                 .contentType(MediaType.APPLICATION_JSON_VALUE))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", is(
                                        ProcessMatcher.matchProcess("mock-script",
                                                                    String.valueOf(admin.getID()),
                                                                    parameters,
                                                                    acceptableProcessStatuses))));

            getClient(token).perform(get("/api/system/processes/" + idRef.get() + "/output"))
                            .andExpect(status().isOk());
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void processShouldReceive400WhenStartIsNotAllowedToBeTrue() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));
        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        getClient(token)
                .perform(multipart("/api/system/scripts/false-start-mock-script/processes")
                                 .param("properties", new ObjectMapper().writeValueAsString(list))
                                 .param("start", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void anonymousUsersShouldGet401WhenSchedulingPendingProcess() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));
        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        AtomicReference<Integer> idRef = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            List<Operation> operations = List.of(new ReplaceOperation("/processStatus", "SCHEDULED"));

            getClient().perform(patch("/api/system/processes/" + idRef.get())
                                             .content(getPatchContent(operations))
                                             .contentType(MediaType.APPLICATION_JSON_VALUE))
                            .andExpect(status().isUnauthorized());
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void nonAdminUsersShouldGet403WhenSchedulingPendingProcess() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);
        String userToken = getAuthToken(eperson.getEmail(), password);

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));
        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        AtomicReference<Integer> idRef = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            List<Operation> operations = List.of(new ReplaceOperation("/processStatus", "SCHEDULED"));

            getClient(userToken).perform(patch("/api/system/processes/" + idRef.get())
                                        .content(getPatchContent(operations))
                                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                       .andExpect(status().isForbidden());
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void schedulingProcessWithoutPendingStatusShouldReturn422Response() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));
        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        AtomicReference<Integer> idRef = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "true"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            Process process = processService.find(context, idRef.get());
            assertNotEquals(ProcessStatus.PENDING, process.getProcessStatus());

            List<Operation> operations = List.of(new ReplaceOperation("/processStatus", "SCHEDULED"));

            getClient(token).perform(patch("/api/system/processes/" + idRef.get())
                                                 .content(getPatchContent(operations))
                                                 .contentType(MediaType.APPLICATION_JSON_VALUE))
                                .andExpect(status().isUnprocessableEntity());
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void creatingDuplicateProcessShouldOnlyReturn422WhenExistingProcessHasNotCompletedOrFailed()
            throws Exception {
        context.turnOffAuthorisationSystem();
        Process completedProcess = ProcessBuilder
                .createProcess(context, admin, "index-discovery", new ArrayList<>())
                .withProcessStatus(ProcessStatus.COMPLETED)
                .build();

        Process failedProcess = ProcessBuilder
                .createProcess(context, admin, "index-discovery", new ArrayList<>())
                .withProcessStatus(ProcessStatus.COMPLETED)
                .build();

        Process pendingProcess = ProcessBuilder
                .createProcess(context, admin, "retry-tracker", new ArrayList<>())
                .withProcessStatus(ProcessStatus.PENDING)
                .build();

        Process scheduledProcess = ProcessBuilder
                .createProcess(context, admin, "metadata-deletion", new ArrayList<>())
                .withProcessStatus(ProcessStatus.SCHEDULED)
                .build();

        Process runningProcess = ProcessBuilder
                .createProcess(context, admin, "filter-media", new ArrayList<>())
                .withProcessStatus(ProcessStatus.RUNNING)
                .build();
        context.restoreAuthSystemState();
        context.commit();

        String token = getAuthToken(admin.getEmail(), password);

        AtomicReference<Integer> idRef = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/index-discovery/processes")
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            getClient(token)
                    .perform(multipart("/api/system/scripts/retry-tracker/processes")
                                     .param("start", "false"))
                    .andExpect(status().isUnprocessableEntity());

            getClient(token)
                    .perform(multipart("/api/system/scripts/metadata-deletion/processes")
                                     .param("start", "false"))
                    .andExpect(status().isUnprocessableEntity());

            getClient(token)
                    .perform(multipart("/api/system/scripts/filter-media/processes")
                                     .param("start", "false"))
                    .andExpect(status().isUnprocessableEntity());
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void creatingNoParameterDuplicateOfUncompletedProcessShouldRespondWith422() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        AtomicReference<Integer> idRef = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/index-discovery/processes")
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            getClient(token)
                    .perform(multipart("/api/system/scripts/index-discovery/processes")
                                     .param("start", "false"))
                    .andExpect(status().isUnprocessableEntity());
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void creatingSameOrderParameterDuplicateOfUncompletedProcessShouldRespondWith422() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));
        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        AtomicReference<Integer> idRef = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isUnprocessableEntity());
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void creatingDifferentOrderParameterDuplicateOfUncompletedProcessShouldRespondWith422() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));
        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        AtomicReference<Integer> idRef = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            Collections.reverse(list);

            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isUnprocessableEntity());
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void creatingSameParameterSameFileDuplicateOfUncompletedProcessShouldRespondWith422() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
        parameters.add(new DSpaceCommandLineParameter("-r", "test"));
        parameters.add(new DSpaceCommandLineParameter("-i", null));
        MockMultipartFile file = new MockMultipartFile("file",
                                                                "duplicate_file.txt", MediaType.TEXT_PLAIN_VALUE,
                                                                "duplicate_file".getBytes());
        parameters.add(new DSpaceCommandLineParameter("-f", "duplicate_file.txt"));
        List<ParameterValueRest> list = parameters.stream()
                                                  .map(dSpaceCommandLineParameter -> dSpaceRunnableParameterConverter
                                                          .convert(dSpaceCommandLineParameter, Projection.DEFAULT))
                                                  .collect(Collectors.toList());

        AtomicReference<Integer> idRef = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .file(file)
                                     .characterEncoding("UTF-8")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .file(file)
                                     .characterEncoding("UTF-8")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isUnprocessableEntity());
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void creatingSameParameterSameFileDifferentFileNameDuplicateOfUncompletedProcessShouldRespondWith422()
            throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        AtomicReference<Integer> idRef = new AtomicReference<>();
        try {
            LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
            parameters.add(new DSpaceCommandLineParameter("-r", "test"));
            parameters.add(new DSpaceCommandLineParameter("-i", null));
            MockMultipartFile file1 = new MockMultipartFile("file",
                                                           "duplicate_file_1.txt", MediaType.TEXT_PLAIN_VALUE,
                                                           "duplicate_file".getBytes());
            parameters.add(new DSpaceCommandLineParameter("-f", "duplicate_file_1.txt"));
            List<ParameterValueRest> list = parameters.stream()
                                                      .map(dsCommandLineParameter -> dSpaceRunnableParameterConverter
                                                              .convert(dsCommandLineParameter, Projection.DEFAULT))
                                                      .collect(Collectors.toList());

            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .file(file1)
                                     .characterEncoding("UTF-8")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));


            parameters = new LinkedList<>();
            parameters.add(new DSpaceCommandLineParameter("-r", "test"));
            parameters.add(new DSpaceCommandLineParameter("-i", null));
            MockMultipartFile file2 = new MockMultipartFile("file",
                                                           "duplicate_file_2.txt", MediaType.TEXT_PLAIN_VALUE,
                                                           "duplicate_file".getBytes());
            parameters.add(new DSpaceCommandLineParameter("-f", "duplicate_file_2.txt"));
            list = parameters.stream()
                                                      .map(dsCommandLineParameter -> dSpaceRunnableParameterConverter
                                                              .convert(dsCommandLineParameter, Projection.DEFAULT))
                                                      .collect(Collectors.toList());
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .file(file2)
                                     .characterEncoding("UTF-8")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isUnprocessableEntity());
        } finally {
            ProcessBuilder.deleteProcess(idRef.get());
        }
    }

    @Test
    public void creatingDifferentScriptOfUncompletedProcessShouldSucceed() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        AtomicReference<Integer> idRef1 = new AtomicReference<>();
        AtomicReference<Integer> idRef2 = new AtomicReference<>();
        try {
            getClient(token)
                    .perform(multipart("/api/system/scripts/index-discovery/processes")
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef1
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef2
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));
        } finally {
            ProcessBuilder.deleteProcess(idRef1.get());
            ProcessBuilder.deleteProcess(idRef2.get());
        }
    }

    @Test
    public void creatingSameScriptDifferentParametersOfUncompletedProcessShouldSucceed() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        AtomicReference<Integer> idRef1 = new AtomicReference<>();
        AtomicReference<Integer> idRef2 = new AtomicReference<>();
        try {
            LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
            parameters.add(new DSpaceCommandLineParameter("-r", "test"));
            parameters.add(new DSpaceCommandLineParameter("-i", null));
            List<ParameterValueRest> list = parameters.stream()
                                                      .map(dsCommandLineParameter -> dSpaceRunnableParameterConverter
                                                              .convert(dsCommandLineParameter, Projection.DEFAULT))
                                                      .collect(Collectors.toList());
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef1
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            parameters.add(new DSpaceCommandLineParameter("-o", null));
            list = parameters.stream()
                                                      .map(dsCommandLineParameter -> dSpaceRunnableParameterConverter
                                                              .convert(dsCommandLineParameter, Projection.DEFAULT))
                                                      .collect(Collectors.toList());
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef2
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));
        } finally {
            ProcessBuilder.deleteProcess(idRef1.get());
            ProcessBuilder.deleteProcess(idRef2.get());
        }
    }

    @Test
    public void creatingSameScriptDifferentFilesOfUncompletedProcessShouldSucceed() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        AtomicReference<Integer> idRef1 = new AtomicReference<>();
        AtomicReference<Integer> idRef2 = new AtomicReference<>();
        try {
            LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
            parameters.add(new DSpaceCommandLineParameter("-r", "test"));
            parameters.add(new DSpaceCommandLineParameter("-i", null));
            MockMultipartFile file1 = new MockMultipartFile("file",
                                                            "different_file_1.txt", MediaType.TEXT_PLAIN_VALUE,
                                                            "different_file_1".getBytes());
            parameters.add(new DSpaceCommandLineParameter("-f", "different_file_1.txt"));
            List<ParameterValueRest> list = parameters.stream()
                                                      .map(dsCommandLineParameter -> dSpaceRunnableParameterConverter
                                                              .convert(dsCommandLineParameter, Projection.DEFAULT))
                                                      .collect(Collectors.toList());
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .file(file1)
                                     .characterEncoding("UTF-8")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef1
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            parameters = new LinkedList<>();
            parameters.add(new DSpaceCommandLineParameter("-r", "test"));
            parameters.add(new DSpaceCommandLineParameter("-i", null));
            MockMultipartFile file2 = new MockMultipartFile("file",
                                                            "different_file_2.txt", MediaType.TEXT_PLAIN_VALUE,
                                                            "different_file_2".getBytes());
            parameters.add(new DSpaceCommandLineParameter("-f", "different_file_2.txt"));
            list = parameters.stream()
                                                      .map(dsCommandLineParameter -> dSpaceRunnableParameterConverter
                                                              .convert(dsCommandLineParameter, Projection.DEFAULT))
                                                      .collect(Collectors.toList());
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .file(file2)
                                     .characterEncoding("UTF-8")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef2
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));
        } finally {
            ProcessBuilder.deleteProcess(idRef1.get());
            ProcessBuilder.deleteProcess(idRef2.get());
        }
    }

    @Test
    public void creatingSameScriptDifferentFilesSameFileNameOfUncompletedProcessShouldSucceed() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        AtomicReference<Integer> idRef1 = new AtomicReference<>();
        AtomicReference<Integer> idRef2 = new AtomicReference<>();
        try {
            LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
            parameters.add(new DSpaceCommandLineParameter("-r", "test"));
            parameters.add(new DSpaceCommandLineParameter("-i", null));
            MockMultipartFile file1 = new MockMultipartFile("file",
                                                            "different_file.txt", MediaType.TEXT_PLAIN_VALUE,
                                                            "different_file_1".getBytes());
            parameters.add(new DSpaceCommandLineParameter("-f", "different_file.txt"));
            List<ParameterValueRest> list = parameters.stream()
                                                      .map(dsCommandLineParameter -> dSpaceRunnableParameterConverter
                                                              .convert(dsCommandLineParameter, Projection.DEFAULT))
                                                      .collect(Collectors.toList());
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .file(file1)
                                     .characterEncoding("UTF-8")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef1
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            parameters = new LinkedList<>();
            parameters.add(new DSpaceCommandLineParameter("-r", "test"));
            parameters.add(new DSpaceCommandLineParameter("-i", null));
            MockMultipartFile file2 = new MockMultipartFile("file",
                                                            "different_file.txt", MediaType.TEXT_PLAIN_VALUE,
                                                            "different_file_2".getBytes());
            parameters.add(new DSpaceCommandLineParameter("-f", "different_file.txt"));
            list = parameters.stream()
                             .map(dsCommandLineParameter -> dSpaceRunnableParameterConverter
                                     .convert(dsCommandLineParameter, Projection.DEFAULT))
                             .collect(Collectors.toList());
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .file(file2)
                                     .characterEncoding("UTF-8")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef2
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));
        } finally {
            ProcessBuilder.deleteProcess(idRef1.get());
            ProcessBuilder.deleteProcess(idRef2.get());
        }
    }

    @Test
    public void creatingSameScriptExtraFileParameterThanUncompletedProcessShouldSucceed() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        AtomicReference<Integer> idRef1 = new AtomicReference<>();
        AtomicReference<Integer> idRef2 = new AtomicReference<>();
        try {
            LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
            parameters.add(new DSpaceCommandLineParameter("-r", "test"));
            parameters.add(new DSpaceCommandLineParameter("-i", null));
            MockMultipartFile file1 = new MockMultipartFile("file",
                                                            "file_1.txt", MediaType.TEXT_PLAIN_VALUE,
                                                            "file_1".getBytes());
            parameters.add(new DSpaceCommandLineParameter("-f", "file_1.txt"));
            List<ParameterValueRest> list = parameters.stream()
                                                      .map(dsCommandLineParameter -> dSpaceRunnableParameterConverter
                                                              .convert(dsCommandLineParameter, Projection.DEFAULT))
                                                      .collect(Collectors.toList());
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .file(file1)
                                     .characterEncoding("UTF-8")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef1
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            parameters = new LinkedList<>();
            parameters.add(new DSpaceCommandLineParameter("-r", "test"));
            parameters.add(new DSpaceCommandLineParameter("-i", null));
            MockMultipartFile file2 = new MockMultipartFile("file",
                                                            "file_1.txt", MediaType.TEXT_PLAIN_VALUE,
                                                            "file_1".getBytes());
            parameters.add(new DSpaceCommandLineParameter("-f", "file_1.txt"));
            MockMultipartFile file3 = new MockMultipartFile("file",
                                                            "file_2.txt", MediaType.TEXT_PLAIN_VALUE,
                                                            "file_2".getBytes());
            parameters.add(new DSpaceCommandLineParameter("-g", "file_2.txt"));
            list = parameters.stream()
                             .map(dsCommandLineParameter -> dSpaceRunnableParameterConverter
                                     .convert(dsCommandLineParameter, Projection.DEFAULT))
                             .collect(Collectors.toList());
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .file(file2)
                                     .file(file3)
                                     .characterEncoding("UTF-8")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef2
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));
        } finally {
            ProcessBuilder.deleteProcess(idRef1.get());
            ProcessBuilder.deleteProcess(idRef2.get());
        }
    }

    @Test
    public void creatingScriptDuplicateShouldCheckAllPendingBeforeSucceeding() throws Exception {
        String token = getAuthToken(admin.getEmail(), password);

        AtomicReference<Integer> idRef1 = new AtomicReference<>();
        AtomicReference<Integer> idRef2 = new AtomicReference<>();
        try {
            LinkedList<DSpaceCommandLineParameter> parameters = new LinkedList<>();
            parameters.add(new DSpaceCommandLineParameter("-r", "test"));
            parameters.add(new DSpaceCommandLineParameter("-i", null));
            MockMultipartFile file1 = new MockMultipartFile("file",
                                                            "file_1.txt", MediaType.TEXT_PLAIN_VALUE,
                                                            "file_1".getBytes());
            parameters.add(new DSpaceCommandLineParameter("-f", "file_1.txt"));
            List<ParameterValueRest> list = parameters.stream()
                                                      .map(dsCommandLineParameter -> dSpaceRunnableParameterConverter
                                                              .convert(dsCommandLineParameter, Projection.DEFAULT))
                                                      .collect(Collectors.toList());
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .file(file1)
                                     .characterEncoding("UTF-8")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef1
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            parameters = new LinkedList<>();
            parameters.add(new DSpaceCommandLineParameter("-r", "test"));
            parameters.add(new DSpaceCommandLineParameter("-i", null));
            MockMultipartFile file2 = new MockMultipartFile("file",
                                                            "file_2.txt", MediaType.TEXT_PLAIN_VALUE,
                                                            "file_2".getBytes());
            parameters.add(new DSpaceCommandLineParameter("-f", "file_2.txt"));
            list = parameters.stream()
                             .map(dsCommandLineParameter -> dSpaceRunnableParameterConverter
                                     .convert(dsCommandLineParameter, Projection.DEFAULT))
                             .collect(Collectors.toList());
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .file(file2)
                                     .characterEncoding("UTF-8")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isAccepted())
                    .andDo(result -> idRef2
                            .set(read(result.getResponse().getContentAsString(), "$.processId")));

            parameters = new LinkedList<>();
            parameters.add(new DSpaceCommandLineParameter("-r", "test"));
            parameters.add(new DSpaceCommandLineParameter("-i", null));
            MockMultipartFile file3 = new MockMultipartFile("file",
                                                            "file_2.txt", MediaType.TEXT_PLAIN_VALUE,
                                                            "file_2".getBytes());
            parameters.add(new DSpaceCommandLineParameter("-f", "file_2.txt"));
            list = parameters.stream()
                             .map(dsCommandLineParameter -> dSpaceRunnableParameterConverter
                                     .convert(dsCommandLineParameter, Projection.DEFAULT))
                             .collect(Collectors.toList());
            getClient(token)
                    .perform(multipart("/api/system/scripts/mock-script/processes")
                                     .file(file3)
                                     .characterEncoding("UTF-8")
                                     .param("properties", new ObjectMapper().writeValueAsString(list))
                                     .param("start", "false"))
                    .andExpect(status().isUnprocessableEntity());
        } finally {
            ProcessBuilder.deleteProcess(idRef1.get());
            ProcessBuilder.deleteProcess(idRef2.get());
        }
    }

    @After
    public void destroy() throws Exception {
        context.turnOffAuthorisationSystem();
        CollectionUtils.emptyIfNull(processService.findAll(context)).stream().forEach(process -> {
            try {
                processService.delete(context, process);
            } catch (SQLException | AuthorizeException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        context.restoreAuthSystemState();
        super.destroy();
    }

}
