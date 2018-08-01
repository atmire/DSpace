package org.dspace.content.dao;

import java.sql.SQLException;
import java.util.List;

import org.dspace.content.ExportToZip;
import org.dspace.core.Context;
import org.dspace.core.GenericDAO;

public interface ExportToZipDAO extends GenericDAO<ExportToZip> {

    public List<ExportToZip> findAllByStatus(Context context, Class clazz, String status) throws SQLException;
}
