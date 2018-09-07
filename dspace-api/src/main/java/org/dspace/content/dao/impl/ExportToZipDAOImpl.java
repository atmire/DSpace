package org.dspace.content.dao.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.dspace.content.DSpaceObject;
import org.dspace.content.ExportToZip;
import org.dspace.content.ExportToZip_;
import org.dspace.content.dao.ExportToZipDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

public class ExportToZipDAOImpl extends AbstractHibernateDAO<ExportToZip> implements ExportToZipDAO {

    public List<ExportToZip> findAllByStatus(Context context, Class clazz, String status) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ExportToZip.class);
        Root<ExportToZip> exportToZipRoot = criteriaQuery.from(ExportToZip.class);
        criteriaQuery.select(exportToZipRoot);
        criteriaQuery.where(
            criteriaBuilder.equal(exportToZipRoot.get(ExportToZip_.status), status));
        return list(context, criteriaQuery, false, ExportToZip.class, -1, -1);

    }

    public ExportToZip findByDsoAndDate(Context context, Class clazz, DSpaceObject dSpaceObject,
                                        Date date)
        throws SQLException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ExportToZip.class);
        Root<ExportToZip> exportToZipRoot = criteriaQuery.from(ExportToZip.class);
        criteriaQuery.select(exportToZipRoot);
        criteriaQuery.where(criteriaBuilder.and(
            criteriaBuilder.equal(exportToZipRoot.get(ExportToZip_.dso), dSpaceObject),
            criteriaBuilder.equal(exportToZipRoot.get(ExportToZip_.date), date)));
        return uniqueResult(context, criteriaQuery, false, ExportToZip.class, -1, -1);


    }

    public List<ExportToZip> findAllByStatusAndDso(Context context, Class<ExportToZip> clazz,
                                                   DSpaceObject dSpaceObject, String status) throws SQLException {

        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ExportToZip.class);
        Root<ExportToZip> exportToZipRoot = criteriaQuery.from(ExportToZip.class);
        criteriaQuery.select(exportToZipRoot);
        criteriaQuery.where(criteriaBuilder.and(
            criteriaBuilder.equal(exportToZipRoot.get(ExportToZip_.status), status),
            criteriaBuilder.equal(exportToZipRoot.get(ExportToZip_.dso), dSpaceObject)));
        return list(context, criteriaQuery, false, ExportToZip.class, -1, -1);

    }

    public List<ExportToZip> findAllByDso(Context context, Class<ExportToZip> clazz,
                                                   DSpaceObject dSpaceObject) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ExportToZip.class);
        Root<ExportToZip> exportToZipRoot = criteriaQuery.from(ExportToZip.class);
        criteriaQuery.select(exportToZipRoot);
        criteriaQuery.where(
            criteriaBuilder.equal(exportToZipRoot.get(ExportToZip_.dso), dSpaceObject));
        return list(context, criteriaQuery, false, ExportToZip.class, -1, -1);
    }
}
