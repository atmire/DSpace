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
import java.util.Map;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.dspace.app.bulkedit.BulkEditChange;
import org.dspace.app.bulkedit.MetadataImportException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.scripts.handler.DSpaceRunnableHandler;
import org.dspace.workflow.WorkflowException;

public interface BulkEditImportService {
    /**
     * Import or update an Item from a {@link BulkEditChange}
     * @param context               DSpace context
     * @param handler               DSpaceRunnableHandler to output messages or content to
     * @param commandLine           Optional CommandLine arguments used to call the import
     * @param bulkEditChange        BulkEditChange containing information about the to-be-imported or updated item
     * @param fakeToRealUUIDMap     A map containing previously imported/updated item UUIDs, mapped to their fake UUID
     *                              found in their respective {@link BulkEditChange}. This way, real relationships
     *                              between newly imported items can be made.
     * @param useCollectionTemplate Use the item's collection template when creating a new item
     * @param useWorkflow           Allow new items to go through workflow
     * @param workflowNotify        Allow workflow notifications for new workflow items
     * @param archive               Archive newly created items
     */
    BulkEditChange importBulkEditChange(Context context, DSpaceRunnableHandler handler, CommandLine commandLine,
                                        BulkEditChange bulkEditChange, Map<UUID, UUID> fakeToRealUUIDMap,
                                        boolean useCollectionTemplate, boolean useWorkflow,
                                        boolean workflowNotify, boolean archive)
        throws SQLException, AuthorizeException, IOException, MetadataImportException, WorkflowException;
}
