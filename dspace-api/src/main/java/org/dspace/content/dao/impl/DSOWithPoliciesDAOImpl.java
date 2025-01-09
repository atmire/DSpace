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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DSOWithPoliciesDAOImpl extends AbstractHibernateDAO<DsoWithPolicies> implements DSOWithPoliciesDAO {

    private final Map<UUID, DsoWithPolicies> policyCache = new ConcurrentHashMap<>();
    boolean isCacheInitialized = false;

    public void initializeCache(Context context) throws SQLException {
        System.out.println("Initializing location cache...");
        long startTime = System.currentTimeMillis();

        List<DsoWithPolicies> allDsoWithPolicies = findAllDsosWithPolicies(context);
        for (DsoWithPolicies dsoWithPolicies : allDsoWithPolicies) {
            policyCache.put(dsoWithPolicies.getDsoId(), dsoWithPolicies);
        }

        System.out.println("Policy cache initialized with " + policyCache.size() + " entries in " +
                (System.currentTimeMillis() - startTime) + "ms");
    }

    @Override
    public DsoWithPolicies findDsoWithPoliciesByDsoId(Context context, UUID dsoId) throws SQLException {
        if (!isCacheInitialized) {
            System.out.println("First time access to location cache. Initializing...");
            initializeCache(context);
            isCacheInitialized = true;
        }
        DsoWithPolicies dsoWithPolicies = policyCache.get(dsoId);
        return dsoWithPolicies;
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

    public void clearCache() {
        policyCache.clear();
        System.out.println("Location cache cleared.");
    }

    public void reloadCache(Context context) throws SQLException {
        clearCache();
        initializeCache(context);
    }
}
