package org.dspace.content.dao;

import org.dspace.content.dao.pojo.DsoWithPolicies;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface DSOWithPoliciesDAO {

    DsoWithPolicies findDsoWithPoliciesByDsoId(Context context, UUID dsoId) throws SQLException;

    List<DsoWithPolicies> findAllDsosWithPolicies(Context context) throws SQLException;
}
