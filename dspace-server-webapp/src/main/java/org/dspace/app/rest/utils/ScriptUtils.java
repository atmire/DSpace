/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.utils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.scripts.handler.impl.RestDSpaceRunnableHandler;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.scripts.DSpaceCommandLineParameter;
import org.dspace.scripts.DSpaceRunnable;
import org.dspace.scripts.configuration.ScriptConfiguration;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class to construct scripts
 */
public class ScriptUtils {

    /**
     * Default constructor
     */
    private ScriptUtils() { }

    /**
     * Method that initializes a DSpaceRunnable and returns it
     * @param files                         Any files that need to be passed to the script for it to run
     * @param context                       The DSpace context
     * @param scriptToExecute               The script configuration to execute
     * @param restDSpaceRunnableHandler     The handler to be used for the DSpaceRunnable
     * @param args                          Any arguments that need to be passed onto the script
     * @param dSpaceRunnable                The DSpaceRunnable that needs to initialized
     * @return  The initialized DSpaceRunnable or null if something went wrong during initialization
     */
    public static DSpaceRunnable prepareDSpaceScript(
            List<MultipartFile> files, Context context, ScriptConfiguration scriptToExecute,
            RestDSpaceRunnableHandler restDSpaceRunnableHandler, List<String> args, DSpaceRunnable dSpaceRunnable)
            throws IOException, SQLException, AuthorizeException, InstantiationException, IllegalAccessException {
        try {
            dSpaceRunnable.initialize(args.toArray(new String[0]), restDSpaceRunnableHandler, context.getCurrentUser());
            if (files != null && !files.isEmpty()) {
                checkFileNames(dSpaceRunnable, files);
                processFiles(context, restDSpaceRunnableHandler, files);
            }
            return dSpaceRunnable;
        } catch (ParseException e) {
            dSpaceRunnable.printHelp();
            try {
                restDSpaceRunnableHandler.handleException(
                        "Failed to parse the arguments given to the script with name: "
                                + scriptToExecute.getName() + " and args: " + args, e
                );
            } catch (Exception re) {
                // ignore re-thrown exception
            }
        }
        return null;
    }

    /**
     * Method that converts a list of DSpaceCommandLineParameters into a list of Strings
     * @param dSpaceCommandLineParameters   The DSpaceCommandLineParameters to convert
     * @return                              A list of String consisting of the given DSpaceCommandLineParameters
     */
    public static List<String> constructArgs(List<DSpaceCommandLineParameter> dSpaceCommandLineParameters) {
        List<String> args = new ArrayList<>();
        for (DSpaceCommandLineParameter parameter : dSpaceCommandLineParameters) {
            args.add(parameter.getName());
            if (parameter.getValue() != null) {
                args.add(parameter.getValue());
            }
        }
        return args;
    }

    /**
     * Method that writes to a filestream of a process for each file given with it
     * @param context                       The DSpace context
     * @param restDSpaceRunnableHandler     The RestDSpaceRunnableHandler to append to
     * @param files                         The files to append
     */
    public static void processFiles(Context context, RestDSpaceRunnableHandler restDSpaceRunnableHandler,
                              List<MultipartFile> files)
            throws IOException, SQLException, AuthorizeException {
        for (MultipartFile file : files) {
            restDSpaceRunnableHandler
                    .writeFilestream(context, file.getOriginalFilename(), file.getInputStream(), "inputfile");
        }
    }

    /**
     * This method checks if the files referenced in the options are actually present for the request
     * If this isn't the case, we'll abort the script now instead of creating issues later on
     * @param dSpaceRunnable   The script that we'll attempt to run
     * @param files             The list of files in the request
     */
    public static void checkFileNames(DSpaceRunnable dSpaceRunnable, List<MultipartFile> files) {
        List<String> fileNames = new LinkedList<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            if (fileNames.contains(fileName)) {
                throw new UnprocessableEntityException("There are two files with the same name: " + fileName);
            } else {
                fileNames.add(fileName);
            }
        }

        List<String> fileNamesFromOptions = dSpaceRunnable.getFileNamesFromInputStreamOptions();
        if (!fileNames.containsAll(fileNamesFromOptions)) {
            throw new UnprocessableEntityException("Files given in properties aren't all present in the request");
        }
    }
}
