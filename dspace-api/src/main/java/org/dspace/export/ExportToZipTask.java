package org.dspace.export;

import java.sql.SQLException;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dspace.app.itemexport.factory.ItemExportServiceFactory;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.DCDate;
import org.dspace.content.ExportToZip;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ExportToZipService;
import org.dspace.core.Context;

public class ExportToZipTask implements Runnable {

    private static Logger log = Logger.getLogger(ExportToZipTask.class);

    private UUID collectionUuid;
    private DCDate currentDate;
    private Context context;
    ExportToZipService exportToZipService = ContentServiceFactory.getInstance().getExportToZipService();

    public ExportToZipTask(UUID collectionUuid, DCDate currentDate) {
        this.collectionUuid = collectionUuid;
        this.currentDate = currentDate;
        this.context = new Context();
    }

    public void run() {
        context.turnOffAuthorisationSystem();
        ExportToZip exportToZip = new ExportToZip();
        try {
            Collection collection = ContentServiceFactory.getInstance().getCollectionService()
                                                         .find(context, collectionUuid);
            exportToZip
                .setDso(collection);
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            exportToZip.setDate(currentDate.toDate());
            exportToZip.setStatus("In Progress");
            exportToZipService.create(context, exportToZip);
            exportToZipService.update(context, exportToZip);
            ItemExportServiceFactory.getInstance()
                                    .getItemExportService().createDownloadableExport(collection, context, false);

            context.complete();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
