package org.dspace.app.rest.model;

import org.dspace.app.rest.RestResourceController;

public class EntityTypeRest extends BaseObjectRest<Integer> {

    public static final String NAME = "entitytype";
    public static final String CATEGORY = "core";

    public String getCategory() {
        return CATEGORY;
    }

    public Class getController() {
        return RestResourceController.class;
    }

    public String getType() {
        return NAME;
    }

    private String label;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
