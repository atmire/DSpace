package org.dspace.content;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.dspace.app.itemexport.factory.ItemExportServiceFactory;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.ExportToZipDAO;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.content.service.ExportToZipService;
import org.dspace.core.Context;
import org.dspace.export.ExportStatus;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;

public class ExportToZipServiceImpl implements ExportToZipService {

    private static final Logger log = Logger.getLogger(ExportToZipServiceImpl.class);

    @Autowired
    protected AuthorizeService authorizeService;

    @Autowired
    protected ExportToZipDAO exportToZipDAO;

    @Autowired
    protected BitstreamService bitstreamService;

    @Autowired
    protected List<DSpaceObjectService<? extends DSpaceObject>> dSpaceObjectServices;

    public ExportToZip create(Context context, DSpaceObject dSpaceObject) throws SQLException, AuthorizeException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                "Only administrators can modify relationship");
        }

        ExportToZip exportToZip = new ExportToZip();
        exportToZip.setDso(dSpaceObject);

        Date date = new DateTime(DateTimeZone.UTC).toDate();
        exportToZip.setDate(date);
        exportToZip.setStatus(ExportStatus.IN_PROGRESS);
        return exportToZipDAO.create(context, exportToZip);
    }

    public void update(Context context, ExportToZip exportToZip) throws SQLException, AuthorizeException {
        update(context, Collections.singletonList(exportToZip));
    }

    public void update(Context context, List<ExportToZip> exportToZipList) throws SQLException, AuthorizeException {
        if (CollectionUtils.isNotEmpty(exportToZipList)) {
            // Check authorisation - only administrators can change formats
            if (!authorizeService.isAdmin(context)) {
                throw new AuthorizeException(
                    "Only administrators can modify relationship");
            }

            for (ExportToZip exportToZip : exportToZipList) {
                exportToZipDAO.save(context, exportToZip);
            }
        }
    }

    public void delete(Context context, ExportToZip exportToZip) throws SQLException, AuthorizeException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                "Only administrators can delete relationship");
        }
        exportToZipDAO.delete(context, exportToZip);
        Bitstream exportToZipBitstream = bitstreamService.find(context, exportToZip.getBitstreamId());
        try {
            bitstreamService.delete(context, exportToZipBitstream);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        bitstreamService.update(context, exportToZipBitstream);
    }

    public List<ExportToZip> findAll(Context context) throws SQLException {
        return exportToZipDAO.findAll(context, ExportToZip.class);
    }

    public List<ExportToZip> findAllByStatus(Context context, String status) throws SQLException {
        return exportToZipDAO.findAllByStatus(context, ExportToZip.class, status);
    }

    public ExportToZip findByDsoAndDate(Context context, DSpaceObject dSpaceObject, Date date) throws SQLException {
        return exportToZipDAO.findByDsoAndDate(context, ExportToZip.class, dSpaceObject, date);
    }

    public List<ExportToZip> findAllByStatusAndDso(Context context, DSpaceObject dSpaceObject, String status)
        throws SQLException {
        return exportToZipDAO.findAllByStatusAndDso(context, ExportToZip.class, dSpaceObject, status);
    }

    public List<ExportToZip> findAllByDso(Context context, DSpaceObject dSpaceObject)
        throws SQLException {
        return exportToZipDAO.findAllByDso(context, ExportToZip.class, dSpaceObject);
    }

    public void create(Context context, UUID uuid, Date exportToZipDate) {
        DSpaceObject dSpaceObject = null;
        try {
            for (DSpaceObjectService dSpaceObjectService : dSpaceObjectServices) {
                dSpaceObject = dSpaceObjectService.find(context, uuid);
                if (dSpaceObject != null) {
                    break;
                }
            }
            UUID bitstreamUuid = ItemExportServiceFactory.getInstance()
                                                         .getItemExportService()
                                                         .createDownloadableExport(dSpaceObject, context, false, true);
            ExportToZip exportToZip = findByDsoAndDate(context, dSpaceObject, exportToZipDate);

            if (bitstreamUuid == null) {
                delete(context, exportToZip);
                log.info("Deleted ExportToZip entry with date: " + exportToZipDate + " and dsoId: " + uuid
                             + ", This record was corrupt and had no bitstreamUUID");
            } else {
                exportToZip.setBitstreamId(bitstreamUuid);
                exportToZip.setStatus(ExportStatus.COMPLETED);
            }
            update(context, exportToZip);
            context.commit();
            context.complete();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            context.close();
        }
    }
}
