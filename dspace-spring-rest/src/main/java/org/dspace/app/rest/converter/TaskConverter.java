/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.TaskRest;
import org.dspace.content.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskConverter extends DSpaceConverter<Task, TaskRest>{


    public TaskRest fromModel(Task task) {
        TaskRest taskRest =  new TaskRest();
        taskRest.setName(task.getTaskName());
        taskRest.setId(task.getTaskName());
        taskRest.setDescription(task.getDescription());
        return taskRest;
    }

    public Task toModel(TaskRest taskRest) {
        Task task = new Task();
        task.setTaskName(taskRest.getName());
        task.setTaskName(taskRest.getId());
        task.setDescription(taskRest.getDescription());
        return task;
    }
}
