/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.pages.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;
import org.dspace.pages.Page;
import org.dspace.pages.Page_;
import org.dspace.pages.dao.PageDao;

/**
 * implementation of the Database Access Object interface class for the Page object.
 * This class is responsible for all database calls for the Page object and is autowired by spring
 * This class should never be accessed directly.
 */
public class PageDaoImpl extends AbstractHibernateDAO<Page> implements PageDao {

    @Override
    public Page findByUuid(Context context, UUID uuid) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, Page.class);
        Root<Page> pageRoot = criteriaQuery.from(Page.class);
        criteriaQuery.select(pageRoot);
        criteriaQuery.where(criteriaBuilder.equal(pageRoot.get(Page_.id), uuid));
        return uniqueResult(context, criteriaQuery, false, Page.class, -1, -1);
    }

    @Override
    public List<Page> findByName(Context context, String name) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, Page.class);
        Root<Page> pageRoot = criteriaQuery.from(Page.class);
        criteriaQuery.select(pageRoot);
        criteriaQuery.where(criteriaBuilder.equal(pageRoot.get(Page_.name), name));
        return list(context, criteriaQuery, false, Page.class, -1, -1);
    }

    @Override
    public Page findByNameAndLanguage(Context context, String name, String language) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, Page.class);
        Root<Page> pageRoot = criteriaQuery.from(Page.class);
        criteriaQuery.select(pageRoot);
        criteriaQuery.where(
            criteriaBuilder.and(criteriaBuilder.equal(pageRoot.get(Page_.name), name)),
                                criteriaBuilder.equal(pageRoot.get(Page_.language), language));
        return uniqueResult(context, criteriaQuery, false, Page.class, -1, -1);    }
}
