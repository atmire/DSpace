package org.dspace.app.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.io.IOException;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.services.ConfigurationService;
import org.junit.Ignore;
import org.junit.Test;


@Ignore
public class AtmireVersionsControllerIT extends AbstractControllerIntegrationTest {


    private static final String CONFIG_NAME = "atmire-versions.directory";
    private static final String DIR = "/home/testing/";
    private static final String NO_DIR = "/this/does/not/exist/";
    private static final String FILE = "atmire-versions.json";

    @Test
    public void test_unauthorisedUserReadsversioning() throws Exception {
        // no token given to ensure the request is not authorised
        getClient().perform(get("/atmire-versions"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void test_adminReadsNotExistingFile() throws Exception {

        ConfigurationService configurationService = mock(ConfigurationService.class);
        when(configurationService.getProperty(CONFIG_NAME)).thenReturn(NO_DIR);

        AtmireVersionsController atmireVersionsController = mock(AtmireVersionsController.class);
        when(atmireVersionsController.readFileAsJson(NO_DIR+FILE)).thenThrow(new IOException("file not found"));

        String token = getAuthToken(admin.getEmail(), password);
        getClient(token).perform(get("/atmire-versions"))
            .andExpect(status().isNotFound());
    }


    @Test
    public void test_adminReadsVersioningFile() throws Exception {

        ConfigurationService configurationService = mock(ConfigurationService.class);
        when(configurationService.getProperty(CONFIG_NAME)).thenReturn(DIR);

        AtmireVersionsController atmireVersionsController = mock(AtmireVersionsController.class);
        when(atmireVersionsController.readFileAsJson(DIR+FILE)).thenReturn("{ 'some':'json' }");

        String token = getAuthToken(admin.getEmail(), password);
        getClient(token).perform(get("/atmire-versions"))
            .andExpect(status().isOk());

    }

}
