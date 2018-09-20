package org.dspace.export;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dspace.app.bulkedit.DSpaceCSV;
import org.dspace.app.bulkedit.MetadataExport;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.ExportToCsv;
import org.dspace.content.Item;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.ExportToCsvService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

public class ExportToCsvTask implements Runnable {

    private static Logger log = Logger.getLogger(ExportToCsvTask.class);

    private DSpaceObject dSpaceObject;
    private EPerson ePerson;
    private Integer exportToCsvId;
    ExportToCsvService exportToCsvService = ContentServiceFactory.getInstance().getExportToCsvService();
    ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
    MetadataExport exporter = null;

    public ExportToCsvTask(EPerson currentUser, DSpaceObject dSpaceObject, Integer exportToCsvId) {
        this.dSpaceObject = dSpaceObject;
        this.ePerson = currentUser;
        this.exportToCsvId = exportToCsvId;
    }

    public void run() {
        Context context = new Context();
        context.turnOffAuthorisationSystem();

        try {
            java.util.List<Item> itemmd = new ArrayList<>();
            if (dSpaceObject.getType() == Constants.ITEM) {
                itemmd.add(itemService.find(context, dSpaceObject.getID()));
                exporter = new MetadataExport(context, itemmd.iterator(), false);
            } else if (dSpaceObject.getType() == Constants.COLLECTION) {
                Collection collection = (Collection) dSpaceObject;
                Iterator<Item> toExport = itemService.findByCollection(context, collection);
                exporter = new MetadataExport(context, toExport, false);
            } else if (dSpaceObject.getType() == Constants.COMMUNITY) {
                exporter = new MetadataExport(context, (Community) dSpaceObject, false);
            }

            DSpaceCSV csv = exporter.export();
            String csvString = csv.toString();
            InputStream csvInputStream = new ByteArrayInputStream(csvString.getBytes(StandardCharsets.UTF_8) );
            Bitstream exportToCsvBitstream = bitstreamService.create(context, csvInputStream);
            ExportToCsv exportToCsv = exportToCsvService.find(context, exportToCsvId);
            if (exportToCsvBitstream == null) {
                exportToCsvService.delete(context, exportToCsv);
                log.info("Deleted ExportToCsv entry with UUID: " + exportToCsvId
                             + ", This record was corrupt and had no bitstreamUUID");
            } else {
                exportToCsv.setBitstreamId(exportToCsvBitstream.getID());
                exportToCsv.setStatus("completed");
            }
            exportToCsvService.update(context, exportToCsv);
            context.commit();
            context.complete();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


    }
}