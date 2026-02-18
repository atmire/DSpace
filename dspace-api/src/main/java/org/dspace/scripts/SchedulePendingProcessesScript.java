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
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dspace.content.ProcessStatus;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.scripts.factory.ScriptServiceFactory;
import org.dspace.scripts.service.ProcessService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;

public class SchedulePendingProcessesScript {

    private static final ProcessService processService = ScriptServiceFactory.getInstance().getProcessService();
    private static final EPersonService epersonService = EPersonServiceFactory.getInstance().getEPersonService();

    private static final ConfigurationService configurationService =
            DSpaceServicesFactory.getInstance().getConfigurationService();

    private SchedulePendingProcessesScript () {}

    public static void main(String[] args) {
        try {
            CommandLine line = parseArgs(args);
            Context context = new Context(Context.Mode.READ_ONLY);

            List<Process> pendingProcesses = processService.findByStatusAndCreationTimeOlderThan(
                    context, List.of(ProcessStatus.PENDING), new Date());
            if (pendingProcesses.isEmpty()) {
                System.out.println("no processes with a PENDING status found");
                System.exit(0);
            }

            EPerson eperson = epersonService.findByEmail(context, line.getOptionValue('e'));
            if  (eperson == null) {
                System.out.println("no eperson found with email " + line.getOptionValue('e'));
                System.exit(0);
            }

            String apiToken = configurationService.getProperty("api.token");
            String dspaceServerUrl = configurationService.getProperty("dspace.server.url");

            HttpClient client = HttpClient.newHttpClient();
            String csrfToken = getCsrfToken(client, dspaceServerUrl);

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

                    if (response.statusCode() - 200 > 99) {
                        System.out.println("failed to start process \"" + process.getID() +
                                                   "\" with status " + response.statusCode());
                        System.out.println(response.body());
                    } else {
                        System.out.println("started process: " + process.getID());
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println("error while sending patch request for process: " + process.getID() + "\n" +
                            "reason: " + e.getMessage());
                }
            }
        } catch (ParseException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static CommandLine parseArgs(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        options.addOption("e", "eperson", true, "email of eperson scheduling the processes");
        options.addOption("h", "help", false, "help");

        CommandLine line = parser.parse(options, args);
        if (line.hasOption('h')) {
            System.out.println(options);
            System.exit(0);
        }
        if (!line.hasOption('e')) {
            System.out.println("missing required argument \"--eperson\"");
            System.exit(0);
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
            System.out.println("failed to retrieve a csrf token\n" +
                                       "reason: " + e.getMessage());
            System.exit(0);
        }
        return null;
    }
}
