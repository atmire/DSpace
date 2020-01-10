/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.harvest;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.service.EPersonService;
import org.dspace.handle.factory.HandleServiceFactory;
import org.dspace.harvest.HarvestedCollection;
import org.dspace.harvest.HarvestingException;
import org.dspace.harvest.OAIHarvester;
import org.dspace.harvest.factory.HarvestServiceFactory;
import org.dspace.harvest.service.HarvestedCollectionService;
import org.dspace.scripts.DSpaceRunnable;
import org.springframework.beans.factory.annotation.Autowired;

public class Harvest extends DSpaceRunnable {

    @Autowired
    private HarvestedCollectionService harvestedCollectionService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private EPersonService ePersonService;

    private Context context;

    private String command = null;
    private String eperson = null;
    private String collection = null;
    private String oaiSource = null;
    private String oaiSetID = null;
    private String metadataKey = null;
    private int harvestType = 0;
    private boolean help = false;

    private Harvest() {
        Options options = constructOptions();
        this.options = options;
    }

    private Options constructOptions() {
        Options options = new Options();

        options.addOption("p", "purge", false, "delete all items in the collection");
        options.getOption("p").setType(String.class);
        options.addOption("r", "run", false, "run the standard harvest procedure");
        options.getOption("r").setType(String.class);
        options.addOption("g", "ping", false, "test the OAI server and set");
        options.getOption("g").setType(String.class);
        options.addOption("o", "once", false, "run the harvest procedure with specified parameters");
        options.getOption("o").setType(String.class);
        options.addOption("s", "setup", false, "Set the collection up for harvesting");
        options.getOption("s").setType(String.class);
        options.addOption("S", "start", false, "start the harvest loop");
        options.getOption("S").setType(String.class);
        options.addOption("R", "reset", false, "reset harvest status on all collections");
        options.getOption("R").setType(String.class);
        options.addOption("P", "purge", false, "purge all harvestable collections");
        options.getOption("P").setType(String.class);


        options.addOption("e", "eperson", true,
                          "eperson");
        options.getOption("e").setType(String.class);
        options.addOption("c", "collection", true,
                          "harvesting collection (handle or id)");
        options.getOption("c").setType(String.class);
        options.addOption("t", "type", true,
                          "type of harvesting (0 for none)");
        options.getOption("t").setType(Integer.class);
        options.addOption("a", "address", true,
                          "address of the OAI-PMH server");
        options.getOption("a").setType(String.class);
        options.addOption("i", "oai_set_id", true,
                          "id of the PMH set representing the harvested collection");
        options.getOption("i").setType(String.class);
        options.addOption("m", "metadata_format", true,
                          "the name of the desired metadata format for harvesting, resolved to namespace and " +
                              "crosswalk in dspace.cfg");
        options.getOption("m").setType(String.class);

        options.addOption("h", "help", false, "help");
        options.getOption("h").setType(Boolean.class);

        return options;
    }

    public void internalRun() throws Exception {
        if (help) {
            handler.logInfo("\nPING OAI server: Harvest -g -a oai_source -i oai_set_id");
            handler.logInfo(
                "RUNONCE harvest with arbitrary options: Harvest -o -e eperson -c collection -t harvest_type -a " +
                    "oai_source -i oai_set_id -m metadata_format");
            handler.logInfo(
                "SETUP a collection for harvesting: Harvest -s -c collection -t harvest_type -a oai_source -i " +
                    "oai_set_id -m metadata_format");
            handler.logInfo("RUN harvest once: Harvest -r -e eperson -c collection");
            handler.logInfo("START harvest scheduler: Harvest -S");
            handler.logInfo("RESET all harvest status: Harvest -R");
            handler.logInfo("PURGE a collection of items and settings: Harvest -p -e eperson -c collection");
            handler.logInfo("PURGE all harvestable collections: Harvest -P -e eperson");
            printHelp();
            return;
        }

        context = new Context(Context.Mode.BATCH_EDIT);

        if (command == null) {
            handler.logError("Error - no parameters specified (run with -h flag for details)");
        } else if ("run".equals(command)) {
            // Run a single harvest cycle on a collection using saved settings.
            if (collection == null || eperson == null) {
                handler.logError("Error - a target collection and eperson must be provided");
                handler.logError(" (run with -h flag for details)");
            }

            runHarvest(collection, eperson);
        } else if ("start".equals(command)) {
            // start the harvest loop
            startHarvester();
        } else if ("reset".equals(command)) {
            // reset harvesting status
            resetHarvesting();
        } else if ("purgeAll".equals(command)) {
            // purge all collections that are set up for harvesting (obviously for testing purposes only)
            if (eperson == null) {
                handler.logError("Error - an eperson must be provided");
                handler.logError(" (run with -h flag for details)");
            }

            List<HarvestedCollection> harvestedCollections = harvestedCollectionService.findAll(context);
            for (HarvestedCollection harvestedCollection : harvestedCollections) {
                handler.logInfo("Purging the following collections (deleting items and resetting harvest status): " +
                                    harvestedCollection.getCollection().getID().toString());
                purgeCollection(harvestedCollection.getCollection().getID().toString(), eperson);
            }
            context.complete();
        } else if ("purge".equals(command)) {
            // Delete all items in a collection. Useful for testing fresh harvests.
            if (collection == null || eperson == null) {
                handler.logError("Error - a target collection and eperson must be provided");
                handler.logError(" (run with -h flag for details)");
            }

            purgeCollection(collection, eperson);
            context.complete();

            //TODO: implement this... remove all items and remember to unset "last-harvested" settings
        } else if ("config".equals(command)) {
            // Configure a collection with the three main settings
            if (collection == null) {
                handler.logError("Error -  a target collection must be provided");
                handler.logError(" (run with -h flag for details)");
            }
            if (oaiSource == null || oaiSetID == null) {
                handler.logError("Error - both the OAI server address and OAI set id must be specified");
                handler.logError(" (run with -h flag for details)");
            }
            if (metadataKey == null) {
                handler.logError("Error - a metadata key (commonly the prefix) must be specified for this collection");
                handler.logError(" (run with -h flag for details)");
            }

            configureCollection(collection, harvestType, oaiSource, oaiSetID, metadataKey);
        } else if ("ping".equals(command)) {
            if (oaiSource == null || oaiSetID == null) {
                handler.logError("Error - both the OAI server address and OAI set id must be specified");
                handler.logError(" (run with -h flag for details)");
            }

            pingResponder(oaiSource, oaiSetID, metadataKey);
        }

    }

