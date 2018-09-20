package org.dspace.content;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.ExportToCsvDAO;
import org.dspace.content.service.ExportToCsvService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

public class ExportToCsvServiceImpl implements ExportToCsvService {

    @Autowired(required = true)
    protected AuthorizeService authorizeService;

    @Autowired(required = true)
    protected ExportToCsvDAO exportToCsvDAO;

    public ExportToCsv create(Context context, ExportToCsv exportToCsv) throws SQLException, AuthorizeException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                "Only administrators can modify relationship");
        }
        return exportToCsvDAO.create(context, exportToCsv);
    }

    public ExportToCsv create(Context context) throws SQLException, AuthorizeException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                "Only administrators can modify relationship");
        }
        return exportToCsvDAO.create(context, new ExportToCsv());
    }

    public ExportToCsv find(Context context, int id) throws SQLException {
        return exportToCsvDAO.findByID(context, ExportToCsv.class, id);
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
}