package org.dspace.content.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.ExportToCsv;
import org.dspace.core.Context;
import org.dspace.service.DSpaceCRUDService;

public interface ExportToCsvService extends DSpaceCRUDService<ExportToCsv> {

    public ExportToCsv create(Context context, ExportToCsv exportToCsv) throws SQLException, AuthorizeException;

    public List<ExportToCsv> findAll(Context context) throws SQLException;

    public List<ExportToCsv> findAllByStatus(Context context, String status) throws SQLException;

    ExportToCsv findByDsoAndDate(Context context, DSpaceObject dSpaceObject, Date date) throws SQLException;

    List<ExportToCsv> findAllByStatusAndDso(Context context, DSpaceObject dSpaceObject, String status)
        throws SQLException;

    List<ExportToCsv> findAllByDso(Context context, DSpaceObject dSpaceObject)
        throws SQLException;

}