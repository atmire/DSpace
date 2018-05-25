package org.dspace.app.rest.converter;

import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.dspace.app.rest.model.RelationshipRest;
import org.dspace.content.Relationship;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RelationshipConverter extends DSpaceConverter<org.dspace.content.Relationship,
    org.dspace.app.rest.model.RelationshipRest> {
    private static final Logger log = Logger.getLogger(RelationshipConverter.class);

    @Autowired
    private ItemService itemService;

    @Autowired
    private RelationshipTypeConverter relationshipTypeConverter;


    public RelationshipRest fromModel(Relationship obj) {
        RelationshipRest relationshipRest = new RelationshipRest();
        relationshipRest.setId(obj.getId());
        relationshipRest.setLeftId(obj.getLeftItem().getID());
        relationshipRest.setRelationshipType(relationshipTypeConverter.fromModel(obj.getRelationshipType()));
        relationshipRest.setRightId(obj.getRightItem().getID());
        relationshipRest.setPlace(obj.getPlace());
        return relationshipRest;
    }

    public Relationship toModel(RelationshipRest obj) {
        Relationship relationship = new Relationship();
        try {
            Context context = new Context();
            relationship.setLeftItem(itemService.find(context, obj.getLeftId()));
            relationship.setRightItem(itemService.find(context, obj.getRightId()));
        } catch (SQLException e) {
            log.error(e,e);
        }
        relationship.setRelationshipType(relationshipTypeConverter.toModel(obj.getRelationshipType()));
        relationship.setPlace(obj.getPlace());
        relationship.setId(obj.getId());
        return relationship;
    }
}
