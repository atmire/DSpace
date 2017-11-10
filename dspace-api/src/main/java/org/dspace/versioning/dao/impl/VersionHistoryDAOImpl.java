/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.versioning.dao.impl;

import org.dspace.content.BitstreamFormat_;
import org.dspace.content.Item;
import org.dspace.content.Item_;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.versioning.VersionHistory;
import org.dspace.versioning.Version_;
import org.dspace.versioning.dao.VersionHistoryDAO;
import org.dspace.workflowbasic.BasicWorkflowItem;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.dspace.versioning.Version;
import org.hibernate.criterion.Order;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

/**
 * Hibernate implementation of the Database Access Object interface class for the VersionHistory object.
 * This class is responsible for all database calls for the VersionHistory object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author Fabio Bolognesi (fabio at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author kevinvandevelde at atmire.com
 */
public class VersionHistoryDAOImpl extends AbstractHibernateDAO<VersionHistory> implements VersionHistoryDAO
{
    protected VersionHistoryDAOImpl()
    {
        super();
    }

    @Override
    public VersionHistory findByItem(Context context, Item item) throws SQLException {

        //TODO RAF CHECK
//        Criteria criteria = createCriteria(context, VersionHistory.class);
//        criteria.createAlias("versions", "v");
//        criteria.add(Restrictions.eq("v.item", item));
//        criteria.addOrder(Order.desc("v.versionNumber"));
//        return singleResult(criteria);

        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, VersionHistory.class);
        Root<VersionHistory> versionHistoryRoot = criteriaQuery.from(VersionHistory.class);
        Join<VersionHistory, Version> join = versionHistoryRoot.join("versions");
        criteriaQuery.select(versionHistoryRoot);
        criteriaQuery.where(criteriaBuilder.equal(join.get(Version_.item), item));

        List<javax.persistence.criteria.Order> orderList = new LinkedList<>();
        orderList.add(criteriaBuilder.desc(join.get(Version_.versionNumber)));
        criteriaQuery.orderBy(orderList);

        return singleResult(context, criteriaQuery);
    }
}
