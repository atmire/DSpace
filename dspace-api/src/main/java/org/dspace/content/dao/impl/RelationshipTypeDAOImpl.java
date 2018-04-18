package org.dspace.content.dao.impl;

import java.sql.SQLException;
import java.util.UUID;

import org.dspace.content.RelationshipType;
import org.dspace.content.dao.RelationshipTypeDAO;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

public class RelationshipTypeDAOImpl extends AbstractHibernateDAO<RelationshipType> implements RelationshipTypeDAO {

    public RelationshipType findbyTypesAndLabels(Context context,UUID leftType,UUID rightType,
                                                    String leftLabel,String rightLabel)
                                                    throws SQLException {
        Criteria criteria = createCriteria(context,RelationshipType.class);
        criteria.add(Restrictions.and(
            Restrictions.eq("leftType.id", leftType),
            Restrictions.eq("rightType.id", rightType),
            Restrictions.eq("leftLabel", leftLabel),
            Restrictions.eq("rightLabel", rightLabel)
        ));
        return singleResult(criteria);
    }

}
