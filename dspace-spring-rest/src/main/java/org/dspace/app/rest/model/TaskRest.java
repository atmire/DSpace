/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

import org.dspace.app.rest.RestResourceController;

public class TaskRest extends BaseObjectRest<String> {
    public static final String NAME = "task";
    public static final String CATEGORY = RestModel.CONFIGURATION;

    private String name;
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }

    private String description;
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public Class getController() {
        return RestResourceController.class;
    }

    public void setId(String id) {
        super.setId(id);
    }
}
