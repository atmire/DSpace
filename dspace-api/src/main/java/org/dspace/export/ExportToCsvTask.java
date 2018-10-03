package org.dspace.export;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.dspace.app.bulkedit.DSpaceCSV;
import org.dspace.app.bulkedit.MetadataExport;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.factory.AuthorizeServiceFactory;
import org.dspace.authorize.service.ResourcePolicyService;
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
import org.dspace.eperson.Group;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.GroupService;

public class ExportToCsvTask implements Runnable {

    private static Logger log = Logger.getLogger(ExportToCsvTask.class);

    private DSpaceObject dSpaceObject;
    private EPerson ePerson;
    private Date exportToCsvDate;
    ExportToCsvService exportToCsvService = ContentServiceFactory.getInstance().getExportToCsvService();
    ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
    ResourcePolicyService resourcePolicyService = AuthorizeServiceFactory.getInstance().getResourcePolicyService();
    GroupService groupService = EPersonServiceFactory.getInstance().getGroupService();
    MetadataExport exporter = null;

    public ExportToCsvTask(EPerson currentUser, DSpaceObject dSpaceObject, Date date) {
        this.dSpaceObject = dSpaceObject;
        this.ePerson = currentUser;
        this.exportToCsvDate = date;
    }

    public void run() {
        Context context = new Context();
        context.turnOffAuthorisationSystem();
        InputStream csvInputStream = null;
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
            csvInputStream = new ByteArrayInputStream(csvString.getBytes(StandardCharsets.UTF_8));
            Bitstream exportToCsvBitstream = bitstreamService.create(context, csvInputStream);
            handleAdminOnlyReadRights(context, exportToCsvBitstream);
            ExportToCsv exportToCsv = exportToCsvService.findByDsoAndDate(context, dSpaceObject, exportToCsvDate);
            if (exportToCsvBitstream == null) {
                exportToCsvService.delete(context, exportToCsv);
                log.warn(
                    "Deleted ExportToCsv entry with date: " + exportToCsvDate + "and dsoId: " + dSpaceObject.getID()
                        + ", This record was corrupt and had no bitstreamUUID");
            } else {
                exportToCsv.setBitstreamId(exportToCsvBitstream.getID());
                exportToCsv.setStatus(ExportStatus.COMPLETED.getValue());
            }
            exportToCsvService.update(context, exportToCsv);
            context.commit();
            context.complete();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (csvInputStream != null) {
                try {
                    csvInputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
            context.close();
        }


    }

    private void handleAdminOnlyReadRights(Context context, Bitstream exportToCsvBitstream)
        throws SQLException, AuthorizeException {
        resourcePolicyService.removeAllPolicies(context, exportToCsvBitstream);
        ResourcePolicy resourcePolicy = resourcePolicyService.create(context);
        resourcePolicy.setdSpaceObject(exportToCsvBitstream);
        resourcePolicy.setAction(Constants.READ);
        resourcePolicy.setGroup(groupService.findByName(context, Group.ADMIN));
        resourcePolicyService.update(context, resourcePolicy);
    }
}