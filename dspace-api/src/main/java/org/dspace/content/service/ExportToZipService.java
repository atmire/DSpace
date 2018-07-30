package org.dspace.content.service;

import java.sql.SQLException;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.ExportToZip;
import org.dspace.core.Context;
import org.dspace.service.DSpaceCRUDService;

public interface ExportToZipService extends DSpaceCRUDService<ExportToZip> {

    public ExportToZip create(Context context, ExportToZip exportToZip) throws SQLException, AuthorizeException;
}
