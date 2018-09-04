package org.dspace.export;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.dspace.app.itemexport.factory.ItemExportServiceFactory;
import org.dspace.content.DSpaceObject;
import org.dspace.content.ExportToZip;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ExportToZipService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;

public class ExportToZipTask implements Runnable {

    private static Logger log = Logger.getLogger(ExportToZipTask.class);

    private DSpaceObject dSpaceObject;
    private EPerson ePerson;
    private Integer exportToZipId;
    ExportToZipService exportToZipService = ContentServiceFactory.getInstance().getExportToZipService();

    public ExportToZipTask(EPerson currentUser, DSpaceObject dSpaceObject, Integer exportToZipId) {
        this.dSpaceObject = dSpaceObject;
        this.ePerson = currentUser;
        this.exportToZipId = exportToZipId;
    }

    public void run() {
        Context context = new Context();
        try {
            ePerson = context.reloadEntity(ePerson);
            dSpaceObject = context.reloadEntity(dSpaceObject);
            context.setCurrentUser(ePerson);
            UUID bitstreamUuid = ItemExportServiceFactory.getInstance()
                                                         .getItemExportService()
                                                         .createDownloadableExport(dSpaceObject, context, false, true);
            ExportToZip exportToZip = exportToZipService.find(context, exportToZipId);
            if (bitstreamUuid == null) {
               exportToZipService.delete(context, exportToZip);
               log.info("Deleted ExportToZip entry with UUID: " + exportToZipId
                            + ", This record was corrupt and had no bitstreamUUID");
            } else {
                exportToZip.setBitstreamId(bitstreamUuid);
                exportToZip.setStatus("completed");
            }
            exportToZipService.update(context, exportToZip);
            context.commit();
            context.complete();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
