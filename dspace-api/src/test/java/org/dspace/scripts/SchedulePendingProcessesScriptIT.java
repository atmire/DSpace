/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.scripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import org.dspace.AbstractIntegrationTestWithDatabase;
import org.dspace.builder.ProcessBuilder;
import org.dspace.content.ProcessStatus;
import org.dspace.scripts.factory.ScriptServiceFactory;
import org.dspace.scripts.service.ProcessService;
import org.junit.Before;
import org.junit.Test;

public class SchedulePendingProcessesScriptIT extends AbstractIntegrationTestWithDatabase {

    private final ProcessService processService = ScriptServiceFactory.getInstance().getProcessService();

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        context.turnOffAuthorisationSystem();
        Process process1 = ProcessBuilder.createProcess(context, admin, "false-start-mock-script", new ArrayList<>())
                                         .withProcessStatus(ProcessStatus.PENDING)
                                         .build();
        Process process2 = ProcessBuilder.createProcess(context, admin, "false-start-mock-script", new ArrayList<>())
                                         .withProcessStatus(ProcessStatus.PENDING)
                                         .build();
        Process process3 = ProcessBuilder.createProcess(context, admin, "false-start-mock-script", new ArrayList<>())
                                         .withProcessStatus(ProcessStatus.PENDING)
                                         .build();
        context.restoreAuthSystemState();
    }

    @Test
    public void testCreatedProcessesHavePendingStatus() throws Exception {
        List<Process> processes = processService.findAll(context);

        assertEquals(3, processes.size());
        processes.forEach(process ->
            assertEquals(ProcessStatus.PENDING, process.getProcessStatus()));
    }

    @Test
    public void testProcessesShouldNotStartWithoutEmailParameter() throws Exception {
        String[] args = new String[] { "schedule-pending-processes" };
        runDSpaceScript(args);

        processService.findAll(context)
                      .forEach(process -> assertEquals(ProcessStatus.PENDING, process.getProcessStatus()));
    }

    @Test
    public void testProcessesShouldNotStartWithEPersonEmailParameter() throws Exception {
        String[] args = new String[] { "schedule-pending-processes", "-e", eperson.getEmail() };
        runDSpaceScript(args);

        processService.findAll(context)
                      .forEach(process -> assertEquals(ProcessStatus.PENDING, process.getProcessStatus()));
    }

    @Test
    public void testProcessesShouldStartWithAdminEmailParameter() throws Exception {
        String[] args = new String[] { "schedule-pending-processes", "-e", eperson.getEmail() };
        runDSpaceScript(args);

        processService.findAll(context)
                      .forEach(process -> assertNotEquals(ProcessStatus.PENDING, process.getProcessStatus()));
    }
}
