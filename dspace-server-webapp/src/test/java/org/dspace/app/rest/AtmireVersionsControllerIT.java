package org.dspace.app.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;


public class AtmireVersionsControllerIT extends AbstractControllerIntegrationTest {


    @Autowired
    private AtmireVersionsController atmireVersionsController;


    @Test
    @Ignore
    public void test_unauthorisedUserReadsversioning() throws Exception {
        // no token given to ensure the request is not authorised
        getClient().perform(get("/api/atmire-versions"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Ignore
    public void test_adminReadsNotExistingFile() throws Exception {
        when(atmireVersionsController.readFileAsJson(anyString())).thenThrow(new IOException("file not found"));

        String token = getAuthToken(admin.getEmail(), password);
        getClient(token).perform(get("/api/atmire-versions"))
            .andExpect(status().isNotFound());
    }


    @Test
    public void test_adminReadsVersioningFile() throws Exception {

        when(atmireVersionsController.readFileAsJson(anyString())).thenReturn("{ 'some':'json' }");

        String token = getAuthToken(admin.getEmail(), password);
        getClient(token).perform(get("/api/atmire-versions"))
            .andExpect(status().isOk())
            .andExpect(content().string("{ 'some':'json' }"))
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header().string("filename", AtmireVersionsController.ATMIRE_VERSIONS_FILE))
            .andExpect(header().string("size", ""+AtmireVersionsController.BUFFER_SIZE));
    }
}
