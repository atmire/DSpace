/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest;

import org.dspace.app.rest.matcher.TaskMatcher;
import org.dspace.app.rest.test.AbstractControllerIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TaskRestRepositoryIT extends AbstractControllerIntegrationTest {

    @Test
    public void findAll() throws Exception{


        context.turnOffAuthorisationSystem();


        getClient().perform(get("/api/config/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tasks", hasItem(TaskMatcher.matchEntry("bitstore-migrate", "Assetstore migration tool"))))
                .andExpect(jsonPath("$.page.size", is(20)));

    }

    @Test
    @Ignore
    public void findOne() throws Exception{

        context.turnOffAuthorisationSystem();

        getClient().perform(get("/api/config/tasks/bitstore-migrate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", allOf(TaskMatcher.matchEntry("bitstore-migrate", "Assetstore migration tool"))));
    }

    @Test
    public void findOneNormalString() throws Exception{

        context.turnOffAuthorisationSystem();

        getClient().perform(get("/api/config/tasks/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", allOf(TaskMatcher.matchEntry("healthcheck", "Create health check report"))));
    }

}
