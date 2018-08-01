package org.dspace.content.service;

import java.sql.SQLException;
import java.util.List;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.ExportToZip;
import org.dspace.core.Context;
import org.dspace.service.DSpaceCRUDService;

public interface ExportToZipService extends DSpaceCRUDService<ExportToZip> {

    public ExportToZip create(Context context, ExportToZip exportToZip) throws SQLException, AuthorizeException;
    public List<ExportToZip> findAll(Context context) throws SQLException;
    public List<ExportToZip> findAllByStatus(Context context, String status) throws SQLException;
}
