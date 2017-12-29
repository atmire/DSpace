/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.service;

import org.dspace.content.Task;
import org.dspace.core.Context;

import java.util.List;

public interface TaskService {

    public List<Task> findAll(Context context);

    public Task findOne(Context context, String taskName);
}
