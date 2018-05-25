package org.dspace.app.rest.model;

import org.dspace.app.rest.RestResourceController;
import org.dspace.content.EntityType;

public class RelationshipTypeRest extends BaseObjectRest<Integer> {

    public static final String NAME = "relationshiptype";
    public static final String CATEGORY = "core";

    private String leftLabel;
    private String rightLabel;
    private int leftMinCardinality;
    private int leftMaxCardinality;
    private int rightMinCardinality;
    private int rightMaxCardinality;
    private EntityType leftType;
    private EntityType rightType;

    public String getType() {
        return NAME;
    }

    public String getCategory() {
        return CATEGORY;
    }

    public Class getController() {
        return RestResourceController.class;
    }

    public String getLeftLabel() {
        return leftLabel;
    }

    public void setLeftLabel(String leftLabel) {
        this.leftLabel = leftLabel;
    }

    public String getRightLabel() {
        return rightLabel;
    }

    public void setRightLabel(String rightLabel) {
        this.rightLabel = rightLabel;
    }

    public int getLeftMinCardinality() {
        return leftMinCardinality;
    }

    public void setLeftMinCardinality(int leftMinCardinality) {
        this.leftMinCardinality = leftMinCardinality;
    }

    public int getLeftMaxCardinality() {
        return leftMaxCardinality;
    }

    public void setLeftMaxCardinality(int leftMaxCardinality) {
        this.leftMaxCardinality = leftMaxCardinality;
    }

    public int getRightMinCardinality() {
        return rightMinCardinality;
    }

    public void setRightMinCardinality(int rightMinCardinality) {
        this.rightMinCardinality = rightMinCardinality;
    }

    public int getRightMaxCardinality() {
        return rightMaxCardinality;
    }

    public void setRightMaxCardinality(int rightMaxCardinality) {
        this.rightMaxCardinality = rightMaxCardinality;
    }

    public EntityType getLeftType() {
        return leftType;
    }

    public void setLeftType(EntityType leftType) {
        this.leftType = leftType;
    }

    public EntityType getRightType() {
        return rightType;
    }

    public void setRightType(EntityType rightType) {
        this.rightType = rightType;
    }
}
