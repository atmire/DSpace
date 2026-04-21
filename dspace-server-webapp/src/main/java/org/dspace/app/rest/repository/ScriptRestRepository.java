/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import static org.dspace.app.rest.utils.ScriptUtils.constructArgs;
import static org.dspace.app.rest.utils.ScriptUtils.prepareDSpaceScript;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.converter.DSpaceRunnableParameterConverter;
import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.exception.DuplicateProcessException;
import org.dspace.app.rest.model.ParameterValueRest;
import org.dspace.app.rest.model.ProcessRest;
import org.dspace.app.rest.model.ScriptRest;
import org.dspace.app.rest.scripts.handler.impl.RestDSpaceRunnableHandler;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.ProcessStatus;
import org.dspace.core.Context;
import org.dspace.core.Utils;
import org.dspace.scripts.DSpaceCommandLineParameter;
import org.dspace.scripts.DSpaceRunnable;
import org.dspace.scripts.Process;
import org.dspace.scripts.configuration.ScriptConfiguration;
import org.dspace.scripts.service.ProcessService;
import org.dspace.scripts.service.ScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * This is the REST repository dealing with the Script logic
 */
@Component(ScriptRest.CATEGORY + "." + ScriptRest.PLURAL_NAME)
public class ScriptRestRepository extends DSpaceRestRepository<ScriptRest, String> {

    private static final Logger log = LogManager.getLogger();

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private DSpaceRunnableParameterConverter dSpaceRunnableParameterConverter;

    @Autowired
    private ObjectMapper mapper;

    @Override
    // authorization is verified inside the method
    @PreAuthorize("hasAuthority('AUTHENTICATED')")
    public ScriptRest findOne(Context context, String name) {
        ScriptConfiguration scriptConfiguration = scriptService.getScriptConfiguration(name);
        if (scriptConfiguration != null) {
            if (scriptConfiguration.isAllowedToExecute(context, null)) {
                return converter.toRest(scriptConfiguration, utils.obtainProjection());
            } else {
                throw new AccessDeniedException("The current user was not authorized to access this script");
            }
        }
        return null;
    }

    @Override
    // authorization check is performed inside the script service
    @PreAuthorize("hasAuthority('AUTHENTICATED')")
    public Page<ScriptRest> findAll(Context context, Pageable pageable) {
        List<ScriptConfiguration> scriptConfigurations =
            scriptService.getScriptConfigurations(context);
        return converter.toRestPage(scriptConfigurations, pageable, utils.obtainProjection());
    }

    @Override
    public Class<ScriptRest> getDomainClass() {
        return ScriptRest.class;
    }

    /**
     * This method will take a String scriptname parameter and it'll try to resolve this to a script known by DSpace.
     * If a script is found, it'll start a process for this script with the given properties to this request
     * @param scriptName    The name of the script that will try to be resolved and started
     * @param start         Indicates whether the script should start immediately or not
     * @return A ProcessRest object representing the started process for this script
     * @throws SQLException If something goes wrong
     * @throws IOException  If something goes wrong
     */
    public ProcessRest startProcess(Context context, String scriptName, List<MultipartFile> files, Boolean start)
            throws SQLException, IOException, AuthorizeException, IllegalAccessException, InstantiationException {
        String properties = requestService.getCurrentRequest().getServletRequest().getParameter("properties");
        List<DSpaceCommandLineParameter> dSpaceCommandLineParameters =
            processPropertiesToDSpaceCommandLineParameters(properties);
        ScriptConfiguration scriptToExecute = scriptService.getScriptConfiguration(scriptName);

        if (scriptToExecute == null) {
            throw new ResourceNotFoundException("The script for name: " + scriptName + " wasn't found");
        }
        if (start == null) {
            start = scriptToExecute.allowImmediateStart();
        }
        try {
            if (!scriptToExecute.isAllowedToExecute(context, dSpaceCommandLineParameters)) {
                throw new AuthorizeException("Current user is not eligible to execute script with name: " + scriptName
                        + " and the specified parameters " + StringUtils.join(dSpaceCommandLineParameters, ", "));
            }
            if (!scriptToExecute.allowImmediateStart() && start) {
                throw new IllegalArgumentException("The given script is not allowed to start immediately");
            }
        } catch (IllegalArgumentException e) {
            throw new DSpaceBadRequestException("Illegal argoument " + e.getMessage(), e);
        }
        checkNewProcessForExistingDuplicates(context, scriptName, dSpaceCommandLineParameters, files);
        RestDSpaceRunnableHandler restDSpaceRunnableHandler = new RestDSpaceRunnableHandler(
            context.getCurrentUser(), scriptToExecute.getName(), dSpaceCommandLineParameters,
            new HashSet<>(context.getSpecialGroups()));
        List<String> args = constructArgs(dSpaceCommandLineParameters);
        DSpaceRunnable dspaceRunnable = prepareDSpaceScript(
                files, context, scriptToExecute, restDSpaceRunnableHandler, args,
                scriptService.createDSpaceRunnableForScriptConfiguration(scriptToExecute));
        if (start && dspaceRunnable != null) {
            restDSpaceRunnableHandler.schedule(dspaceRunnable);
        }
        return converter.toRest(restDSpaceRunnableHandler.getProcess(context), utils.obtainProjection());
    }

