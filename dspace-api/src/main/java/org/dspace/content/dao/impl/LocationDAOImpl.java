package org.dspace.content.dao.impl;

import org.dspace.content.dao.LocationDAO;
import org.dspace.content.dao.pojo.Location;
import org.dspace.core.AbstractHibernateDAO;
import org.dspace.core.Context;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LocationDAOImpl extends AbstractHibernateDAO<Location> implements LocationDAO {

    // Thread-safe cache for storing dso_id -> Location mappings
    private final Map<UUID, Location> locationCache = new ConcurrentHashMap<>();
    boolean isCacheInitialized = false;

    /**
     * Initializes the cache on startup by loading all locations into the map.
     */
    public void initializeCache(Context context) throws SQLException {
        System.out.println("Initializing location cache...");
        long startTime = System.currentTimeMillis();

        List<Location> allLocations = findAllLocations(context);
        for (Location location : allLocations) {
            locationCache.put(location.getDsoId(), location);
        }

        System.out.println("Location cache initialized with " + locationCache.size() + " entries in " +
                (System.currentTimeMillis() - startTime) + "ms");
    }

    @Override
    public Location findLocationByDsoId(Context context, UUID dsoId) throws SQLException {
        if (!isCacheInitialized) {
            System.out.println("First time access to location cache. Initializing...");
            initializeCache(context);
            isCacheInitialized = true;
        }
        // Check if the location is already cached
        Location location = locationCache.get(dsoId);
        return location;
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

    public void clearCache() {
        locationCache.clear();
        System.out.println("Location cache cleared.");
    }

    public void reloadCache(Context context) throws SQLException {
        clearCache();
        initializeCache(context);
    }
}