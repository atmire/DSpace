package org.dspace.content.dao;

import java.sql.SQLException;
import java.util.UUID;

import org.dspace.content.RelationshipType;
import org.dspace.core.Context;
import org.dspace.core.GenericDAO;

public interface RelationshipTypeDAO extends GenericDAO<RelationshipType> {

    RelationshipType findbyTypesAndLabels(Context context,
                                                UUID leftType,UUID rightType,String leftLabel,String rightLabel)
                                                throws SQLException;

}