    /*
     * Resolve the ID into a collection and check to see if its harvesting options are set. If so, return
     * the collection, if not, bail out.
     */
    private Collection resolveCollection(String collectionID) {

        DSpaceObject dso;
        Collection targetCollection = null;

        try {
            // is the ID a handle?
            if (collectionID != null) {
                if (collectionID.indexOf('/') != -1) {
                    // string has a / so it must be a handle - try and resolve it
                    dso = HandleServiceFactory.getInstance().getHandleService().resolveToObject(context, collectionID);

                    // resolved, now make sure it's a collection
                    if (dso == null || dso.getType() != Constants.COLLECTION) {
                        targetCollection = null;
                    } else {
                        targetCollection = (Collection) dso;
                    }
                } else {
                    // not a handle, try and treat it as an collection database UUID
                    handler.logInfo("Looking up by UUID: " + collectionID + ", " + "in context: " + context);
                    targetCollection = collectionService.find(context, UUID.fromString(collectionID));
                }
            }
            // was the collection valid?
            if (targetCollection == null) {
                handler.logError("Cannot resolve " + collectionID + " to collection");
            }
        } catch (SQLException se) {
            handler.handleException(se);
        }

        return targetCollection;
    }


    private void configureCollection(String collectionID, int type, String oaiSource, String oaiSetId,
                                     String mdConfigId) {
        handler.logInfo("Running: configure collection");

        Collection collection = resolveCollection(collectionID);
        handler.logInfo(String.valueOf(collection.getID()));

        try {
            HarvestedCollection hc = harvestedCollectionService.find(context, collection);
            if (hc == null) {
                hc = harvestedCollectionService.create(context, collection);
            }

            context.turnOffAuthorisationSystem();
            hc.setHarvestParams(type, oaiSource, oaiSetId, mdConfigId);
            hc.setHarvestStatus(HarvestedCollection.STATUS_READY);
            harvestedCollectionService.update(context, hc);
            context.restoreAuthSystemState();
            context.complete();
        } catch (Exception e) {
            handler.logError("Changes could not be committed");
            handler.handleException(e);
        } finally {
            if (context != null) {
                context.restoreAuthSystemState();
            }
        }
    }


    /**
     * Purges a collection of all harvest-related data and settings. All items in the collection will be deleted.
     *
     * @param collectionID
     * @param email
     */
    private void purgeCollection(String collectionID, String email) {
        handler.logInfo("Purging collection of all items and resetting last_harvested and harvest_message: " +
                            collectionID);
        Collection collection = resolveCollection(collectionID);

        try {
            EPerson eperson = ePersonService.findByEmail(context, email);
            context.setCurrentUser(eperson);
            context.turnOffAuthorisationSystem();

            ItemService itemService = ContentServiceFactory.getInstance().getItemService();
            Iterator<Item> it = itemService.findByCollection(context, collection);
            int i = 0;
            while (it.hasNext()) {
                i++;
                Item item = it.next();
                handler.logInfo("Deleting: " + item.getHandle());
                collectionService.removeItem(context, collection, item);
                context.uncacheEntity(item);// Dispatch events every 50 items
                if (i % 50 == 0) {
                    context.dispatchEvents();
                    i = 0;
                }
            }

            HarvestedCollection hc = harvestedCollectionService.find(context, collection);
            if (hc != null) {
                hc.setLastHarvested(null);
                hc.setHarvestMessage("");
                hc.setHarvestStatus(HarvestedCollection.STATUS_READY);
                hc.setHarvestStartTime(null);
                harvestedCollectionService.update(context, hc);
            }
            context.restoreAuthSystemState();
            context.dispatchEvents();
        } catch (Exception e) {
            handler.logError("Changes could not be committed");
            handler.handleException(e);
        } finally {
            context.restoreAuthSystemState();
        }
    }


