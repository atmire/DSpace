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

import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.operation.PatchOperation;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.service.ResourcePolicyService;
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
 * Submission "remove" operation to remove resource policies from the Bitstream
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class ResourcePolicyRemovePatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    ItemService itemService;

    @Autowired
    ResourcePolicyService resourcePolicyService;

    @Autowired
    BitstreamService bitstreamService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException, AuthorizeException {
        // "path" : "/sections/upload/files/0/accessConditions/0"
        // "abspath" : "/files/0/accessConditions/0"
        String[] split = submitPatchUtils.getAbsolutePath(operation.getPath()).split("/");
        String bitstreamIdx = split[1];
        Item item = resource.getItem();
        List<Bundle> bundle = itemService.getBundles(item, Constants.CONTENT_BUNDLE_NAME);
        Bitstream bitstream = null;
        for (Bundle bb : bundle) {
            int idx = 0;
            for (Bitstream b : bb.getBitstreams()) {
                if (idx == Integer.parseInt(bitstreamIdx)) {
                    if (split.length == 3) {
                        resourcePolicyService.removePolicies(context, b,
                                ResourcePolicy.TYPE_CUSTOM);
                    } else {
                        String rpIdx = split[3];
                        List<ResourcePolicy> policies = resourcePolicyService.find(context, b,
                                ResourcePolicy.TYPE_CUSTOM);
                        int index = 0;
                        for (ResourcePolicy policy : policies) {
                            Integer toDelete = Integer.parseInt(rpIdx);
                            if (index == toDelete) {
                                b.getResourcePolicies().remove(policy);
                                bitstream = b;
                                break;
                            }
                            index++;
                        }
                    }
                }
                idx++;
            }
        }
        if (bitstream != null) {
            bitstreamService.update(context, bitstream);
        }
        return resource;
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
                && operation.getPath().contains("accessConditions")
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_REMOVE));
    }
}
