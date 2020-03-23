/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.patch.operation;

import java.sql.SQLException;
import java.util.List;

import org.dspace.app.rest.model.MetadataValueRest;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.operation.DSpaceObjectMetadataPatchUtils;
import org.dspace.app.rest.repository.patch.operation.PatchOperation;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Submission "replace" operation to replace metadata in the Bitstream
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class BitstreamMetadataValueReplacePatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    BitstreamService bitstreamService;

    @Autowired
    ItemService itemService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Autowired
    DSpaceObjectMetadataPatchUtils metadataPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException {
        //"path": "/sections/upload/files/0/metadata/dc.title/2"
        //"abspath": "/files/0/metadata/dc.title/2"
        String[] split = submitPatchUtils.getAbsolutePath(operation.getPath()).split("/");
        Item item = resource.getItem();
        List<Bundle> bundle = itemService.getBundles(item, Constants.CONTENT_BUNDLE_NAME);
        for (Bundle bb : bundle) {
            int idx = 0;
            for (Bitstream b : bb.getBitstreams()) {
                if (idx == Integer.parseInt(split[1])) {
                    replace(context, b, split, operation);
                }
                idx++;
            }
        }
        return resource;
    }

    /**
     * Replace md at index determined by last section of path on this specific given bitstream
     * @param context       Context of patch
     * @param bitstream     Bitstream whose md is being replaced
     * @param split         All sections of the operation path (/ seperator)
     * @param operation     Patch operation, containing the value the md is being replaced with
     * @throws SQLException if db error
     */
    private void replace(Context context, Bitstream bitstream, String[] split, Operation operation)
            throws SQLException {
        String mdString = split[3];
        List<MetadataValue> metadataByMetadataString
                = bitstreamService.getMetadataByMetadataString(bitstream, mdString);
        Assert.notEmpty(metadataByMetadataString);

        String indexString = split[4];
        // if split size is one so we have a call to initialize or replace
        MetadataField metadataField = metadataPatchUtils.getMetadataField(context, mdString);
        if (split.length == 5) {
            MetadataValueRest obj = (MetadataValueRest)
                super.extractValuesFromOperation(operation, MetadataValueRest.class).get(0);
            metadataPatchUtils.replaceValue(context, bitstream, bitstreamService, metadataField, obj, indexString,
                    null, null);
        } else {
            //"path": "/sections/upload/files/0/metadata/dc.title/2/language"
            if (split.length > 5) {
                submitPatchUtils.setDeclaredField(context, bitstream, operation.getValue(), metadataField, split[5],
                        metadataByMetadataString, indexString, bitstreamService);
            }
        }
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
                && operation.getPath().contains("files")
                && operation.getPath().contains("metadata")
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_REPLACE));
    }
}
