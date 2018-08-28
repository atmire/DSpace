package org.dspace.content;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.ExportToZipDAO;
import org.dspace.content.service.ExportToZipService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

public class ExportToZipServiceImpl implements ExportToZipService {

    @Autowired(required = true)
    protected AuthorizeService authorizeService;

    @Autowired(required = true)
    protected ExportToZipDAO exportToZipDAO;

    public ExportToZip create(Context context, ExportToZip exportToZip) throws SQLException, AuthorizeException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                "Only administrators can modify relationship");
        }
        return exportToZipDAO.create(context, exportToZip);
    }

    public ExportToZip create(Context context) throws SQLException, AuthorizeException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                "Only administrators can modify relationship");
        }
        return exportToZipDAO.create(context, new ExportToZip());
    }

    public ExportToZip find(Context context, int id) throws SQLException {
        return exportToZipDAO.findByID(context, ExportToZip.class, id);
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
}
