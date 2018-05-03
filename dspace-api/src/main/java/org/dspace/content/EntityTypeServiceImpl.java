package org.dspace.content;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.EntityTypeDAO;
import org.dspace.content.service.EntityTypeService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

public class EntityTypeServiceImpl implements EntityTypeService {

    @Autowired(required = true)
    protected EntityTypeDAO entityTypeDAO;

    @Autowired(required = true)
    protected AuthorizeService authorizeService;

    public EntityType findByEntityType(Context context,String entityType) throws SQLException {
        return entityTypeDAO.findByEntityType(context, entityType);
    }

    public EntityType create(Context context) throws SQLException, AuthorizeException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                "Only administrators can modify entityType");
        }
        return entityTypeDAO.create(context, new EntityType());
    }

    public EntityType create(Context context, String entityTypeString) throws SQLException, AuthorizeException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                "Only administrators can modify entityType");
        }
        EntityType entityType = new EntityType();
        entityType.setLabel(entityTypeString);
        return entityTypeDAO.create(context, entityType);
    }

    public EntityType find(Context context,int id) throws SQLException {
        EntityType entityType = entityTypeDAO.findByID(context, EntityType.class, id);
        return entityType;
    }

    public void update(Context context,EntityType entityType) throws SQLException, AuthorizeException {
        update(context,Collections.singletonList(entityType));
    }

    public void update(Context context,List<EntityType> entityTypes) throws SQLException, AuthorizeException {
        if (CollectionUtils.isNotEmpty(entityTypes)) {
            // Check authorisation - only administrators can change formats
            if (!authorizeService.isAdmin(context)) {
                throw new AuthorizeException(
                    "Only administrators can modify entityType");
            }

            for (EntityType entityType : entityTypes) {
                entityTypeDAO.save(context, entityType);
            }
        }
    }

    public void delete(Context context,EntityType entityType) throws SQLException, AuthorizeException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                "Only administrators can delete entityType");
        }
        entityTypeDAO.delete(context, entityType);
    }
}
