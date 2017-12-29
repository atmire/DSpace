/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import org.dspace.content.service.TaskService;
import org.dspace.core.Context;
import org.dspace.servicemanager.config.DSpaceConfigurationService;
import org.dspace.util.ScriptLauncherConverter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TaskServiceImpl implements TaskService{

    Map<String, Task> taskMap = new HashMap<>();

    public TaskServiceImpl(){
        fillTaskMap();
    }

    private void fillTaskMap() {
        List<Task> tasks = populateListWithTasks();
        for(Task task : tasks){
            taskMap.put(task.getTaskName(), task);
        }
    }

    public Task findOne(Context context, String taskName){
        return taskMap.get(taskName);
    }

    public List<Task> findAll(Context context) {
        List<Task> tasks = populateListWithTasks();
        return tasks;
    }

    private List<Task> populateListWithTasks() {
        List<Task> tasks = new LinkedList<>();
        tasks.addAll(this.getAllScriptLauncherTasks());
        tasks.addAll(this.getAllCurationTasks());
        return tasks;
    }

    public List<Task> getAllScriptLauncherTasks(){
        return ScriptLauncherConverter.getAllScriptLauncherTasks();
    }

    public List<Task> getAllCurationTasks(){
        String[] strings = new DSpaceConfigurationService().getArrayProperty("curate.ui.tasknames");
        List<Task> tasksToBeAdded = new LinkedList<>();
        for (String s : strings){
            Task task = new Task();
            task.setTaskName(s.split("=")[0].trim());
            tasksToBeAdded.add(task);
        }
        return tasksToBeAdded;
    }
}
