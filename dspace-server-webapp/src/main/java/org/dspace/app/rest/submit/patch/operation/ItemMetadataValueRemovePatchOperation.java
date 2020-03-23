/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.patch.operation;

import java.sql.SQLException;

import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.operation.DSpaceObjectMetadataPatchUtils;
import org.dspace.app.rest.repository.patch.operation.PatchOperation;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.MetadataField;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Submission "remove" PATCH operation.
 *
 * Path used to remove a <b>specific metadata value</b> using index:
 * "/sections/<:name-of-the-form>/<:metadata>/<:idx-zero-based>"
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.server.url}/api/submission/workspaceitems/<:id-workspaceitem> -H "
 * Content-Type: application/json" -d '[{ "op": "remove", "path": "
 * /sections/traditionalpageone/dc.title/1"}]'
 * </code>
 *
 * Path used to remove <b>all the metadata values</b> for a specific metadata key:
 * "/sections/<:name-of-the-form>/<:metadata>"
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.server.url}/api/submission/workspaceitems/<:id-workspaceitem> -H "
 * Content-Type: application/json" -d '[{ "op": "remove", "path": "
 * /sections/traditionalpageone/dc.title"}]'
 * </code>
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class ItemMetadataValueRemovePatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    ItemService itemService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Autowired
    DSpaceObjectMetadataPatchUtils metadataPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException {
        String[] split = submitPatchUtils.getAbsolutePath(operation.getPath()).split("/");
        MetadataField metadataField = metadataPatchUtils.getMetadataField(context, split[0]);
        if (split.length == 1) {
            metadataPatchUtils.removeValue(context, resource.getItem(), itemService, metadataField, null);
        } else {
            String indexToDelete = split[1];
            metadataPatchUtils.removeValue(context, resource.getItem(), itemService, metadataField, indexToDelete);
        }
        return resource;
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        // TODO not hardcoded form name
        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
                && (operation.getPath().contains("traditionalpageone")
                || operation.getPath().contains("traditionalpagetwo"))
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_REMOVE));
    }
}