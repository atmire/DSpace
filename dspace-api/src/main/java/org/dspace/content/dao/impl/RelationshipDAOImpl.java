package org.dspace.content.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.dspace.content.Item;
import org.dspace.content.Relationship;
import org.dspace.content.dao.RelationshipDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public class RelationshipDAOImpl extends AbstractHibernateDAO<Relationship> implements RelationshipDAO {

    public List<Relationship> findByItem(Context context,Item item) throws SQLException {
        Criteria criteria = createCriteria(context,Relationship.class);
        criteria.add(Restrictions.or(
            Restrictions.eq("leftItem", item),
            Restrictions.eq("rightItem", item)
        ));

        return list(criteria);
    }

    public int findPlaceByLeftItem(Context context, Item item) throws SQLException {
        Criteria criteria = createCriteria(context, Relationship.class);
        criteria.add(Restrictions.and(
            Restrictions.eq("leftItem", item)
        ));

        Relationship relationship = singleResult(criteria);
        return relationship == null ? 0 : relationship.getPlace();
    }
}
