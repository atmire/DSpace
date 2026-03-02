/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.scripts;

import static org.dspace.api.token.service.ApiTokenService.API_TOKEN_HEADER;
import static org.dspace.api.token.service.ApiTokenService.API_USER_HEADER;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.ProcessStatus;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.scripts.factory.ScriptServiceFactory;
import org.dspace.scripts.service.ProcessService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;

/**
 * Schedules all processes that currently have the PENDING status
 *
 * @author Bram Maegerman bram.maegerman@atmire.com
 */
public class SchedulePendingProcessesScript {

    private static final ProcessService processService = ScriptServiceFactory.getInstance().getProcessService();
    private static final EPersonService epersonService = EPersonServiceFactory.getInstance().getEPersonService();
    private static final AuthorizeService authorizeService =
            AuthorizeServiceFactory.getInstance().getAuthorizeService();
    private static final ConfigurationService configurationService =
            DSpaceServicesFactory.getInstance().getConfigurationService();

    private SchedulePendingProcessesScript () {}

    public static void main(String[] argv) {
        schedulePendingProcesses(argv);
    }

    /**
     * Run the schedule-pending-processes logic.
     * This method is called by main() for CLI usage and directly by tests.
     *
     * @param argv the command line arguments
     * @throws RuntimeException if an error occurs during processing
     */
    public static void schedulePendingProcesses(String[] argv) {
        try {
            CommandLine line = parseArgs(argv);
            if (line == null) {
                return;
            }

            Context context = new Context(Context.Mode.READ_ONLY);

            List<Process> pendingProcesses = processService.findByStatusAndCreationTimeOlderThan(
                    context, List.of(ProcessStatus.PENDING), new Date());
            if (pendingProcesses.isEmpty()) {
                System.out.println("No processes with a PENDING status found");
                return;
            }

            EPerson eperson = epersonService.findByEmail(context, line.getOptionValue('e'));
            if  (eperson == null) {
                System.out.println("No EPerson found with email " + line.getOptionValue('e'));
                return;
            }
            if (!authorizeService.isAdmin(context, eperson)) {
                System.out.println("Provided EPerson is not an admin");
                return;
            }

            String apiToken = configurationService.getProperty("api.token");
            if (apiToken == null) {
                System.out.println("No API token found");
                return;
            }

            String dspaceServerUrl = configurationService.getProperty("dspace.server.url");
            if (dspaceServerUrl == null) {
                System.out.println("No server URL configured");
                return;
            }

            HttpClient client = HttpClient.newHttpClient();
            String csrfToken = getCsrfToken(client, dspaceServerUrl);
            if (csrfToken == null) {
                return;
            }

            String patchBody = "[{ \"op\": \"replace\", \"path\": \"/processStatus\", \"value\":\"SCHEDULED\"}]";
            for (Process process : pendingProcesses) {
                try {
                    HttpRequest request = HttpRequest
                            .newBuilder()
                            .uri(URI.create(dspaceServerUrl + "/api/system/processes/" + process.getID()))
                            .method("PATCH", HttpRequest.BodyPublishers.ofString(patchBody))
                            .header("Content-Type", "application/json")
                            .header(API_USER_HEADER, eperson.getID().toString())
                            .header(API_TOKEN_HEADER, apiToken)
                            .header("X-XSRF-TOKEN", csrfToken)
                            .header("Cookie", "DSPACE-XSRF-COOKIE=" + csrfToken)
                            .build();

                    HttpResponse<String> response =
                            client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() != 200) {
                        System.out.println("failed to start process \"" + process.getID() +
                                                   "\" with status " + response.statusCode());
                        System.out.println(response.body());
                    } else {
                        System.out.println("started process: " + process.getID());
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println("Error while sending patch request for process \"" + process.getID() + "\"" +
                                               ", reason:\n\n" + e.getMessage());
                }
            }
        } catch (ParseException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static CommandLine parseArgs(String[] argv) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        options.addOption("e", "eperson", true, "email of eperson scheduling the processes");
        options.addOption("h", "help", false, "prints this help message");

        CommandLine line = parser.parse(options, argv);
        if (line.hasOption('h')) {
            new HelpFormatter().printHelp("ds schedule-pending-processes [options]",
                                      "Schedule all processes that currently have a PENDING status",
                                      options, "");
            return null;
        }
        if (!line.hasOption('e')) {
            System.out.println("Missing required argument \"--eperson\"");
            return null;
        }

        return line;
    }

    private static String getCsrfToken(HttpClient client, String baseUrl) {
        try {
            HttpResponse<String> csrfCookieResponse = client.send(
                    HttpRequest.newBuilder().uri(URI.create(baseUrl + "/api/authn/status")).GET().build(),
                    HttpResponse.BodyHandlers.ofString());

            Optional<String> csrfCookie = csrfCookieResponse.headers().allValues("Set-Cookie").stream()
                                                            .filter(cookie -> cookie.contains("DSPACE-XSRF-COOKIE"))
                                                            .findFirst();

            if (csrfCookie.isPresent()) {
                return csrfCookie.get().split(";")[0].split("=")[1];
            } else {
                throw new RuntimeException("No CSRF cookie found");
            }
        } catch (IOException | InterruptedException | RuntimeException e) {
            System.out.println("Failed to retrieve a csrf token, reason:\n\n" + e.getMessage());
        }
        return null;
    }
}
