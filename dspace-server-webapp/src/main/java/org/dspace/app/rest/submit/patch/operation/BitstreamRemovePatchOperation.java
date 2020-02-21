/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.patch.operation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.operation.PatchOperation;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.service.BundleService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.json.patch.PatchException;
import org.springframework.stereotype.Component;

/**
 * Submission "remove" operation for deletion of the Bitstream
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class BitstreamRemovePatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    ItemService itemService;

    @Autowired
    BundleService bundleService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation)
            throws SQLException, AuthorizeException, PatchException {
        String absPath = submitPatchUtils.getAbsolutePath(operation.getPath());
        String[] split = absPath.split("/");
        int index = Integer.parseInt(split[1]);

        Item item = resource.getItem();
        List<Bundle> bbb = itemService.getBundles(item, Constants.CONTENT_BUNDLE_NAME);
        Bitstream bitstream = null;
        external:
        for (Bundle bb : bbb) {
            int idx = 0;
            for (Bitstream b : bb.getBitstreams()) {
                if (idx == index) {
                    bitstream = b;
                    break external;
                }
                idx++;
            }
        }
        // remove bitstream from bundle..
        // delete bundle if it's now empty
        List<Bundle> bundles = bitstream.getBundles();
        Bundle bundle = bundles.get(0);
        try {
            bundleService.removeBitstream(context, bundle, bitstream);
        } catch (IOException e) {
            throw new PatchException("IOException in BitstreamRemovePatchOperation.perform trying to remove " +
                    "a bitstream", e);
        }

        List<Bitstream> bitstreams = bundle.getBitstreams();

        // remove bundle if it's now empty
        if (bitstreams.size() < 1) {
            try {
                itemService.removeBundle(context, item, bundle);
            } catch (IOException e) {
                throw new PatchException("IOException in BitstreamRemovePatchOperation.perform trying to " +
                        "remove a bundle", e);
            }
        }
        return resource;
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
                && operation.getPath().contains("files")
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_REMOVE));
    }
}
