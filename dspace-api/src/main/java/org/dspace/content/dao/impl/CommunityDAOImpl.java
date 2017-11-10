/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.dao.impl;

import org.apache.commons.collections.ListUtils;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.ResourcePolicy_;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Community_;
import org.dspace.content.MetadataField;
import org.dspace.content.dao.CommunityDAO;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.AbstractHibernateDSODAO;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.xmlworkflow.storedcomponents.CollectionRole;
import org.hibernate.Criteria;
import javax.persistence.Query;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import javax.persistence.criteria.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Hibernate implementation of the Database Access Object interface class for the Community object.
 * This class is responsible for all database calls for the Community object and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class CommunityDAOImpl extends AbstractHibernateDSODAO<Community> implements CommunityDAO
{
    protected CommunityDAOImpl()
    {
        super();
    }

    /**
     * Get a list of all communities in the system. These are alphabetically
     * sorted by community name.
     *
     * @param context DSpace context object
     * @param sortField sort field
     *
     * @return the communities in the system
     * @throws SQLException if database error
     */
    @Override
    public List<Community> findAll(Context context, MetadataField sortField) throws SQLException
    {
        return findAll(context, sortField, null, null);
    }

    @Override
    public List<Community> findAll(Context context, MetadataField sortField, Integer limit, Integer offset) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT ").append(Community.class.getSimpleName()).append(" FROM Community as ").append(Community.class.getSimpleName()).append(" ");
        addMetadataLeftJoin(queryBuilder, Community.class.getSimpleName(), Arrays.asList(sortField));
        addMetadataSortQuery(queryBuilder, Arrays.asList(sortField), ListUtils.EMPTY_LIST);

        Query query = createQuery(context, queryBuilder.toString());
        if(offset != null)
        {
            query.setFirstResult(offset);
        }
        if(limit != null){
            query.setMaxResults(limit);
        }
        query.setParameter(sortField.toString(), sortField.getID());
        return list(query);
    }

    @Override
    public Community findByAdminGroup(Context context, Group group) throws SQLException {
//        Criteria criteria = createCriteria(context, Community.class);
//        criteria.add(Restrictions.eq("admins", group));
//        return singleResult(criteria);
//
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, Community.class);
        Root<Community> communityRoot = criteriaQuery.from(Community.class);
        criteriaQuery.select(communityRoot);
        criteriaQuery.where(criteriaBuilder.equal(communityRoot.get(Community_.admins), group));
        return singleResult(context, criteriaQuery);
    }

    @Override
    public List<Community> findAllNoParent(Context context, MetadataField sortField) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT community FROM Community as community ");
        addMetadataLeftJoin(queryBuilder, Community.class.getSimpleName().toLowerCase(), Arrays.asList(sortField));
        addMetadataValueWhereQuery(queryBuilder, ListUtils.EMPTY_LIST, null, " community.parentCommunities IS EMPTY");
        addMetadataSortQuery(queryBuilder, Arrays.asList(sortField), ListUtils.EMPTY_LIST);

        Query query = createQuery(context, queryBuilder.toString());
        query.setParameter(sortField.toString(), sortField.getID());
        query.setHint("org.hibernate.cacheable", Boolean.TRUE);


        return findMany(context, query);
    }

    @Override
    public List<Community> findAuthorized(Context context, EPerson ePerson, List<Integer> actions) throws SQLException {

        //TODO RAF CHECK

        /*TableRowIterator tri = DatabaseManager.query(context,
                "SELECT \n" +
                        "  * \n" +
                        "FROM \n" +
                        "  public.eperson, \n" +
                        "  public.epersongroup2eperson, \n" +
                        "  public.epersongroup, \n" +
                        "  public.community, \n" +
                        "  public.resourcepolicy\n" +
                        "WHERE \n" +
                        "  epersongroup2eperson.eperson_id = eperson.eperson_id AND\n" +
                        "  epersongroup.eperson_group_id = epersongroup2eperson.eperson_group_id AND\n" +
                        "  resourcepolicy.epersongroup_id = epersongroup.eperson_group_id AND\n" +
                        "  resourcepolicy.resource_id = community.community_id AND\n" +
                        " ( resourcepolicy.action_id = 3 OR \n" +
                        "  resourcepolicy.action_id = 11) AND \n" +
                        "  resourcepolicy.resource_type_id = 4 AND eperson.eperson_id = ?", context.getCurrentUser().getID());
        */
//        Criteria criteria = createCriteria(context, Community.class);
//        criteria.createAlias("resourcePolicies", "resourcePolicy");
//
//        Disjunction actionQuery = Restrictions.or();
//        for (Integer action : actions)
//        {
//            actionQuery.add(Restrictions.eq("resourcePolicy.actionId", action));
//        }
//        criteria.add(Restrictions.and(
//                Restrictions.eq("resourcePolicy.resourceTypeId", Constants.COMMUNITY),
//                Restrictions.eq("resourcePolicy.eperson", ePerson),
//                actionQuery
//        ));
//        criteria.setCacheable(true);
//
//        return list(criteria);
//
//
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder(context);
        CriteriaQuery criteriaQuery = getCriteriaQuery(criteriaBuilder, Community.class);
        Root<Community> communityRoot = criteriaQuery.from(Community.class);
        Join<Community, ResourcePolicy> join = communityRoot.join("resourcePolicies");
        List<Predicate> orPredicates = new LinkedList<Predicate>();
        for(Integer action : actions){
            orPredicates.add(criteriaBuilder.equal(join.get(ResourcePolicy_.actionId), action));
        }
        Predicate orPredicate = criteriaBuilder.and(orPredicates.toArray(new Predicate[]{}));
        criteriaQuery.select(communityRoot);
        criteriaQuery.where(criteriaBuilder.and(criteriaBuilder.equal(join.get(ResourcePolicy_.resourceTypeId), Constants.COLLECTION),
                                                criteriaBuilder.equal(join.get(ResourcePolicy_.eperson), ePerson),
                                                orPredicate
                                                )
                            );
        return list(context, criteriaQuery, true, Community.class, -1, -1);
    }

    @Override
    public List<Community> findAuthorizedByGroup(Context context, EPerson ePerson, List<Integer> actions) throws SQLException {
//        "SELECT \n" +
//                "  * \n" +
//                "FROM \n" +
//                "  public.eperson, \n" +
//                "  public.epersongroup2eperson, \n" +
//                "  public.epersongroup, \n" +
//                "  public.community, \n" +
//                "  public.resourcepolicy\n" +
//                "WHERE \n" +
//                "  epersongroup2eperson.eperson_id = eperson.eperson_id AND\n" +
//                "  epersongroup.eperson_group_id = epersongroup2eperson.eperson_group_id AND\n" +
//                "  resourcepolicy.epersongroup_id = epersongroup.eperson_group_id AND\n" +
//                "  resourcepolicy.resource_id = community.community_id AND\n" +
//                " ( resourcepolicy.action_id = 3 OR \n" +
//                "  resourcepolicy.action_id = 11) AND \n" +
//                "  resourcepolicy.resource_type_id = 4 AND eperson.eperson_id = ?", context.getCurrentUser().getID());
        StringBuilder query = new StringBuilder();
        query.append("select c from Community c join c.resourcePolicies rp join rp.epersonGroup rpGroup WHERE ");
        for (int i = 0; i < actions.size(); i++) {
            Integer action = actions.get(i);
            if(i != 0)
            {
                query.append(" AND ");
            }
            query.append("rp.actionId=").append(action);
        }
        query.append(" AND rp.resourceTypeId=").append(Constants.COMMUNITY);
        query.append(" AND rp.epersonGroup.id IN (select g.id from Group g where (from EPerson e where e.id = :eperson_id) in elements(epeople))");
        Query persistenceQuery = createQuery(context, query.toString());
        persistenceQuery.setParameter("eperson_id", ePerson.getID());

        persistenceQuery.setHint("org.hibernate.cacheable", Boolean.TRUE);

        return list(persistenceQuery);
    }

    @Override
    public int countRows(Context context) throws SQLException {
        return count(createQuery(context, "SELECT count(*) FROM Community"));
    }
}
