/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package com.atmire.dspace.statistics;

import static java.lang.Integer.parseInt;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Collections.singletonList;
import static org.apache.commons.cli.Option.builder;
import static org.apache.commons.lang.time.DateFormatUtils.format;
import static org.apache.log4j.Logger.getLogger;
import static org.dspace.core.LogManager.getHeader;
import static org.dspace.statistics.SolrLoggerServiceImpl.DATE_FORMAT_8601;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.dspace.core.Context;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.statistics.factory.StatisticsServiceFactory;
import org.dspace.statistics.service.SolrLoggerService;

public class AnonymizeStatistics {

    private static Logger log = getLogger(AnonymizeStatistics.class);
    private static Context context = new Context();
    private static String action = "anonymise_statistics";

    private static final String HELP_OPTION = "h";
    private static final String SLEEP_OPTION = "s";

    private static int sleep;

    private static SolrLoggerService solrLoggerService = StatisticsServiceFactory.getInstance().getSolrLoggerService();
    private static ConfigurationService configurationService =
            DSpaceServicesFactory.getInstance().getConfigurationService();

    private static final int BATCH_SIZE = 100;

    private static final String IP_V4_REGEX = "^((?:\\d{1,3}\\.){3})\\d{1,3}$";
    private static final String IP_V6_REGEX = "^(.*):.*:.*$";

    private static final String IP_V4_MASK =
            configurationService.getProperty("anonymise_statistics.ip_v4_mask", "255");
    private static final String IP_V6_MASK =
            configurationService.getProperty("anonymise_statistics.ip_v6_mask", "FFFF:FFFF");

    private static final Object ANONYMISED = configurationService.getProperty("anonymise_statistics.dns_mask", "anonymised");;

    private static final String TIME_LIMIT;

    static {
        Calendar calendar = Calendar.getInstance();
        calendar.add(DAY_OF_YEAR, -configurationService.getIntProperty("anonymise_statistics.time_limit", 90));
        TIME_LIMIT = format(calendar, DATE_FORMAT_8601);
    }

    private AnonymizeStatistics() {

    }


    public static void main(String... args) throws ParseException {

        parseCommandLineOptions(createCommandLineOptions(), args);
        anonymiseStatistics();
    }

    private static Options createCommandLineOptions() {

        Options options = new Options();

        options.addOption(
                builder(HELP_OPTION)
                        .longOpt("help")
                        .desc("Print the usage of the script")
                        .hasArg(false)
                        .build()
        );

        options.addOption(
                builder(SLEEP_OPTION)
                        .longOpt("sleep")
                        .desc("Sleep a certain time between each solr request")
                        .hasArg(true)
                        .build()
        );

        return options;
    }

    private static void parseCommandLineOptions(Options options, String... args) throws ParseException {

        CommandLine commandLine = new DefaultParser().parse(options, args);

        if (commandLine.hasOption(HELP_OPTION)) {
            printHelp(options);
            System.exit(0);
        }

        if (commandLine.hasOption(SLEEP_OPTION)) {
            sleep = parseInt(commandLine.getOptionValue(SLEEP_OPTION));
        }
    }

    private static void printHelp(Options options) {
        new HelpFormatter().printHelp("dsrun " + AnonymizeStatistics.class.getCanonicalName(), options);
    }

    private static void printInfo(String info) {
        System.out.println(info);
        log.info(getHeader(context, action, info));
    }

    private static void printWarning(String warning) {
        System.out.println(warning);
        log.warn(getHeader(context, action, warning));
    }

    private static void printError(Exception error) {
        error.printStackTrace();
        log.error(getHeader(context, action, error.getMessage()), error);
    }


    private static void anonymiseStatistics() {

        try {

            long updated = 0;
            long total = getDocuments().getResults().getNumFound();
            printInfo(total + " documents to update");

            QueryResponse documents;
            do {
                documents = getDocuments();

                for (SolrDocument document : documents.getResults()) {
                    try {
                        solrLoggerService.update(
                                "uid:" + document.getFieldValue("uid"),
                                "replace",
                                asList(
                                        "ip",
                                        "dns"
                                ),
                                asList(
                                        singletonList(anonymise(document.getFieldValue("ip").toString())),
                                        singletonList(ANONYMISED)
                                )
                        );
                        updated++;
                        printInfo("updated document with uid " + document.getFieldValue("uid"));
                    } catch (Exception e) {
                        printError(e);
                    }
                }
            } while (documents.getResults().getNumFound() > 0);

            printInfo(updated + " documents updated");
            if (updated == total) {
                printInfo("all relevant documents were updated");
            } else {
                printWarning("not all relevant documents were updated, check the DSpace logs for more details");
            }

        } catch (Exception e) {
            printError(e);
        }
    }

    private static QueryResponse getDocuments() throws SolrServerException {

        if (sleep > 0) {
            try {
                printInfo("sleep " + sleep + "ms");
                sleep(sleep);
            } catch (InterruptedException e) {
                printError(e);
                currentThread().interrupt();
            }
        }

        return solrLoggerService.query(
                "ip:*",
                "time:[* TO " + TIME_LIMIT + "] AND -dns:" + ANONYMISED,
                null, BATCH_SIZE, -1, null, null, null, null, null, false, false
        );
    }

    private static Object anonymise(String ip) throws UnknownHostException {

        InetAddress address = InetAddress.getByName(ip);
        if (address instanceof Inet4Address) {
            return ip.replaceFirst(IP_V4_REGEX, "$1" + IP_V4_MASK);
        } else if (address instanceof Inet6Address) {
            return ip.replaceFirst(IP_V6_REGEX, "$1:" + IP_V6_MASK);
        }

        throw new UnknownHostException("unknown ip format: " + ip);
    }
}
