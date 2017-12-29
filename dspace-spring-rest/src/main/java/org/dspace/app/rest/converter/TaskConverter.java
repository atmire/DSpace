package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.TaskRest;
import org.dspace.content.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskConverter extends DSpaceConverter<Task, TaskRest>{


    public TaskRest fromModel(Task task) {
        TaskRest taskRest =  new TaskRest();
        taskRest.setTaskName(task.getTaskName());
        taskRest.setId(task.getTaskName());
        taskRest.setDescription(task.getDescription());
        return taskRest;
    }

    public Task toModel(TaskRest taskRest) {
        Task task = new Task();
        task.setTaskName(taskRest.getTaskName());
        task.setTaskName(taskRest.getId());
        task.setDescription(taskRest.getDescription());
        return task;
    }
}
