/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.operation;

import java.sql.SQLException;

import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.operation.PatchOperation;
import org.dspace.app.util.DCInputsReaderException;
import org.dspace.content.Collection;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.Context;
import org.dspace.util.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Submission "replace" patch operation to replace the Collection the submission item is in
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class CollectionReplacePatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    CollectionService collectionService;

    @Autowired
    ItemService itemService;

    @Autowired
    WorkspaceItemService workspaceItemService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException, DCInputsReaderException {
        if (!(resource instanceof WorkspaceItem)) {
            throw new IllegalArgumentException("the replace operation is only supported on workspaceitem");
        }
        WorkspaceItem wsi = (WorkspaceItem) resource;
        String uuid = (String) operation.getValue();
        Collection fromCollection = resource.getCollection();
        Collection toCollection = collectionService.find(context, UUIDUtils.fromString(uuid));
        workspaceItemService.move(context, wsi, fromCollection, toCollection);
        return resource;
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
                && operation.getPath().contains("collection")
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_REPLACE));
    }
}
