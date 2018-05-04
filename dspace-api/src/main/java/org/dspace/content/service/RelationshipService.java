package org.dspace.content.service;

import java.sql.SQLException;
import java.util.List;

import org.dspace.content.Item;
import org.dspace.content.Relationship;
import org.dspace.core.Context;
import org.dspace.service.DSpaceCRUDService;

public interface RelationshipService extends DSpaceCRUDService<Relationship> {
    public List<Relationship> findByItem(Context context,Item item) throws SQLException;

    public List<Relationship> findAll(Context context) throws SQLException;

}