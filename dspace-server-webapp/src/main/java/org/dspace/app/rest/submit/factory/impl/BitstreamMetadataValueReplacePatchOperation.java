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

import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.model.MetadataValueRest;
import org.dspace.app.rest.model.patch.LateObjectEvaluator;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.factories.impl.PatchOperation;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
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

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException, IllegalAccessException {
        this.replace(context, resource, operation.getPath(), operation.getValue());
        return resource;
    }

    /**
     * TODO
     * @param context
     * @param source
     * @param path
     * @param value
     * @throws SQLException
     * @throws IllegalAccessException
     */
    private void replace(Context context, InProgressSubmission source, String path, Object value)
            throws SQLException, IllegalAccessException {
        //"path": "/sections/upload/files/0/metadata/dc.title/2"
        //"abspath": "/files/0/metadata/dc.title/2"
        String[] split = submitPatchUtils.getAbsolutePath(path).split("/");
        Item item = source.getItem();
        List<Bundle> bundle = itemService.getBundles(item, Constants.CONTENT_BUNDLE_NAME);
        for (Bundle bb : bundle) {
            int idx = 0;
            for (Bitstream b : bb.getBitstreams()) {
                if (idx == Integer.parseInt(split[1])) {
                    replace(context, b, split, value);
                }
                idx++;
            }
        }
    }

    /**
     * TODO
     * @param context
     * @param bitstream
     * @param split
     * @param value
     * @throws SQLException
     * @throws IllegalAccessException
     */
    private void replace(Context context, Bitstream bitstream, String[] split, Object value)
            throws SQLException, IllegalAccessException {
        String mdString = split[3];
        List<MetadataValue> metadataByMetadataString = bitstreamService.getMetadataByMetadataString(bitstream, mdString);
        Assert.notEmpty(metadataByMetadataString);

        int index = Integer.parseInt(split[4]);
        // if split size is one so we have a call to initialize or replace
        if (split.length == 5) {
            MetadataValueRest obj = (MetadataValueRest) submitPatchUtils.evaluateSingleObject((LateObjectEvaluator) value, MetadataValueRest.class);
            submitPatchUtils.replaceValue(context, bitstream, mdString, obj, index, bitstreamService);
        } else {
            //"path": "/sections/upload/files/0/metadata/dc.title/2/language"
            if (split.length > 5) {
                submitPatchUtils.setDeclaredField(context, bitstream, value, mdString, split[5], metadataByMetadataString, index, bitstreamService);
            }
        }
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
                && operation.getPath().contains("files")
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_REPLACE));
    }
}
