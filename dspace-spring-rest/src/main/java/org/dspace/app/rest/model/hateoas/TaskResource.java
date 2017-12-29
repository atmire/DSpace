package org.dspace.app.rest.model.hateoas;

import org.dspace.app.rest.model.TaskRest;
import org.dspace.app.rest.model.hateoas.annotations.RelNameDSpaceResource;
import org.dspace.app.rest.utils.Utils;

@RelNameDSpaceResource(TaskRest.NAME)
public class TaskResource extends DSpaceResource<TaskRest>{
    public TaskResource(TaskRest data, Utils utils, String... rels) {
        super(data, utils, rels);
    }
}
