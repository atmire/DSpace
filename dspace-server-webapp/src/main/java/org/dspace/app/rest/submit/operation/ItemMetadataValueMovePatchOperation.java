/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.operation;

import java.sql.SQLException;

import org.dspace.app.rest.model.patch.MoveOperation;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.operation.PatchOperation;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Submission "move" PATCH operation.
 *
 * It is possible to rearrange the metadata values using the move operation. For
 * instance to put the 3rd author as 1st author you need to run:
 *
 * <code>
 * curl -X PATCH http://${dspace.url}/api/submission/workspaceitems/<:id-workspaceitem> -H "
 * Content-Type: application/json" -d '[{ "op": "move", "from": "
 * /sections/traditionalpageone/dc.contributor.author/2", "path": "
 * /sections/traditionalpageone/dc.contributor.author/0"}]'
 * </code>
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class ItemMetadataValueMovePatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    ItemService itemService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException {
        String[] splitTo = submitPatchUtils.getAbsolutePath(operation.getPath()).split("/");

        String evalFrom = submitPatchUtils.getAbsolutePath(((MoveOperation) operation).getFrom());
        String[] splitFrom = evalFrom.split("/");
        String metadata = splitFrom[0];

        if (splitTo.length > 1) {
            String stringTo = splitTo[1];
            if (splitFrom.length > 1) {
                String stringFrom = splitFrom[1];

                int intTo = Integer.parseInt(stringTo);
                int intFrom = Integer.parseInt(stringFrom);
                submitPatchUtils.moveValue(context, resource.getItem(), metadata, intFrom, intTo, itemService);
            }
        }
        return resource;
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        // TODO not hardcoded form name
        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
                && (operation.getPath().contains("traditionalpageone")
                || operation.getPath().contains("traditionalpagetwo"))
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_MOVE));
    }
}
