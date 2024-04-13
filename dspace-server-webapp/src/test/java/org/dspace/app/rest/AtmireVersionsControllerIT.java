package org.dspace.app.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.junit.Test;

public class AtmireVersionsControllerIT extends AbstractControllerIntegrationTest {
    private final ConfigurationService configurationService =
        DSpaceServicesFactory.getInstance().getConfigurationService();
    @Override
    public void destroy() throws Exception {
        configurationService.setProperty(AtmireVersionsController.CONFIG_DIR, null);
        super.destroy();
    }

    @Test
    public void test_unauthorisedUserReadsversioning() throws Exception {
        // no token given to ensure the request is not authorised
        getClient().perform(get("/api/atmire-versions"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void test_adminReadsNotExistingFile() throws Exception {
        configurationService.setProperty(AtmireVersionsController.CONFIG_DIR, "/something/wrong/");

        String token = getAuthToken(admin.getEmail(), password);
        getClient(token).perform(get("/api/atmire-versions"))
            .andExpect(status().isNotFound());
    }

    @Test
    public void test_adminReadsVersioningFile() throws Exception {

        configurationService.setProperty(AtmireVersionsController.CONFIG_DIR, "src/test/data/dspaceFolder/");

        String token = getAuthToken(admin.getEmail(), password);
        getClient(token).perform(get("/api/atmire-versions"))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"this\":\"is for testing\"}"))
            .andExpect(header().string("Content-Type", "application/json;charset=UTF-8"))
            .andExpect(header().string("Content-Disposition",
                String.format("attachment;filename=\"%s\"",AtmireVersionsController.ATMIRE_VERSIONS_FILE)));
    }
}
