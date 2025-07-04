/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.bulkedit.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.dspace.app.bulkedit.BulkEditChange;
import org.dspace.app.bulkedit.MetadataImportException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.scripts.handler.DSpaceRunnableHandler;
import org.dspace.workflow.WorkflowException;

public interface BulkEditService {
    /**
     * Import or update a Items from a List of {@link BulkEditChange} in batches
     * @param context               DSpace context
     * @param bulkEditChanges       List of BulkEditChanges containing information about the to-be-imported or updated
     *                              items
     */
    List<BulkEditChange> applyBulkEditChanges(Context context, List<BulkEditChange> bulkEditChanges)
        throws SQLException, AuthorizeException, IOException, MetadataImportException, WorkflowException;

    /**
     * Import or update an Item from a {@link BulkEditChange}
     * @param context               DSpace context
     * @param bulkEditChange        BulkEditChange containing information about the to-be-imported or updated item
     */
    BulkEditChange applyBulkEditChange(Context context, BulkEditChange bulkEditChange)
        throws SQLException, AuthorizeException, IOException, MetadataImportException, WorkflowException;

    /**
     * Set the handler
     * @param handler   DSpaceRunnableHandler to output messages or content to
     */
    void setHandler(DSpaceRunnableHandler handler);

    /**
     * Set whether we want to use the collection's template
     * @param useCollectionTemplate Use the item's collection template when creating a new item
     */
    void setUseCollectionTemplate(boolean useCollectionTemplate);

    /**
     * Set whether to allow new items to go through workflow
     * @param useWorkflow   Allow new items to go through workflow
     */
    void setUseWorkflow(boolean useWorkflow);

    /**
     * Set whether to allow workflow notifications
     * @param workflowNotify    Allow workflow notifications for new workflow items
     */
    void setWorkflowNotify(boolean workflowNotify);

    /**
     * Set whether to archive items or leave them in their workspace state
     * @param archive   Archive newly created items
     */
    void setArchive(boolean archive);
}
