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

import org.dspace.app.rest.model.patch.MoveOperation;
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
public class BitstreamMetadataValueMovePatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    BitstreamService bitstreamService;

    @Autowired
    ItemService itemService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException {
        this.move(context, resource, operation.getPath(), ((MoveOperation) operation).getFrom());
        return resource;
    }

    /**
     * TODO
     * @param context
     * @param source
     * @param path
     * @param from
     * @throws SQLException
     */
    private void move(Context context, InProgressSubmission source, String path, String from) throws SQLException {
        //"path": "/sections/upload/files/0/metadata/dc.title/2"
        //"abspath": "/files/0/metadata/dc.title/2"
        String[] splitTo = submitPatchUtils.getAbsolutePath(path).split("/");
        Item item = source.getItem();
        List<Bundle> bundle = itemService.getBundles(item, Constants.CONTENT_BUNDLE_NAME);
        for (Bundle bb : bundle) {
            int idx = 0;
            for (Bitstream b : bb.getBitstreams()) {
                if (idx == Integer.parseInt(splitTo[1])) {

                    String evalFrom = submitPatchUtils.getAbsolutePath(from);
                    String[] splitFrom = evalFrom.split("/");
                    String metadata = splitFrom[3];

                    if (splitTo.length > 4) {
                        String stringTo = splitTo[4];
                        if (splitFrom.length > 4) {
                            String stringFrom = splitFrom[4];

                            int intTo = Integer.parseInt(stringTo);
                            int intFrom = Integer.parseInt(stringFrom);
                            submitPatchUtils.moveValue(context, b, metadata, intFrom, intTo, bitstreamService);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
                && operation.getPath().contains("files")
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_MOVE));
    }
}
