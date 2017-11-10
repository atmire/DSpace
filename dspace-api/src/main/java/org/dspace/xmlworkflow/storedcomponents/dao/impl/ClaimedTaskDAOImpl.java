/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.xmlworkflow.storedcomponents.dao.impl;

import org.dspace.content.WorkspaceItem;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.eperson.EPerson;
import org.dspace.xmlworkflow.storedcomponents.ClaimedTask;
import org.dspace.xmlworkflow.storedcomponents.ClaimedTask_;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.dao.ClaimedTaskDAO;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.sql.SQLException;
import java.util.List;

/**
 * Hibernate implementation of the Database Access Object interface class for the ClaimedTask object.
 * This class is responsible for all database calls for the ClaimedTask object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class ClaimedTaskDAOImpl extends AbstractHibernateDAO<ClaimedTask> implements ClaimedTaskDAO
{
    protected ClaimedTaskDAOImpl()
    {
        super();
    }

    @Override
    public List<ClaimedTask> findByWorkflowItem(Context context, XmlWorkflowItem workflowItem) throws SQLException {
//        Criteria criteria = createCriteria(context, ClaimedTask.class);
//        criteria.add(Restrictions.eq("workflowItem", workflowItem));
//
//        return list(criteria);
//
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ClaimedTask.class);
        Root<ClaimedTask> claimedTaskRoot = criteriaQuery.from(ClaimedTask.class);
        criteriaQuery.select(claimedTaskRoot);
        criteriaQuery.where(criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.workflowItem), workflowItem));
        return list(context, criteriaQuery, false, ClaimedTask.class, -1, -1);

    }

    @Override
    public ClaimedTask findByWorkflowItemAndEPerson(Context context, XmlWorkflowItem workflowItem, EPerson ePerson) throws SQLException {
//        Criteria criteria = createCriteria(context, ClaimedTask.class);
//        criteria.add(Restrictions.and(
//                Restrictions.eq("workflowItem", workflowItem),
//                Restrictions.eq("owner", ePerson)
//        ));
//
//        return uniqueResult(criteria);
//
//
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ClaimedTask.class);
        Root<ClaimedTask> claimedTaskRoot = criteriaQuery.from(ClaimedTask.class);
        criteriaQuery.select(claimedTaskRoot);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.workflowItem), workflowItem),
                                                criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.owner), ePerson)
                                                )
                            );
        return uniqueResult(context, criteriaQuery, false, ClaimedTask.class, -1, -1);


    }

    @Override
    public List<ClaimedTask> findByEperson(Context context, EPerson ePerson) throws SQLException {
//        Criteria criteria = createCriteria(context, ClaimedTask.class);
//        criteria.add(Restrictions.eq("owner", ePerson));
//
//        return list(criteria);
//
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ClaimedTask.class);
        Root<ClaimedTask> claimedTaskRoot = criteriaQuery.from(ClaimedTask.class);
        criteriaQuery.select(claimedTaskRoot);
        criteriaQuery.where(criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.owner), ePerson));
        return list(context, criteriaQuery, false, ClaimedTask.class, -1, -1);
    }

    @Override
    public List<ClaimedTask> findByWorkflowItemAndStepId(Context context, XmlWorkflowItem workflowItem, String stepID) throws SQLException {
//        Criteria criteria = createCriteria(context, ClaimedTask.class);
//        criteria.add(Restrictions.and(
//                Restrictions.eq("workflowItem", workflowItem),
//                Restrictions.eq("stepId", stepID)
//        ));
//
//        return list(criteria);
//
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ClaimedTask.class);
        Root<ClaimedTask> claimedTaskRoot = criteriaQuery.from(ClaimedTask.class);
        criteriaQuery.select(claimedTaskRoot);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.workflowItem), workflowItem),
                                                criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.stepId), stepID)
                                                )
                            );
        return list(context, criteriaQuery, false, ClaimedTask.class, -1, -1);
    }

    @Override
    public ClaimedTask findByEPersonAndWorkflowItemAndStepIdAndActionId(Context context, EPerson ePerson, XmlWorkflowItem workflowItem, String stepID, String actionID) throws SQLException {
//        Criteria criteria = createCriteria(context, ClaimedTask.class);
//        criteria.add(Restrictions.and(
//                Restrictions.eq("workflowItem", workflowItem),
//                Restrictions.eq("owner", ePerson),
//                Restrictions.eq("stepId", stepID),
//                Restrictions.eq("actionId", actionID)
//        ));
//
//        return uniqueResult(criteria);
//
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ClaimedTask.class);
        Root<ClaimedTask> claimedTaskRoot = criteriaQuery.from(ClaimedTask.class);
        criteriaQuery.select(claimedTaskRoot);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.workflowItem), workflowItem),
                                                criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.stepId), stepID),
                                                criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.owner), ePerson),
                                                criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.actionId), actionID)
                                                )
                            );
        return uniqueResult(context, criteriaQuery, false, ClaimedTask.class, -1, -1);
    }

    @Override
    public List<ClaimedTask> findByWorkflowItemAndStepIdAndActionId(Context context, XmlWorkflowItem workflowItem, String stepID, String actionID) throws SQLException {
//        Criteria criteria = createCriteria(context, ClaimedTask.class);
//        criteria.add(Restrictions.and(
//                Restrictions.eq("workflowItem", workflowItem),
//                Restrictions.eq("stepId", stepID),
//                Restrictions.eq("actionId", actionID)
//        ));
//
//        return list(criteria);
//
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ClaimedTask.class);
        Root<ClaimedTask> claimedTaskRoot = criteriaQuery.from(ClaimedTask.class);
        criteriaQuery.select(claimedTaskRoot);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.workflowItem), workflowItem),
                                                criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.stepId), stepID),
                                                criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.actionId), actionID)
                                                )
                            );
        return list(context, criteriaQuery, false, ClaimedTask.class, -1, -1);
    }

    @Override
    public List<ClaimedTask> findByStep(Context context, String stepID) throws SQLException {
//        Criteria criteria = createCriteria(context, ClaimedTask.class);
//        criteria.add(Restrictions.eq("stepId", stepID));
//
//        return list(criteria);
//
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, ClaimedTask.class);
        Root<ClaimedTask> claimedTaskRoot = criteriaQuery.from(ClaimedTask.class);
        criteriaQuery.select(claimedTaskRoot);
        criteriaQuery.where(criteriaBuilder.equal(claimedTaskRoot.get(ClaimedTask_.stepId), stepID));
        return list(context, criteriaQuery, false, ClaimedTask.class, -1, -1);
    }
}
