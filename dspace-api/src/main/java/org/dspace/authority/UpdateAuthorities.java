/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority;

import java.io.*;
import java.sql.*;
import java.util.*;
import org.apache.commons.cli.*;
import org.apache.log4j.*;
import org.dspace.authority.factory.*;
import org.dspace.authority.service.*;
import org.dspace.content.*;
import org.dspace.content.factory.*;
import org.dspace.content.service.*;
import org.dspace.core.*;

/**
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class UpdateAuthorities {

    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(UpdateAuthorities.class);

    protected PrintWriter print = null;

    private Context context;
    private List<String> selectedIDs;

    protected final ItemService itemService;
    protected final AuthorityValueService authorityValueService;

    public UpdateAuthorities(Context context) {
        print = new PrintWriter(System.out);
        this.context = context;
        this.authorityValueService = AuthorityServiceFactory.getInstance().getAuthorityValueService();
        this.itemService = ContentServiceFactory.getInstance().getItemService();
    }

    public static void main(String[] args) throws ParseException {

        Context c = null;
        try {
            c = new Context();

            UpdateAuthorities UpdateAuthorities = new UpdateAuthorities(c);
            if (processArgs(args, UpdateAuthorities) == 0) {
                System.exit(0);
            }
            UpdateAuthorities.run();

            c.complete();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (c != null && c.isValid()) {
                c.abort();
            }
        }

    }

    protected static int processArgs(String[] args, UpdateAuthorities UpdateAuthorities) throws ParseException {
        CommandLineParser parser = new PosixParser();
        Options options = createCommandLineOptions();
        CommandLine line = parser.parse(options, args);

        // help

        HelpFormatter helpFormatter = new HelpFormatter();
        if (line.hasOption("h")) {
            helpFormatter.printHelp("dsrun " + UpdateAuthorities.class.getCanonicalName(), options);
            return 0;
        }

        // other arguments
        if (line.hasOption("i")) {
            UpdateAuthorities.setSelectedIDs(line.getOptionValue("i"));
        }

        // print to std out
        UpdateAuthorities.setPrint(new PrintWriter(System.out, true));

        return 1;
    }

    private void setSelectedIDs(String b) {
        this.selectedIDs = new ArrayList<String>();
        String[] orcids = b.split(",");
        for (String orcid : orcids) {
            this.selectedIDs.add(orcid.trim());
        }
    }

    protected static Options createCommandLineOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "help");
        options.addOption("i", "id", true, "Import and/or update specific solr records with the given ids (comma-separated)");
        return options;
    }


    public void run() {
        // This implementation could be very heavy on the REST service.
        // Use with care or make it more efficient.

        List<AuthorityValue> authorities;

        if (selectedIDs != null && !selectedIDs.isEmpty()) {
            authorities = new ArrayList<AuthorityValue>();
            for (String selectedID : selectedIDs) {
                AuthorityValue byID = authorityValueService.findByID(context, selectedID);
                authorities.add(byID);
            }
        } else {
            authorities = authorityValueService.findAll(context);
        }

        if (authorities != null) {
            print.println(authorities.size() + " authorities found.");
            for (AuthorityValue authority : authorities) {
                AuthorityValue updated = authorityValueService.update(authority);
                if (!updated.getLastModified().equals(authority.getLastModified())) {
                    followUp(updated);
                }
            }
        }
    }


    protected void followUp(AuthorityValue authority) {
        print.println("Updated: " + authority.getValue() + " - " + authority.getId());

        boolean updateItems = ConfigurationManager.getBooleanProperty("auto-update-items");
        if (updateItems) {
            updateItems(authority);
        }
    }

    protected void updateItems(AuthorityValue authority) {
        try {
            context.turnOffAuthorisationSystem();
            String field = authority.getField().replaceAll("_", "\\.");
            Iterator<Item> itemIterator = itemService.findByMetadataFieldAuthority(context, field, authority.getId());
            while (itemIterator.hasNext()) {
                Item next = itemIterator.next();
                List<MetadataValue> metadata = itemService.getMetadata(next, field, authority.getId());
                String valueBefore = metadata.get(0).getValue();
                authority.updateItem(context, next, metadata.get(0)); //should be only one

                if (!valueBefore.equals(metadata.get(0).getValue())) {
                    print.println("Updated item with id " + next.getID());
                }
            }
        } catch (Exception e) {
            log.error("Error updating item", e);
            print.println("Error updating item. " + Arrays.toString(e.getStackTrace()));
        }
        finally {
            context.restoreAuthSystemState();
        }
    }


    public PrintWriter getPrint() {
        return print;
    }

    public void setPrint(PrintWriter print) {
        this.print = print;
    }
}
