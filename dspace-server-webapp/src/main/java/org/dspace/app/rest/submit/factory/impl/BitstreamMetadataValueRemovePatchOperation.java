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

import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.factories.impl.PatchOperation;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Submission "remove" PATCH operation at metadata Bitstream level.
 *
 * Path used to remove a <b>specific metadata value</b> using index:
 * "/sections/<:name-of-the-form>/<:metadata>/<:idx-zero-based>"
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.url}/api/submission/workspaceitems/<:id-workspaceitem> -H "
 * Content-Type: application/json" -d '[{ "op": "remove", "path": "
 * /sections/traditionalpageone/dc.title/1"}]'
 * </code>
 *
 * Path used to remove <b>all the metadata values</b> for a specific metadata key:
 * "/sections/<:name-of-the-form>/<:metadata>"
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.url}/api/submission/workspaceitems/<:id-workspaceitem> -H "
 * Content-Type: application/json" -d '[{ "op": "remove", "path": "
 * /sections/traditionalpageone/dc.title"}]'
 * </code>
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class BitstreamMetadataValueRemovePatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    BitstreamService bitstreamService;

    @Autowired
    ItemService itemService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException {
        this.remove(context, resource, operation.getPath());
        return resource;
    }

    /**
     * TODO
     * @param context
     * @param source
     * @param path
     * @throws Exception
     */
    private void remove(Context context, InProgressSubmission source, String path) throws SQLException {
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
                        submitPatchUtils.deleteValue(context, b, split[3], -1, bitstreamService);
                    } else {
                        Integer toDelete = Integer.parseInt(split[4]);
                        submitPatchUtils.deleteValue(context, b, split[3], toDelete, bitstreamService);
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
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_REMOVE));
    }
}
