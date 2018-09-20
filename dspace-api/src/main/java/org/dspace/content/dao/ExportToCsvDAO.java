package org.dspace.content.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.dspace.content.DSpaceObject;
import org.dspace.content.ExportToCsv;
import org.dspace.core.Context;
import org.dspace.core.GenericDAO;

public interface ExportToCsvDAO extends GenericDAO<ExportToCsv> {

    List<ExportToCsv> findAllByStatus(Context context, Class clazz, String status) throws SQLException;

    ExportToCsv findByDsoAndDate(Context context, Class clazz, DSpaceObject dSpaceObject, Date date)
        throws SQLException;

    List<ExportToCsv> findAllByStatusAndDso(Context context, Class<ExportToCsv> clazz, DSpaceObject dSpaceObject, String status)

        throws SQLException;

    List<ExportToCsv> findAllByDso(Context context, Class<ExportToCsv> clazz, DSpaceObject dSpaceObject)

        throws SQLException;
}