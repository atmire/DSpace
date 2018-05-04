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
            Restrictions.eq("left_id", item),
            Restrictions.eq("right_id", item)
        ));

        return list(criteria);
    }
}
