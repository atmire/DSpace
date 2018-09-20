package org.dspace.content.dao.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.dspace.content.DSpaceObject;
import org.dspace.content.ExportToCsv;
import org.dspace.content.ExportToCsv_;
import org.dspace.content.dao.ExportToCsvDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

public class ExportToCsvDAOImpl extends AbstractHibernateDAO<ExportToCsv> implements ExportToCsvDAO {

    public List<ExportToCsv> findAllByStatus(Context context, Class clazz, String status) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ExportToCsv.class);
        Root<ExportToCsv> exportToCsvRoot = criteriaQuery.from(ExportToCsv.class);
        criteriaQuery.select(exportToCsvRoot);
        criteriaQuery.where(
            criteriaBuilder.equal(exportToCsvRoot.get(ExportToCsv_.status), status));
        return list(context, criteriaQuery, false, ExportToCsv.class, -1, -1);

    }

    public ExportToCsv findByDsoAndDate(Context context, Class clazz, DSpaceObject dSpaceObject,
                                        Date date)
        throws SQLException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ExportToCsv.class);
        Root<ExportToCsv> exportToCsvRoot = criteriaQuery.from(ExportToCsv.class);
        criteriaQuery.select(exportToCsvRoot);
        criteriaQuery.where(criteriaBuilder.and(
            criteriaBuilder.equal(exportToCsvRoot.get(ExportToCsv_.dso), dSpaceObject),
            criteriaBuilder.equal(exportToCsvRoot.get(ExportToCsv_.date), date)));
        return uniqueResult(context, criteriaQuery, false, ExportToCsv.class, -1, -1);


    }

    public List<ExportToCsv> findAllByStatusAndDso(Context context, Class<ExportToCsv> clazz,
                                                   DSpaceObject dSpaceObject, String status) throws SQLException {

        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ExportToCsv.class);
        Root<ExportToCsv> exportToCsvRoot = criteriaQuery.from(ExportToCsv.class);
        criteriaQuery.select(exportToCsvRoot);
        criteriaQuery.where(criteriaBuilder.and(
            criteriaBuilder.equal(exportToCsvRoot.get(ExportToCsv_.status), status),
            criteriaBuilder.equal(exportToCsvRoot.get(ExportToCsv_.dso), dSpaceObject)));
        return list(context, criteriaQuery, false, ExportToCsv.class, -1, -1);

    }

    public List<ExportToCsv> findAllByDso(Context context, Class<ExportToCsv> clazz,
                                          DSpaceObject dSpaceObject) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ExportToCsv.class);
        Root<ExportToCsv> exportToCsvRoot = criteriaQuery.from(ExportToCsv.class);
        criteriaQuery.select(exportToCsvRoot);
        criteriaQuery.where(
            criteriaBuilder.equal(exportToCsvRoot.get(ExportToCsv_.dso), dSpaceObject));
        return list(context, criteriaQuery, false, ExportToCsv.class, -1, -1);
    }
}