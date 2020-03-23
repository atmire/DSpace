/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.patch.operation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dspace.app.rest.model.ResourcePolicyRest;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.operation.PatchOperation;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.ResourcePolicy;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.ItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Submission "add" operation to add resource policies in the Bitstream
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class ResourcePolicyAddPatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    BitstreamService bitstreamService;

    @Autowired
    ItemService itemService;

    @Autowired
    AuthorizeService authorizeService;

    @Autowired
    GroupService groupService;
    @Autowired
    EPersonService epersonService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException, AuthorizeException {
        //"path": "/sections/upload/files/0/accessConditions"
        String[] split = submitPatchUtils.getAbsolutePath(operation.getPath()).split("/");
        Item item = resource.getItem();
        List<Bundle> bundle = itemService.getBundles(item, Constants.CONTENT_BUNDLE_NAME);
        for (Bundle bb : bundle) {
            int idx = 0;
            for (Bitstream b : bb.getBitstreams()) {
                if (idx == Integer.parseInt(split[1])) {

                    List<ResourcePolicyRest> newAccessConditions = new ArrayList<ResourcePolicyRest>();
                    if (split.length == 3) {
                        authorizeService.removePoliciesActionFilter(context, b, Constants.READ);
                        newAccessConditions = (List<ResourcePolicyRest>)
                            super.extractValuesFromOperation(operation, ResourcePolicyRest.class);
                    } else if (split.length == 4) {
                        // contains "-", call index-based accessConditions it make not sense
                        newAccessConditions.add((ResourcePolicyRest)
                            super.extractValuesFromOperation(operation, ResourcePolicyRest.class).get(0));
                    }
                    for (ResourcePolicyRest newAccessCondition : newAccessConditions) {
                        String name = newAccessCondition.getName();
                        String description = newAccessCondition.getDescription();

                        //TODO manage error on select group and eperson
                        Group group = null;
                        if (newAccessCondition.getGroupUUID() != null) {
                            group = groupService.find(context, newAccessCondition.getGroupUUID());
                        }
                        EPerson eperson = null;
                        if (newAccessCondition.getEpersonUUID() != null) {
                            eperson = epersonService.find(context, newAccessCondition.getEpersonUUID());
                        }

                        Date startDate = newAccessCondition.getStartDate();
                        Date endDate = newAccessCondition.getEndDate();
                        authorizeService.createResourcePolicy(context, b, group, eperson, Constants.READ,
                                ResourcePolicy.TYPE_CUSTOM, name, description, startDate,
                                endDate);
                        // TODO manage duplicate policy
                    }
                }
                idx++;
            }
        }
        return resource;
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
                && operation.getPath().contains("files")
                && operation.getPath().contains("accessConditions")
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_ADD));
    }
}
