package org.dspace.content.dao.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.dspace.content.DSpaceObject;
import org.dspace.content.ExportToZip;
import org.dspace.content.dao.ExportToZipDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public class ExportToZipDAOImpl extends AbstractHibernateDAO<ExportToZip> implements ExportToZipDAO {

    public List<ExportToZip> findAllByStatus(Context context, Class clazz, String status) throws SQLException {
        Criteria criteria = createCriteria(context, clazz);
        criteria.add(Restrictions.and(
            Restrictions.eq("status", status)
        ));

        return list(criteria);
    }

    public ExportToZip findByDsoAndDate(Context context, Class clazz, DSpaceObject dSpaceObject,
                                        Date date)
        throws SQLException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Criteria criteria = createCriteria(context, clazz);
        criteria.add(Restrictions.and(
            Restrictions.eq("dso", dSpaceObject),
            Restrictions.eq("date", date)
        ));

        return uniqueResult(criteria);
    }

    public List<ExportToZip> findAllByStatusAndDso(Context context, Class<ExportToZip> clazz,
                                                          DSpaceObject dSpaceObject, String status) throws SQLException {
        Criteria criteria = createCriteria(context, clazz);
        criteria.add(Restrictions.and(
            Restrictions.eq("status", status),
            Restrictions.eq("dso", dSpaceObject)
        ));

        return list(criteria);
    }

    public List<ExportToZip> findAllByDso(Context context, Class<ExportToZip> clazz,
                                                   DSpaceObject dSpaceObject) throws SQLException {
        Criteria criteria = createCriteria(context, clazz);
        criteria.add(Restrictions.eq("dso", dSpaceObject));

        return list(criteria);
    }
}
