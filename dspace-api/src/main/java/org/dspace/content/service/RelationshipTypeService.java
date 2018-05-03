package org.dspace.content.service;

import java.sql.SQLException;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.EntityType;
import org.dspace.content.RelationshipType;
import org.dspace.core.Context;
import org.dspace.service.DSpaceCRUDService;

public interface RelationshipTypeService extends DSpaceCRUDService<RelationshipType> {

    RelationshipType create(Context context,RelationshipType relationshipType) throws SQLException, AuthorizeException;

    RelationshipType findbyTypesAndLabels(Context context,EntityType leftType,EntityType rightType,
                                          String leftLabel,String rightLabel)
                                            throws SQLException;
}
