package org.dspace.content.dao.impl;

import org.dspace.content.dao.DSOWithPoliciesDAO;
import org.dspace.content.dao.LocationDAO;
import org.dspace.content.dao.pojo.DsoWithPolicies;
import org.dspace.content.dao.pojo.Location;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DSOWithPoliciesDAOImpl extends AbstractHibernateDAO<DsoWithPolicies> implements DSOWithPoliciesDAO {

    @Override
    public DsoWithPolicies findDsoWithPoliciesByDsoId(Context context, UUID dsoId) throws SQLException {
        String sql = "SELECT Cast(dso_id as varchar), array_to_string(read, ',') as read, " +
                "array_to_string(edit, ',') as edit, array_to_string(admin, ',') as admin " +
                "FROM dso_policies_including_inherited_and_implied " +
                "WHERE Cast(dso_id as varchar) = :dsoId";
        Query query = getHibernateSession(context).createNativeQuery(sql);
        // Set postgres sql dialect
        query.setParameter("dsoId", dsoId.toString());
        return new DsoWithPolicies((Object[]) query.getSingleResult());
    }

    @Override
    public List<DsoWithPolicies> findAllDsosWithPolicies(Context context) throws SQLException {
        String sql = "SELECT Cast(dso_id as varchar), array_to_string(read, ',') as read, " +
                "array_to_string(edit, ',') as edit, array_to_string(admin, ',')as admin " +
                "FROM dso_policies_including_inherited_and_implied";
        Query query = getHibernateSession(context).createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        List<DsoWithPolicies> locations = new ArrayList<>();
        for (Object[] row : results) {
            locations.add(new DsoWithPolicies(row));
        }
        return locations;
    }
}
