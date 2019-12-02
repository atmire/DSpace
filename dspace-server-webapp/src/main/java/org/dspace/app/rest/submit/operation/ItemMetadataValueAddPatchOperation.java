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
 * Submission "add" PATCH operation.
 *
 * Path used to add a new value to an <b>existent metadata</b>:
 * "/sections/<:name-of-the-form>/<:metadata>/-"
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.url}/api/submission/workspaceitems/<:id-workspaceitem> -H "
 * Content-Type: application/json" -d '[{ "op": "add", "path": "
 * /sections/traditionalpageone/dc.title/-", "value": {"value": "Add new
 * title"}}]'
 * </code>
 *
 * Path used to insert the new metadata value in a <b>specific position</b>:
 * "/sections/<:name-of-the-form>/<:metadata>/<:idx-zero-based>"
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.url}/api/submission/workspaceitems/<:id-workspaceitem> -H "
 * Content-Type: application/json" -d '[{ "op": "add", "path": "
 * /sections/traditionalpageone/dc.title/1", "value": {"value": "Add new
 * title"}}]'
 * </code>
 *
 * Path used to <b>initialize or replace</b> the whole metadata values:
 * "/sections/<:name-of-the-form>/<:metadata>"
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.url}/api/submission/workspaceitems/<:id-workspaceitem> -H "
 * Content-Type: application/json" -d '[{ "op": "add", "path": "
 * /sections/traditionalpageone/dc.title", "value": [{"value": "Add new first
 * title"}, {"value": "Add new second title"}]}]'
 * </code>
 *
 * Please note that according to the JSON Patch specification RFC6902 to
 * initialize a new metadata in the section the add operation must receive an
 * array of values and it is not possible to add a single value to the not yet
 * initialized "/sections/<:name-of-the-form>/<:metadata>/-" path.
 *
 * NOTE: If the target location specifies an object member that does exist, that
 * member's value is replaced.
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class ItemMetadataValueAddPatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    ItemService itemService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException {
        String[] split = submitPatchUtils.getAbsolutePath(operation.getPath()).split("/");
        // if split size is one so we have a call to initialize or replace
        if (split.length == 1) {
            List<MetadataValueRest> list = submitPatchUtils.evaluateArrayObject(
                    (LateObjectEvaluator) operation.getValue(), MetadataValueRest[].class);
            submitPatchUtils.replaceValue(context, resource.getItem(), split[0], list, itemService);

        } else {
            // call with "-" or "index-based" we should receive only single
            // object member
            MetadataValueRest object =
                    (MetadataValueRest) submitPatchUtils.evaluateSingleObject(
                            (LateObjectEvaluator) operation.getValue(), MetadataValueRest.class);
            // check if is not empty
            List<MetadataValue> metadataByMetadataString = itemService.getMetadataByMetadataString(resource.getItem(),
                    split[0]);
            Assert.notEmpty(metadataByMetadataString);
            if (split.length > 1) {
                String controlChar = split[1];
                switch (controlChar) {
                    case "-":
                        submitPatchUtils.addValue(context, resource.getItem(), split[0], object, -1, itemService);
                        break;
                    default:
                        // index based

                        int index = Integer.parseInt(controlChar);
                        if (index > metadataByMetadataString.size()) {
                            throw new IllegalArgumentException(
                                    "The specified index MUST NOT be greater than the number of elements in the array");
                        }
                        submitPatchUtils.addValue(context, resource.getItem(), split[0], object, index, itemService);

                        break;
                }
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
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_ADD));
    }
}
