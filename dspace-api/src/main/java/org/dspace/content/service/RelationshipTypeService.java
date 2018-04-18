package org.dspace.content.service;

import java.sql.SQLException;
import java.util.UUID;

import org.dspace.content.RelationshipType;
import org.dspace.core.Context;
import org.dspace.service.DSpaceCRUDService;

public interface RelationshipTypeService extends DSpaceCRUDService<RelationshipType> {

    RelationshipType create(Context context,RelationshipType relationshipType) throws SQLException;

    RelationshipType findbyTypesAndLabels(Context context,UUID leftType,UUID rightType,
                                            String leftLabel,String rightLabel)
                                            throws SQLException;
}
