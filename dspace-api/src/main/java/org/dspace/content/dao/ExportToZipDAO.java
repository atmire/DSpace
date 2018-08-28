package org.dspace.content.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.dspace.content.Collection;
import org.dspace.content.DSpaceObject;
import org.dspace.content.ExportToZip;
import org.dspace.core.Context;
import org.dspace.core.GenericDAO;

public interface ExportToZipDAO extends GenericDAO<ExportToZip> {

    List<ExportToZip> findAllByStatus(Context context, Class clazz, String status) throws SQLException;

    ExportToZip findByDsoAndDate(Context context, Class clazz, DSpaceObject dSpaceObject, Date date)
        throws SQLException;

    List<ExportToZip> findAllByStatusAndDso(Context context, Class<ExportToZip> clazz, DSpaceObject dSpaceObject, String status)

        throws SQLException;
}
