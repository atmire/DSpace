package org.dspace.export;

import java.util.Date;
import java.util.UUID;

import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ExportToCsvService;
import org.dspace.core.Context;

public class ExportToCsvTask implements Runnable {

    private UUID uuid;
    private Date exportToCsvDate;
    ExportToCsvService exportToCsvService = ContentServiceFactory.getInstance().getExportToCsvService();

    public ExportToCsvTask(UUID uuid, Date date) {
        this.uuid = uuid;
        this.exportToCsvDate = date;
    }

    public void run() {
        Context context = new Context();
        context.turnOffAuthorisationSystem();
        try {
            exportToCsvService.performExport(context, uuid, exportToCsvDate);
        } finally {
            if (context.isValid()) {
                context.close();
            }
        }

    }

}