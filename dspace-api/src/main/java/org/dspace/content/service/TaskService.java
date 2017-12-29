package org.dspace.content.service;

import org.dspace.content.Task;
import org.dspace.core.Context;

import java.util.List;

public interface TaskService {

    public List<Task> findAll(Context context);

    public Task findOne(Context context, String taskName);
}
