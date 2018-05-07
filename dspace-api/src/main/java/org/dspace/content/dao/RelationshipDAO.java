package org.dspace.content.dao;

import java.sql.SQLException;
import java.util.List;

import org.dspace.content.Item;
import org.dspace.content.Relationship;
import org.dspace.core.Context;
import org.dspace.core.GenericDAO;

public interface RelationshipDAO extends GenericDAO<Relationship> {

    List<Relationship> findByItem(Context context,Item item) throws SQLException;

    int findPlaceByLeftItem(Context context,Item item) throws SQLException;
}
