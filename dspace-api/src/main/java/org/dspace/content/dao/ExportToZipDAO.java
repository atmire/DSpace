package org.dspace.content.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.dspace.content.Collection;
import org.dspace.content.DCDate;
import org.dspace.content.ExportToZip;
import org.dspace.core.Context;
import org.dspace.core.GenericDAO;

public interface ExportToZipDAO extends GenericDAO<ExportToZip> {

    List<ExportToZip> findAllByStatus(Context context, Class clazz, String status) throws SQLException;

    ExportToZip findByCollectionAndDate(Context context, Class clazz, Collection collection, Date date) throws SQLException;
}
