package org.dspace.app.rest.model;

import java.util.UUID;

import org.dspace.app.rest.RestResourceController;
import org.dspace.content.RelationshipType;

public class RelationshipRest extends BaseObjectRest<Integer> {
    public static final String NAME = "relationship";
    public static final String CATEGORY = "entities";

    private UUID leftId;
    private RelationshipType relationshipType;
    private UUID rightId;
    private int place;

    public String getType() {
        return NAME;
    }

    public String getCategory() {
        return CATEGORY;
    }

    public Class getController() {
        return RestResourceController.class;
    }

    public UUID getLeftId() {
        return leftId;
    }

    public void setLeftId(UUID leftId) {
        this.leftId = leftId;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    public UUID getRightId() {
        return rightId;
    }

    public void setRightId(UUID rightId) {
        this.rightId = rightId;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }
}