    /**
     * Run a single harvest cycle on the specified collection under the authorization of the supplied EPerson
     */
    private void runHarvest(String collectionID, String email) {
        handler.logInfo("Running: a harvest cycle on " + collectionID);

        handler.logInfo("Initializing the harvester... ");
        OAIHarvester harvester = null;
        try {
            Collection collection = resolveCollection(collectionID);
            HarvestedCollection hc = harvestedCollectionService.find(context, collection);
            harvester = new OAIHarvester(context, collection, hc);
            handler.logInfo("success. ");
        } catch (HarvestingException hex) {
            handler.logError("failed. ");
            handler.handleException(hex);
            throw new IllegalStateException("Unable to harvest", hex);
        } catch (SQLException se) {
            handler.logError("failed. ");
            handler.handleException(se);
            throw new IllegalStateException("Unable to access database", se);
        }

        try {
            // Harvest will not work for an anonymous user
            EPerson eperson = ePersonService.findByEmail(context, email);
            handler.logInfo("Harvest started... ");
            context.setCurrentUser(eperson);
            harvester.runHarvest();
            context.complete();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to run harvester", e);
        } catch (AuthorizeException e) {
            throw new IllegalStateException("Failed to run harvester", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to run harvester", e);
        }

        handler.logInfo("Harvest complete. ");
    }

    /**
     * Resets harvest_status and harvest_start_time flags for all collections that have a row in the
     * harvested_collections table
     */
    private void resetHarvesting() {
        handler.logInfo("Resetting harvest status flag on all collections... ");

        try {
            List<HarvestedCollection> harvestedCollections = harvestedCollectionService.findAll(context);
            for (HarvestedCollection harvestedCollection : harvestedCollections) {
                //hc.setHarvestResult(null,"");
                harvestedCollection.setHarvestStartTime(null);
                harvestedCollection.setHarvestStatus(HarvestedCollection.STATUS_READY);
                harvestedCollectionService.update(context, harvestedCollection);
            }
            handler.logInfo("success. ");
        } catch (Exception ex) {
            handler.logError("failed. ");
            handler.handleException(ex);
        }
    }

    /**
     * Starts up the harvest scheduler. Terminating this process will stop the scheduler.
     */
    private void startHarvester() {
        try {
            handler.logInfo("Starting harvest loop... ");
            HarvestServiceFactory.getInstance().getHarvestSchedulingService().startNewScheduler();
            handler.logInfo("running. ");
        } catch (Exception ex) {
            handler.handleException(ex);
        }
    }

    /**
     * See if the responder is alive and working.
     *
     * @param server         address of the responder's host.
     * @param set            name of an item set.
     * @param metadataFormat local prefix name, or null for "dc".
     */
    private void pingResponder(String server, String set, String metadataFormat) {
        List<String> errors;

        handler.logInfo("Testing basic PMH access:  ");
        errors = harvestedCollectionService.verifyOAIharvester(server, set,
                                                               (null != metadataFormat) ? metadataFormat : "dc", false);
        if (errors.isEmpty()) {
            handler.logInfo("OK");
        } else {
            for (String error : errors) {
                handler.logError(error);
            }
        }

        handler.logInfo("Testing ORE support:  ");
        errors = harvestedCollectionService.verifyOAIharvester(server, set,
                                                               (null != metadataFormat) ? metadataFormat : "dc", true);
        if (errors.isEmpty()) {
            handler.logInfo("OK");
        } else {
            for (String error : errors) {
                handler.logError(error);
            }
        }
    }

    public void setup() throws ParseException {
        if (commandLine.hasOption('h')) {
            help = true;

        }

        if (commandLine.hasOption('s')) {
            command = "config";
        }
        if (commandLine.hasOption('p')) {
            command = "purge";
        }
        if (commandLine.hasOption('r')) {
            command = "run";
        }
        if (commandLine.hasOption('g')) {
            command = "ping";
        }
        if (commandLine.hasOption('o')) {
            command = "runOnce";
        }
        if (commandLine.hasOption('S')) {
            command = "start";
        }
        if (commandLine.hasOption('R')) {
            command = "reset";
        }
        if (commandLine.hasOption('P')) {
            command = "purgeAll";
        }


        if (commandLine.hasOption('e')) {
            eperson = commandLine.getOptionValue('e');
        }
        if (commandLine.hasOption('c')) {
            collection = commandLine.getOptionValue('c');
        }
        if (commandLine.hasOption('t')) {
            harvestType = Integer.parseInt(commandLine.getOptionValue('t'));
        } else {
            harvestType = 0;
        }
        if (commandLine.hasOption('a')) {
            oaiSource = commandLine.getOptionValue('a');
        }
        if (commandLine.hasOption('i')) {
            oaiSetID = commandLine.getOptionValue('i');
        }
        if (commandLine.hasOption('m')) {
            metadataKey = commandLine.getOptionValue('m');
        }

    }
}
