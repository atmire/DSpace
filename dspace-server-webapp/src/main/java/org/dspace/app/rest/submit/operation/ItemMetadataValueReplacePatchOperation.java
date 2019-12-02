/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.operation;

import java.sql.SQLException;
import java.util.List;

import org.dspace.app.rest.model.MetadataValueRest;
import org.dspace.app.rest.model.patch.LateObjectEvaluator;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.operation.PatchOperation;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Submission "replace" PATCH operation.
 *
 * The replace operation allows to replace existent information with new one.
 * Attempt to use the replace operation to set not yet initialized information
 * must return an error.
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.url}/api/submission/workspaceitems/<:id-workspaceitem> -H "
 * Content-Type: application/json" -d '[{ "op": "replace", "path": "
 * /sections/traditionalpageone/dc.title/0", "value": {"value": "Add new
 * title", "language": "en"}}]'
 * </code>
 *
 * It is also possible to change only a single attribute of the {@link MetadataValueRest} (except the "place").
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.url}/api/submission/workspaceitems/<:id-workspaceitem> -H "
 * Content-Type: application/json" -d '[{ "op": "replace", "path": "
 * /sections/traditionalpageone/dc.title/0/language", "value": "it"}]'
 * </code>
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class ItemMetadataValueReplacePatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    ItemService itemService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException, IllegalAccessException {
        String[] split = submitPatchUtils.getAbsolutePath(operation.getPath()).split("/");
        List<MetadataValue> metadataByMetadataString = itemService.getMetadataByMetadataString(resource.getItem(),
                split[0]);
        Assert.notEmpty(metadataByMetadataString);
        int index = Integer.parseInt(split[1]);
        // if split size is one so we have a call to initialize or replace
        if (split.length == 2) {
            MetadataValueRest obj =
                    (MetadataValueRest) submitPatchUtils.evaluateSingleObject(
                            (LateObjectEvaluator) operation.getValue(), MetadataValueRest.class);
            submitPatchUtils.replaceValue(context, resource.getItem(), split[0], obj, index, itemService);
        } else {
            if (split.length == 3) {
                submitPatchUtils.setDeclaredField(context, resource.getItem(), operation.getValue(), split[0], split[2],
                        metadataByMetadataString, index, itemService);
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
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_REPLACE));
    }
}
