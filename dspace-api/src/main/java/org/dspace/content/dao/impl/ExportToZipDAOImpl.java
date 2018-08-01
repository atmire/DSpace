package org.dspace.content.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.dspace.content.BitstreamFormat;
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
}
