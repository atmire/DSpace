/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.api.token.script;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dspace.api.token.ApiToken;
import org.dspace.api.token.ApiTokenServiceImpl;
import org.dspace.api.token.service.ApiTokenService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.utils.DSpace;

/**
 * Create or delete API tokens through the CLI.
 *
 * @author Bram Maegerman (bram.maegerman at atmire.com)
 */
public class ApiTokenCLI {

    protected static final int DEFAULT_EXPIRY_HOURS = 1;

    private static final EPersonService epersonService = EPersonServiceFactory.getInstance().getEPersonService();

    private static final ApiTokenService apiTokenService = new DSpace()
            .getServiceManager().getServiceByName(ApiTokenServiceImpl.class.getName(), ApiTokenServiceImpl.class);

    private ApiTokenCLI() {}

    public static void main(String[] argv) {
        Context context = null;

        try {
            CommandLine line = parseArgs(argv);
            if (line == null) {
                System.exit(1);
            }

            context = new Context(Context.Mode.READ_WRITE);

            EPerson eperson = epersonService.findByEmail(context, line.getOptionValue('e'));
            if  (eperson == null) {
                System.out.println("No EPerson found with email " + line.getOptionValue('e'));
                System.exit(1);
            }
            context.setCurrentUser(eperson);

            if (line.hasOption('c')) {
                // CREATE TOKEN
                Date expiry = Date.from(Instant.now().plusSeconds(DEFAULT_EXPIRY_HOURS * 60 * 60));
                if (line.hasOption("expiry")) {
                    expiry = parseExpiry(line.getOptionValue("expiry"));
                }

                ApiToken newApiToken = apiTokenService.create(context, eperson, expiry);
                if (newApiToken == null) {
                    System.out.println("Something went wrong while trying to create an API token");
                    System.exit(1);
                }

                System.out.println(newApiToken.getToken());
            } else {
                // DELETE TOKEN
                ApiToken apiTokenToDelete = apiTokenService.find(context, eperson, line.getOptionValue('d'));
                if (apiTokenToDelete == null) {
                    System.out.println("No such API token found for the given eperson");
                    System.exit(1);
                }

                apiTokenService.delete(context, apiTokenToDelete);
            }

            context.complete();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            if (context != null) {
                context.abort();
            }
        }
    }

    private static CommandLine parseArgs(String[] argv) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        options.addOption("c", "create", false,
                          "creates a new API token for the given ePerson");
        options.addOption("e", "eperson", true,
                          "email of ePerson creating the token for");
        options.addOption(null, "expiry", true,
                          "the amount time the new token will be active for, example usage: 1(m|h|d)");

        options.addOption("d", "delete", true, "deletes the given token");

        options.addOption("h", "help", false, "prints this help message");

        CommandLine line = parser.parse(options, argv);
        if (line.hasOption('h')) {
            new HelpFormatter().printHelp("ds api-token [options]",
                                          "Create or delete an API token",
                                          options, "");
            return null;
        }

        if (!line.hasOption('c') && !line.hasOption('d')) {
            System.out.println("Missing required script mode! Please use either \"--create\" or \"--delete\"");
            return null;
        }
        if (line.hasOption('c') && line.hasOption('d')) {
            System.out.println("Multiple script modes given! Please use either \"--create\" or \"--delete\"");
            return null;
        }

        if (!line.hasOption('e')) {
            System.out.println("Missing required argument \"--eperson\"");
            return null;
        }

        return line;
    }

    private static Date parseExpiry(String arg) {
        if (arg.length() < 2) {
            System.out.println("Invalid expiry value given");
            System.exit(1);
        }

        char unit = arg.charAt(arg.length() - 1);
        int amount = Integer.parseInt(arg.substring(0, arg.length() - 1));

        switch (unit) {
            case 'm': return Date.from(Instant.now().plus(Duration.ofMinutes(amount)));
            case 'h': return Date.from(Instant.now().plus(Duration.ofHours(amount)));
            case 'd': return Date.from(Instant.now().plus(Duration.ofDays(amount)));
            default: {
                System.out.println("Expiry should end with one of the supported time units: 'm', 'h' or 'd'");
                System.exit(1);
            }
        }
        return null;
    }
}
