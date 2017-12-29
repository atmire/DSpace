/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
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
