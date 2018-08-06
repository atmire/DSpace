package org.dspace.export;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.dspace.app.itemexport.factory.ItemExportServiceFactory;
import org.dspace.content.Collection;
import org.dspace.content.ExportToZip;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ExportToZipService;
import org.dspace.core.Context;

public class ExportToZipTask implements Runnable {

    private static Logger log = Logger.getLogger(ExportToZipTask.class);

    private Collection collection;
    private Context context;
    private Integer exportToZipId;
    ExportToZipService exportToZipService = ContentServiceFactory.getInstance().getExportToZipService();

    public ExportToZipTask(Context context, Collection collection, Integer exportToZipId) {
        this.collection = collection;
        this.context = context;
        this.exportToZipId = exportToZipId;
    }

    public void run() {
        context.turnOffAuthorisationSystem();
        try {
            UUID bitstreamUuid = ItemExportServiceFactory.getInstance()
                                                         .getItemExportService()
                                                         .createDownloadableExport(collection, context, false, true);
            ExportToZip exportToZip = exportToZipService.find(context, exportToZipId);
            exportToZip.setBitstreamId(bitstreamUuid);
            exportToZip.setStatus("Completed");
            exportToZipService.update(context, exportToZip);
            context.commit();
            context.complete();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
