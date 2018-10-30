
package org.dspace.export;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ExportToZipService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;

public class ExportToZipTask implements Runnable {

    private static final Logger log = Logger.getLogger(ExportToZipTask.class);

    private UUID uuid;
    private Date exportToZipDate;
    private UUID epersonUuid;
    ExportToZipService exportToZipService = ContentServiceFactory.getInstance().getExportToZipService();

    public ExportToZipTask(UUID uuid, Date date, UUID epersonUuid) {
        this.uuid = uuid;
        this.exportToZipDate = date;
        this.epersonUuid = epersonUuid;
    }

    public void run() {
        Context context = new Context();
        EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();
        EPerson ePerson = null;
        try {
            ePerson = ePersonService.find(context, epersonUuid);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        if (ePerson != null) {
            context.setCurrentUser(ePerson);
        }

        context.turnOffAuthorisationSystem();
        exportToZipService.create(context, uuid, exportToZipDate);
    }

}