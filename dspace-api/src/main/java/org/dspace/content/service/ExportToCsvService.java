package org.dspace.content.service;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.ExportToCsv;
import org.dspace.core.Context;

public interface ExportToCsvService {

    public ExportToCsv create(Context context, DSpaceObject dSpaceObject)
        throws SQLException, AuthorizeException, ParseException;

    public List<ExportToCsv> findAll(Context context) throws SQLException;

    public List<ExportToCsv> findAllByStatus(Context context, String status) throws SQLException;

    ExportToCsv findByDsoAndDate(Context context, DSpaceObject dSpaceObject, Date date) throws SQLException;

    List<ExportToCsv> findAllByStatusAndDso(Context context, DSpaceObject dSpaceObject, String status)
        throws SQLException;

    List<ExportToCsv> findAllByDso(Context context, DSpaceObject dSpaceObject)
        throws SQLException;

    public void update(Context context, ExportToCsv exportToCsv) throws SQLException, AuthorizeException;

    public void delete(Context context, ExportToCsv exportToCsv) throws SQLException, AuthorizeException;

    public void performExport(Context context, UUID uuid, Date exportToCsvDate);

    public void deleteAttachedExportToCsv(Context context, DSpaceObject dSpaceObject)
        throws SQLException, AuthorizeException,
        IOException;
}