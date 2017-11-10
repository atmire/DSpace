/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.xmlworkflow.storedcomponents.dao.impl;

import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.PoolTask;
import org.dspace.xmlworkflow.storedcomponents.WorkflowItemRole;
import org.dspace.xmlworkflow.storedcomponents.WorkflowItemRole_;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.dao.WorkflowItemRoleDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.sql.SQLException;
import java.util.List;

/**
 * Hibernate implementation of the Database Access Object interface class for the WorkflowItemRole object.
 * This class is responsible for all database calls for the WorkflowItemRole object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class WorkflowItemRoleDAOImpl extends AbstractHibernateDAO<WorkflowItemRole> implements WorkflowItemRoleDAO
{
    protected WorkflowItemRoleDAOImpl()
    {
        super();
    }

    @Override
    public List<WorkflowItemRole> findByWorkflowItemAndRole(Context context, XmlWorkflowItem workflowItem, String role) throws SQLException {
//        Criteria criteria = createCriteria(context, WorkflowItemRole.class);
//        criteria.add(Restrictions.and(
//                        Restrictions.eq("workflowItem", workflowItem),
//                        Restrictions.eq("role", role)
//                )
//        );
//
//        return list(criteria);
//
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, WorkflowItemRole.class);
        Root<WorkflowItemRole> workflowItemRoleRoot = criteriaQuery.from(WorkflowItemRole.class);
        criteriaQuery.select(workflowItemRoleRoot);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(workflowItemRoleRoot.get(WorkflowItemRole_.workflowItem), workflowItem),
                                                criteriaBuilder.equal(workflowItemRoleRoot.get(WorkflowItemRole_.roleId), role)
                                                )
                            );
        return list(context, criteriaQuery, false, WorkflowItemRole.class, -1, -1);
    }

    @Override
    public List<WorkflowItemRole> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException {
//        Criteria criteria = createCriteria(context, WorkflowItemRole.class);
//        criteria.add(Restrictions.eq("workflowItem", workflowItem));
//
//        return list(criteria);
//
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, WorkflowItemRole.class);
        Root<WorkflowItemRole> workflowItemRoleRoot = criteriaQuery.from(WorkflowItemRole.class);
        criteriaQuery.select(workflowItemRoleRoot);
        criteriaQuery.where(criteriaBuilder.equal(workflowItemRoleRoot.get(WorkflowItemRole_.workflowItem), workflowItem));
        return list(context, criteriaQuery, false, WorkflowItemRole.class, -1, -1);
    }

    @Override
    public List<WorkflowItemRole> findByEPerson(Context context, EPerson ePerson) throws SQLException {
//        Criteria criteria = createCriteria(context, WorkflowItemRole.class);
//        criteria.add(Restrictions.eq("ePerson", ePerson));
//
//        return list(criteria);
//
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, WorkflowItemRole.class);
        Root<WorkflowItemRole> workflowItemRoleRoot = criteriaQuery.from(WorkflowItemRole.class);
        criteriaQuery.select(workflowItemRoleRoot);
        criteriaQuery.where(criteriaBuilder.equal(workflowItemRoleRoot.get(WorkflowItemRole_.ePerson), ePerson));
        return list(context, criteriaQuery, false, WorkflowItemRole.class, -1, -1);
    }
}
