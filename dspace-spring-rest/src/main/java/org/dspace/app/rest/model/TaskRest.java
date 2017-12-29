package org.dspace.app.rest.model;

import org.dspace.app.rest.RestResourceController;

public class TaskRest extends BaseObjectRest<String> {
    public static final String NAME = "task";
    public static final String CATEGORY = RestModel.CONFIGURATION;

    private String taskName;
    public String getTaskName(){
        return taskName;
    }
    public void setTaskName(String taskName){
        this.taskName = taskName;
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
