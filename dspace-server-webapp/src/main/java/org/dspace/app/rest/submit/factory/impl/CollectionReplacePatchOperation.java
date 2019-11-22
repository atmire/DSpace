/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.factory.impl;

import java.sql.SQLException;

import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.factories.impl.PatchOperation;
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
        this.replace(context, resource, operation.getValue());
        return resource;
    }

    /**
     * TODO
     * @param context
     * @param source
     * @param value
     * @throws SQLException
     * @throws DCInputsReaderException
     */
    private void replace(Context context, InProgressSubmission source, Object value)
            throws SQLException, DCInputsReaderException {
        if (!(source instanceof WorkspaceItem)) {
            throw new IllegalArgumentException("the replace operation is only supported on workspaceitem");
        }
        WorkspaceItem wsi = (WorkspaceItem) source;
        String uuid = (String) value;
        Collection fromCollection = source.getCollection();
        Collection toCollection = collectionService.find(context, UUIDUtils.fromString(uuid));
        workspaceItemService.move(context, wsi, fromCollection, toCollection);
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return false;
        // TODO
//        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
//                && operation.getOp().trim().equalsIgnoreCase(OPERATION_REPLACE));
    }
}
