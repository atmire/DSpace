package org.dspace.content;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.RelationshipTypeDAO;
import org.dspace.content.service.RelationshipTypeService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

public class RelationshipTypeServiceImpl implements RelationshipTypeService {

    @Autowired(required = true)
    protected RelationshipTypeDAO relationshipTypeDAO;

    @Autowired(required = true)
    protected AuthorizeService authorizeService;

    public RelationshipType create(Context context) throws SQLException, AuthorizeException {
        return relationshipTypeDAO.create(context, new RelationshipType());
    }

    public RelationshipType create(Context context, RelationshipType relationshipType) throws SQLException {
        return relationshipTypeDAO.create(context, relationshipType);
    }

    public RelationshipType findbyTypesAndLabels(Context context,UUID leftType,UUID rightType,String leftLabel,
                                                       String rightLabel) throws SQLException {
        return relationshipTypeDAO.findbyTypesAndLabels(context, leftType, rightType, leftLabel, rightLabel);
    }

    public RelationshipType find(Context context,int id) throws SQLException {
        return relationshipTypeDAO.findByID(context, RelationshipType.class, id);
    }

    public void update(Context context,RelationshipType relationshipType) throws SQLException, AuthorizeException {
        update(context,Collections.singletonList(relationshipType));
    }

    public void update(Context context,List<RelationshipType> relationshipTypes)
        throws SQLException, AuthorizeException {
        if (CollectionUtils.isNotEmpty(relationshipTypes)) {
            // Check authorisation - only administrators can change formats
            if (!authorizeService.isAdmin(context)) {
                throw new AuthorizeException(
                    "Only administrators can modify RelationshipType");
            }

            for (RelationshipType relationshipType : relationshipTypes) {
                relationshipTypeDAO.save(context, relationshipType);
            }
        }

    }

    public void delete(Context context,RelationshipType relationshipType) throws SQLException, AuthorizeException {
        if (!authorizeService.isAdmin(context)) {
            throw new AuthorizeException(
                "Only administrators can delete entityType");
        }
        relationshipTypeDAO.delete(context, relationshipType);
    }
}
