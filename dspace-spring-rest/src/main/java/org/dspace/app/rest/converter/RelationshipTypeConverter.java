package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.RelationshipTypeRest;
import org.dspace.content.RelationshipType;
import org.springframework.stereotype.Component;

@Component
public class RelationshipTypeConverter extends DSpaceConverter<org.dspace.content.RelationshipType,
    org.dspace.app.rest.model.RelationshipTypeRest>  {
    public RelationshipTypeRest fromModel(RelationshipType obj) {
        RelationshipTypeRest relationshipTypeRest = new RelationshipTypeRest();

        relationshipTypeRest.setId(obj.getId());
        relationshipTypeRest.setLeftLabel(obj.getLeftLabel());
        relationshipTypeRest.setRightLabel(obj.getRightLabel());
        relationshipTypeRest.setLeftMinCardinality(obj.getLeftMinCardinality());
        relationshipTypeRest.setLeftMaxCardinality(obj.getLeftMaxCardinality());
        relationshipTypeRest.setRightMinCardinality(obj.getRightMinCardinality());
        relationshipTypeRest.setRightMaxCardinality(obj.getRightMaxCardinality());
        relationshipTypeRest.setLeftType(obj.getLeftType());
        relationshipTypeRest.setRightType(obj.getRightType());

        return relationshipTypeRest;
    }

    public RelationshipType toModel(RelationshipTypeRest obj) {
        RelationshipType relationshipType = new RelationshipType();

        relationshipType.setId(obj.getId());
        relationshipType.setLeftLabel(obj.getLeftLabel());
        relationshipType.setRightLabel(obj.getRightLabel());
        relationshipType.setLeftMinCardinality(obj.getLeftMinCardinality());
        relationshipType.setLeftMaxCardinality(obj.getLeftMaxCardinality());
        relationshipType.setRightMinCardinality(obj.getRightMinCardinality());
        relationshipType.setRightMaxCardinality(obj.getRightMaxCardinality());
        relationshipType.setLeftType(obj.getLeftType());
        relationshipType.setRightType(obj.getRightType());

        return relationshipType;
    }
}
