/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.factory.impl;

import java.sql.SQLException;
import java.util.List;

import org.dspace.app.rest.model.MetadataValueRest;
import org.dspace.app.rest.model.patch.LateObjectEvaluator;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.factories.impl.PatchOperation;
import org.dspace.content.*;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Submission "add" PATCH operation at metadata Bitstream level.
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
public class BitstreamMetadataValueAddPatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    BitstreamService bitstreamService;

    @Autowired
    ItemService itemService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException {
        this.add(context, resource, operation.getPath(), operation.getValue());
        return resource;
    }

    /**
     * TODO
     *
     * @param context
     * @param source
     * @param path
     * @param value
     * @throws Exception
     */
    private void add(Context context, InProgressSubmission source, String path, Object value) throws SQLException {
        //"path": "/sections/upload/files/0/metadata/dc.title/2"
        //"abspath": "/files/0/metadata/dc.title/2"
        String[] split = submitPatchUtils.getAbsolutePath(path).split("/");
        Item item = source.getItem();
        List<Bundle> bundle = itemService.getBundles(item, Constants.CONTENT_BUNDLE_NAME);
        for (Bundle bb : bundle) {
            int idx = 0;
            for (Bitstream b : bb.getBitstreams()) {
                if (idx == Integer.parseInt(split[1])) {

                    if (split.length == 4) {
                        List<MetadataValueRest> list = submitPatchUtils.evaluateArrayObject((LateObjectEvaluator) value, MetadataValueRest[].class);
                        submitPatchUtils.replaceValue(context, b, split[3], list, bitstreamService);

                    } else {
                        // call with "-" or "index-based" we should receive only single
                        // object member
                        MetadataValueRest object = (MetadataValueRest) submitPatchUtils.evaluateSingleObject((LateObjectEvaluator) value, MetadataValueRest.class);
                        // check if is not empty
                        List<MetadataValue> metadataByMetadataString =
                                bitstreamService.getMetadataByMetadataString(b, split[3]);
                        Assert.notEmpty(metadataByMetadataString);
                        if (split.length > 4) {
                            String controlChar = split[4];
                            switch (controlChar) {
                                case "-":
                                    submitPatchUtils.addValue(context, b, split[3], object, -1, bitstreamService);
                                    break;
                                default:
                                    // index based

                                    int index = Integer.parseInt(controlChar);
                                    if (index > metadataByMetadataString.size()) {
                                        throw new IllegalArgumentException(
                                                "The specified index MUST NOT be greater than the number of elements in " +
                                                        "the array");
                                    }
                                    submitPatchUtils.addValue(context, b, split[3], object, index, bitstreamService);
                                    break;
                            }
                        }
                    }
                }
                idx++;
            }
        }
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
                && operation.getPath().contains("files")
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_ADD));
    }
}