    private List<DSpaceCommandLineParameter> processPropertiesToDSpaceCommandLineParameters(String propertiesJson)
        throws IOException {
        List<ParameterValueRest> parameterValueRestList = new LinkedList<>();
        if (StringUtils.isNotBlank(propertiesJson)) {
            parameterValueRestList = Arrays.asList(mapper.readValue(propertiesJson, ParameterValueRest[].class));
        }

        List<DSpaceCommandLineParameter> dSpaceCommandLineParameters = new LinkedList<>();
        dSpaceCommandLineParameters.addAll(
            parameterValueRestList.stream().map(x -> dSpaceRunnableParameterConverter.toModel(x))
                                  .collect(Collectors.toList()));
        return dSpaceCommandLineParameters;
    }

    /**
     * This method compares a new script against all PENDING, SCHEDULED or RUNNING processes
     * and throws a {@link DuplicateProcessException} if it finds a match
     * @param context           The current DSpace context
     * @param scriptName        The name of the new script
     * @param parameters        The parameters of the new script
     * @param files             The files of the new script
     * @throws SQLException     When something goes wrong while retrieving the existing processes
     * @throws IOException      When something goes wrong during comparing of file contents
     */
    private void checkNewProcessForExistingDuplicates(Context context, String scriptName,
                                     List<DSpaceCommandLineParameter> parameters, List<MultipartFile> files)
            throws SQLException, IOException {
        // Retrieve all existing processes with a PENDING, SCHEDULED or RUNNING ("in progress") status
        List<Process> inProgressProcesses = processService.findByStatusAndCreationTimeOlderThan(
                context, Arrays.asList(ProcessStatus.PENDING, ProcessStatus.SCHEDULED, ProcessStatus.RUNNING),
                Instant.now());
        // No duplicates found when nu processes are "in progress"
        if (inProgressProcesses.isEmpty()) {
            return;
        }

        // Compare the new script against every "in progress" process
        for (Process process: inProgressProcesses) {
            // Script name is different -> no duplicate
            if (!process.getName().equals(scriptName)) {
                continue;
            }

            // Checks all parameters of a currently existing process equal new parameters
            List<DSpaceCommandLineParameter> processParameters = processService.getParameters(process);
            // Processes are equal when name matches and both parameter lists are empty
            if (processParameters.isEmpty() && (parameters == null || parameters.isEmpty())) {
                throw new DuplicateProcessException(process.getID());
            }

            // Extract file arguments by comparing file names to parameter values
            Set<String> fileArguments = files == null ? new HashSet<>() : parameters
                    .stream()
                    .filter(param -> files.stream()
                                          .anyMatch(f -> Objects.equals(f.getOriginalFilename(), param.getValue())))
                    .map(DSpaceCommandLineParameter::getName)
                    .collect(Collectors.toSet());

            // Map parameters of both processes to Set<String> (excluding file parameters) to make it easier to compare
            Set<String> existingParamSet = processParameters
                    .stream()
                    .filter(param -> !fileArguments.contains(param.getName()))
                    .map(param -> param.getName() + ":" + param.getValue())
                    .collect(Collectors.toSet());
            Set<String> newParamSet = parameters
                    .stream()
                    .filter(param -> !fileArguments.contains(param.getName()))
                    .map(param -> param.getName() + ":" + param.getValue())
                    .collect(Collectors.toSet());

            // Parameters (excluding files) are different -> no duplicate
            if (!existingParamSet.equals(newParamSet)) {
                continue;
            }

            // Check if the amount of input files of the process match the amount of given files
            Set<String> parameterNames = processParameters
                    .stream()
                    .map(DSpaceCommandLineParameter::getValue)
                    .collect(Collectors.toSet());

            Set<String> processInputBitstreamChecksums = processService
                    .getBitstreams(context, process)
                    .stream()
                    .filter(bitstream -> parameterNames.contains(bitstream.getName()))
                    .map(Bitstream::getChecksum)
                    .collect(Collectors.toSet());

            // If files of the new and existing process are empty -> duplicate
            if (files == null && processInputBitstreamChecksums.isEmpty()) {
                throw new DuplicateProcessException(process.getID());
            }

            // If file amount is different -> no duplicates
            if (files == null || processInputBitstreamChecksums.size() != files.size()) {
                continue;
            }

            // Compare new file checksums with existing checksums
            boolean hasNewFile = false;
            for (MultipartFile file : files) {
                try {
                    String fileChecksum = Utils.toHex(this.generateChecksumFrom(file.getInputStream()));
                    // If the new script has any new files -> no duplicates
                    if (!processInputBitstreamChecksums.contains(fileChecksum)) {
                        hasNewFile = true;
                        break;
                    }
                } catch (NoSuchAlgorithmException e) {
                    throw new IOException(e);
                }
            }

            // When a process doesn't have any new files -> duplicate
            if (!hasNewFile) {
                throw new DuplicateProcessException(process.getID());
            }
        }
    }

    private byte[] generateChecksumFrom(InputStream is) throws IOException, NoSuchAlgorithmException {
        try (DigestInputStream dis = new DigestInputStream(is, MessageDigest.getInstance("MD5"))) {
            final int BUFFER_SIZE = 1024 * 4;
            final byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                final int count = dis.read(buffer, 0, BUFFER_SIZE);
                if (count == -1) {
                    break;
                }
            }
            return dis.getMessageDigest().digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

}
