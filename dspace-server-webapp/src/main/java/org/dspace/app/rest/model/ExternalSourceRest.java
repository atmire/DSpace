package org.dspace.app.rest.model;

import org.dspace.app.rest.ExternalSourcesRestController;

public class ExternalSourceRest extends BaseObjectRest<String> {

    public static final String NAME = "externalsource";
    public static final String PLURAL_NAME = "externalsources";
    public static final String CATEGORY = RestAddressableModel.INTEGRATION;

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public Class getController() {
        return ExternalSourcesRestController.class;
    }

    @Override
    public String getType() {
        return NAME;
    }

    private String id;
    private String name;
    private boolean hierarchical;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHierarchical() {
        return hierarchical;
    }

    public void setHierarchical(boolean hierarchical) {
        this.hierarchical = hierarchical;
    }

}
