package org.dspace.content.dao.impl;

import org.dspace.content.dao.LocationDAO;
import org.dspace.content.dao.pojo.Location;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LocationDAOImpl extends AbstractHibernateDAO<Location> implements LocationDAO {

    @Override
    public Location findLocationByDsoId(Context context, UUID dsoId) throws SQLException {
        String sql = "SELECT Cast(dso_id as varchar), array_to_string(location_comm, ',') as location_comm, " +
                "array_to_string(location_coll, ',') FROM locations WHERE Cast(dso_id as varchar) = :dsoId";
        Query query = getHibernateSession(context).createNativeQuery(sql);
        query.setParameter("dsoId", dsoId.toString());
        return new Location((Object[]) query.getSingleResult());
    }

    @Override
    public List<Location> findAllLocations(Context context) throws SQLException {
        String sql = "SELECT Cast(dso_id as varchar), array_to_string(location_comm, ',') as location_comm, " +
                "array_to_string(location_coll, ',') FROM locations";

        Query query = getHibernateSession(context).createNativeQuery(sql);
        List<Object[]> results = query.getResultList();
        List<Location> locations = new ArrayList<>();
        for (Object[] row : results) {
            locations.add(new Location(row));
        }
        return locations;
    }
}
