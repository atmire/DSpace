package org.dspace.content;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.dspace.app.bulkedit.DSpaceCSV;
import org.dspace.app.bulkedit.MetadataExport;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.authorize.service.ResourcePolicyService;
import org.dspace.content.dao.ExportToCsvDAO;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.content.service.ExportToCsvService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.service.GroupService;
import org.dspace.export.ExportStatus;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;

public class ExportToCsvServiceImpl implements ExportToCsvService {

    private static final Logger log = Logger.getLogger(ExportToCsvServiceImpl.class);

    @Autowired(required = true)
    protected AuthorizeService authorizeService;

    @Autowired(required = true)
    protected ExportToCsvDAO exportToCsvDAO;

    @Autowired(required = true)
    protected BitstreamService bitstreamService;

    @Autowired
    protected ItemService itemService;

    @Autowired
    protected List<DSpaceObjectService<? extends DSpaceObject>> dSpaceObjectServices;

    @Autowired
    protected ResourcePolicyService resourcePolicyService;

    @Autowired
    protected GroupService groupService;

    public ExportToCsv create(Context context, DSpaceObject dSpaceObject)
        throws SQLException, AuthorizeException, ParseException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                "Only administrators can modify relationship");
        }
        ExportToCsv exportToCsv = new ExportToCsv();
        exportToCsv.setDso(dSpaceObject);

        Date date = new DateTime(DateTimeZone.UTC).toDate();
        exportToCsv.setDate(date);
        exportToCsv.setStatus(ExportStatus.IN_PROGRESS);
        return exportToCsvDAO.create(context, exportToCsv);

    }

    public void update(Context context, ExportToCsv exportToCsv) throws SQLException, AuthorizeException {
        update(context, Collections.singletonList(exportToCsv));
    }

    public void update(Context context, List<ExportToCsv> exportToCsvList) throws SQLException, AuthorizeException {
        if (CollectionUtils.isNotEmpty(exportToCsvList)) {
            // Check authorisation - only administrators can change formats
            if (!authorizeService.isAdmin(context)) {
                throw new AuthorizeException(
                    "Only administrators can modify relationship");
            }

            for (ExportToCsv exportToCsv : exportToCsvList) {
                exportToCsvDAO.save(context, exportToCsv);
            }
        }
    }

    public void delete(Context context, ExportToCsv exportToCsv) throws SQLException, AuthorizeException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                "Only administrators can delete relationship");
        }
        exportToCsvDAO.delete(context, exportToCsv);
        Bitstream exportToCsvBitstream = bitstreamService.find(context, exportToCsv.getBitstreamId());
        try {
            bitstreamService.delete(context, exportToCsvBitstream);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        bitstreamService.update(context, exportToCsvBitstream);
    }

    public List<ExportToCsv> findAll(Context context) throws SQLException {
        return exportToCsvDAO.findAll(context, ExportToCsv.class);
    }

    public List<ExportToCsv> findAllByStatus(Context context, String status) throws SQLException {
        return exportToCsvDAO.findAllByStatus(context, ExportToCsv.class, status);
    }

    public ExportToCsv findByDsoAndDate(Context context, DSpaceObject dSpaceObject, Date date) throws SQLException {
        return exportToCsvDAO.findByDsoAndDate(context, ExportToCsv.class, dSpaceObject, date);
    }

    public List<ExportToCsv> findAllByStatusAndDso(Context context, DSpaceObject dSpaceObject, String status)
        throws SQLException {
        return exportToCsvDAO.findAllByStatusAndDso(context, ExportToCsv.class, dSpaceObject, status);
    }

    public List<ExportToCsv> findAllByDso(Context context, DSpaceObject dSpaceObject)
        throws SQLException {
        return exportToCsvDAO.findAllByDso(context, ExportToCsv.class, dSpaceObject);
    }

    public void performExport(Context context, UUID targetUuid, Date exportToCsvDate) {
        MetadataExport exporter = null;
        InputStream csvInputStream = null;
        DSpaceObject dSpaceObject = null;
        try {
            for (DSpaceObjectService dSpaceObjectService : dSpaceObjectServices) {
                dSpaceObject = dSpaceObjectService.find(context, targetUuid);
                if (dSpaceObject != null) {
                    break;
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
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
            handleEPersonReadRights(context, exportToCsvBitstream);
            ExportToCsv exportToCsv = findByDsoAndDate(context, dSpaceObject, exportToCsvDate);
            if (exportToCsvBitstream == null) {
                delete(context, exportToCsv);
                log.warn(
                    "Deleted ExportToCsv entry with date: " + exportToCsvDate + "and dsoId: " + dSpaceObject.getID()
                        + ", This record was corrupt and had no bitstreamUUID");
            } else {
                exportToCsv.setBitstreamId(exportToCsvBitstream.getID());
                exportToCsv.setStatus(ExportStatus.COMPLETED);
            }
            update(context, exportToCsv);
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


    private void handleEPersonReadRights(Context context, Bitstream exportToCsvBitstream)
        throws SQLException, AuthorizeException {
        ResourcePolicy resourcePolicy = resourcePolicyService.create(context);
        resourcePolicy.setdSpaceObject(exportToCsvBitstream);
        resourcePolicy.setAction(Constants.READ);
        resourcePolicy.setEPerson(context.getCurrentUser());
        resourcePolicyService.update(context, resourcePolicy);
    }

    public void deleteAttachedExportToCsv(Context context, DSpaceObject dSpaceObject)
        throws SQLException, AuthorizeException, IOException {
        List<ExportToCsv> list = findAllByDso(context, dSpaceObject);
        for (ExportToCsv exportToCsv : list) {
            bitstreamService.delete(context, bitstreamService.find(context, exportToCsv.getBitstreamId()));
            delete(context, exportToCsv);
        }
    }
}