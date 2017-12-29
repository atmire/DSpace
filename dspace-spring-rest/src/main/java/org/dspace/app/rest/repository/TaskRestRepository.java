/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository;

import org.dspace.app.rest.converter.TaskConverter;
import org.dspace.app.rest.model.MetadataSchemaRest;
import org.dspace.app.rest.model.TaskRest;
import org.dspace.app.rest.model.hateoas.DSpaceResource;
import org.dspace.app.rest.model.hateoas.TaskResource;
import org.dspace.content.Task;
import org.dspace.content.service.TaskService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component(TaskRest.CATEGORY + "." + TaskRest.NAME)
public class TaskRestRepository extends DSpaceRestRepository<TaskRest, String> {

    @Autowired
    TaskService taskService;

    @Autowired
    TaskConverter taskConverter;

    public TaskRest findOne(Context context, String s) {
        return taskConverter.fromModel(taskService.findOne(context, s));
    }

    public Page<TaskRest> findAll(Context context, Pageable pageable) {

        List<Task> tasks = new LinkedList<>();
        for(Task task : taskService.findAll(context)){
            tasks.add(task);
        }
//        Page<TaskRest> page = new PageImpl<Task>(tasks, pageable, tasks.size()).map(taskConverter);
        Page<TaskRest> page = utils.getPage(tasks, pageable).map(taskConverter);
        return page;

    }

    public Class<TaskRest> getDomainClass() {
        return null;
    }

    public DSpaceResource<TaskRest> wrapResource(TaskRest model, String... rels) {
        return new TaskResource(model, utils, rels);
    }
}
