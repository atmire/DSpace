package org.dspace.content;

public class Task {
    private String taskName;
    private String description;
    public String getTaskName(){
        return taskName;
    }
    public void setTaskName(String taskName){
        this.taskName = taskName;
    }

    public String getDescription(){
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
