/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.pages.dao.impl;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.dspace.content.Bitstream;
import org.dspace.content.BitstreamFormat;
import org.dspace.content.BitstreamFormat_;
import org.dspace.content.DSpaceObject;
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
    public List<Page> findByNameAndDSpaceObject(Context context, String name,
                                                DSpaceObject dSpaceObject) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, Page.class);
        Root<Page> pageRoot = criteriaQuery.from(Page.class);
        criteriaQuery.select(pageRoot);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(pageRoot.get(Page_.name), name),
                                                criteriaBuilder.equal(pageRoot.get(Page_.dSpaceObject), dSpaceObject)));
        return list(context, criteriaQuery, false, Page.class, -1, -1);
    }

    @Override
    public Page findByNameLanguageAndDSpaceObject(Context context, String name, String language,
                                                  DSpaceObject dSpaceObject) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, Page.class);
        Root<Page> pageRoot = criteriaQuery.from(Page.class);
        criteriaQuery.select(pageRoot);
        criteriaQuery.where(
            criteriaBuilder.and(criteriaBuilder.equal(pageRoot.get(Page_.name), name),
                                criteriaBuilder.equal(pageRoot.get(Page_.language), language),
                                criteriaBuilder.equal(pageRoot.get(Page_.dSpaceObject), dSpaceObject)));

        return uniqueResult(context, criteriaQuery, false, Page.class, -1, -1);
    }

    public List<Page> findByDSpaceObject(Context context, DSpaceObject dSpaceObject) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, Page.class);
        Root<Page> pageRoot = criteriaQuery.from(Page.class);
        criteriaQuery.select(pageRoot);
        criteriaQuery.where(criteriaBuilder.equal(pageRoot.get(Page_.dSpaceObject), dSpaceObject));
        return list(context, criteriaQuery, false, Page.class, -1, -1);    }

    public List<Page> findPagesByParameters(Context context, String name, String format, String language,
                                            DSpaceObject dSpaceObject) throws SQLException {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, Page.class);
        Root<Page> pageRoot = criteriaQuery.from(Page.class);
        criteriaQuery.select(pageRoot);

        List<Predicate> andPredicates = new LinkedList<>();
        if (StringUtils.isNotBlank(name)) {
            andPredicates.add(criteriaBuilder.equal(pageRoot.get(Page_.name), name));
        }
        if (StringUtils.isNotBlank(language) && !StringUtils.equals(language, "*")) {
            andPredicates.add(criteriaBuilder.equal(pageRoot.get(Page_.language), language));
        }
        if (dSpaceObject != null) {
            andPredicates.add(criteriaBuilder.equal(pageRoot.get(Page_.dSpaceObject), dSpaceObject));
        }
        if (StringUtils.isNotBlank(format)) {
            Join<Page, Bitstream> join = pageRoot.join("bitstream");
            Join<Bitstream, BitstreamFormat> secondJoin = join.join("bitstreamFormat");
            andPredicates.add(criteriaBuilder.equal(secondJoin.get(BitstreamFormat_.mimetype), format));
        }
        Predicate andPredicate = criteriaBuilder.and(andPredicates.toArray(new Predicate[] {}));
        criteriaQuery.where(andPredicate);
        return list(context, criteriaQuery, false, Page.class, -1, -1);
    }
}
