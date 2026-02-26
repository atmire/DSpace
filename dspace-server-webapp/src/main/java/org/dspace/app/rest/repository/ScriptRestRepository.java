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
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
@Component(ScriptRest.CATEGORY + "." + ScriptRest.NAME)
public class ScriptRestRepository extends DSpaceRestRepository<ScriptRest, String> {

    private static final Logger log = LogManager.getLogger();

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private DSpaceRunnableParameterConverter dSpaceRunnableParameterConverter;

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
        if (!canCreateProcess(context, scriptName, dSpaceCommandLineParameters, files)) {
            throw new DuplicateProcessException();
        }
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
        ObjectMapper objectMapper = new ObjectMapper();
        if (StringUtils.isNotBlank(propertiesJson)) {
            parameterValueRestList = Arrays.asList(objectMapper.readValue(propertiesJson, ParameterValueRest[].class));
        }

        List<DSpaceCommandLineParameter> dSpaceCommandLineParameters = new LinkedList<>();
        dSpaceCommandLineParameters.addAll(
            parameterValueRestList.stream().map(x -> dSpaceRunnableParameterConverter.toModel(x))
                                  .collect(Collectors.toList()));
        return dSpaceCommandLineParameters;
    }

    private boolean canCreateProcess(Context context, String scriptName,
                                     List<DSpaceCommandLineParameter> parameters, List<MultipartFile> files)
            throws SQLException, IOException {
        List<Process> inProgressProcesses = processService.findByStatusAndCreationTimeOlderThan(
                context, Arrays.asList(ProcessStatus.PENDING, ProcessStatus.SCHEDULED, ProcessStatus.RUNNING),
                new Date());
        if (inProgressProcesses.isEmpty()) {
            return true;
        }

        for (Process process:  inProgressProcesses) {
            if (!process.getName().equals(scriptName)) {
                continue;
            }

            // Checks all parameters of a currently existing process equal new parameters
            List<DSpaceCommandLineParameter> processParameters = processService.getParameters(process);
            // Processes are equal when name matches and both parameter lists are empty
            if (processParameters.isEmpty() && (parameters == null || parameters.isEmpty())) {
                return false;
            }

            Set<String> existingParamSet = processParameters.stream().map(param -> param.getName().equals("-f") ?
                                            param.getName() : param.getName() + ":" + param.getValue())
                                    .collect(Collectors.toSet());
            Set<String> newParamSet = parameters.stream().map(param -> param.getName().equals("-f") ?
                                                            param.getName() : param.getName() + ":" + param.getValue())
                                                .collect(Collectors.toSet());
            // Ensure both either have a file parameter or not
            if (existingParamSet.contains("-f") != newParamSet.contains("-f")) {
                continue;
            }
            // Ensure all parameters outside of file are the same
            existingParamSet.remove("-f");
            newParamSet.remove("-f");
            if (!existingParamSet.equals(newParamSet)) {
                continue;
            }

            // Check if the amount of input files of the process match the amount of given files
            Set<String> parameterNames = processParameters.stream()
                                                          .map(DSpaceCommandLineParameter::getValue)
                                                          .collect(Collectors.toSet());
            Set<String> processInputBitstreamChecksums = processService.getBitstreams(context, process).stream().filter(
                bitstream -> parameterNames.contains(bitstream.getName()))
                    .map(Bitstream::getChecksum).collect(Collectors.toSet());
            if (files == null && processInputBitstreamChecksums.isEmpty()) {
                return false;
            }
            if (files == null || processInputBitstreamChecksums.size() != files.size()) {
                continue;
            }

            // Compare new file checksums with existing checksums
            for (MultipartFile file : files) {
                try {
                    String fileChecksum = Utils.toHex(this.generateChecksumFrom(file.getInputStream()));
                    // If there are any new files, allow the new process to be created
                    if (!processInputBitstreamChecksums.contains(fileChecksum)) {
                        return true;
                    }
                } catch (NoSuchAlgorithmException e) {
                    throw new IOException(e);
                }

            }
            return false;
        }

        return true;
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
