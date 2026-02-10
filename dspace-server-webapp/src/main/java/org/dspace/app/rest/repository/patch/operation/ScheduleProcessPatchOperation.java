/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository.patch.operation;

import static org.dspace.app.rest.utils.ScriptUtils.constructArgs;
import static org.dspace.app.rest.utils.ScriptUtils.prepareDSpaceScript;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.scripts.handler.impl.RestDSpaceRunnableHandler;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.ProcessStatus;
import org.dspace.core.Context;
import org.dspace.scripts.DSpaceCommandLineParameter;
import org.dspace.scripts.DSpaceRunnable;
import org.dspace.scripts.Process;
import org.dspace.scripts.configuration.ScriptConfiguration;
import org.dspace.scripts.service.ProcessService;
import org.dspace.scripts.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TODO
 */
public class ScheduleProcessPatchOperation extends PatchOperation<Process> {

    @Autowired
    private ProcessService processService;

    @Autowired
    private ScriptService scriptService;

    private static final String OPERATION_SCHEDULE_PROCESS = "processStatus";
    private static final String REQUIRED_STATUS_VALUE = "SCHEDULED";

    @Override
    public Process perform(Context context, Process resource, Operation operation) throws SQLException {
        ScriptConfiguration scriptConfiguration = scriptService.getScriptConfiguration(resource.getName());
        List<DSpaceCommandLineParameter> parameters = processService.getParameters(resource);
        List<String> args = constructArgs(parameters);

        RestDSpaceRunnableHandler restDSpaceRunnableHandler = new RestDSpaceRunnableHandler(
                context.getCurrentUser(), scriptConfiguration.getName(), parameters,
                new HashSet<>(context.getSpecialGroups()));

        try {
            DSpaceRunnable dspaceRunnable = prepareDSpaceScript(
                    null, context, scriptConfiguration, restDSpaceRunnableHandler, args,
                    scriptService.createDSpaceRunnableForScriptConfiguration(scriptConfiguration));

            restDSpaceRunnableHandler.schedule(dspaceRunnable);
        } catch (IOException | AuthorizeException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return processService.find(context, resource.getID());
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return (objectToMatch instanceof Process &&
                ((Process) objectToMatch).getProcessStatus().equals(ProcessStatus.PENDING) &&
                operation.getPath().equals(OPERATION_SCHEDULE_PROCESS) &&
                operation.getValue().equals(REQUIRED_STATUS_VALUE));
    }
}
