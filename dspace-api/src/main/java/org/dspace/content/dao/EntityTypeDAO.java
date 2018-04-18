package org.dspace.content.dao;

import java.sql.SQLException;

import org.dspace.content.EntityType;
import org.dspace.core.Context;
import org.dspace.core.GenericDAO;

public interface EntityTypeDAO extends GenericDAO<EntityType> {

    public EntityType findByEntityType(Context context, String entityType) throws SQLException;

}
