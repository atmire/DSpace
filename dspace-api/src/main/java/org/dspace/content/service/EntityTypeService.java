package org.dspace.content.service;

import java.sql.SQLException;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.EntityType;
import org.dspace.core.Context;
import org.dspace.service.DSpaceCRUDService;

public interface EntityTypeService extends DSpaceCRUDService<EntityType> {

    public EntityType findByEntityType(Context context,String entityType) throws SQLException;

    public EntityType create(Context context, String entityTypeString) throws SQLException, AuthorizeException;
}
