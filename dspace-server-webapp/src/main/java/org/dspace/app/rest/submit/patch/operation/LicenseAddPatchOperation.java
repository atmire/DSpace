/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.patch.operation;

import static org.dspace.app.rest.submit.AbstractRestProcessingStep.LICENSE_STEP_OPERATION_ENTRY;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.lang3.BooleanUtils;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.operation.PatchOperation;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.LicenseUtils;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.json.patch.PatchException;
import org.springframework.stereotype.Component;

/**
 * Submission "add" PATCH operation
 *
 * To accept/reject the license.
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.server.url}/api/submission/workspaceitems/31599 -H "Content-Type:
 * application/json" -d '[{ "op": "add", "path": "/sections/license/granted", "value":"true"}]'
 * </code>
 *
 * Please note that according to the JSON Patch specification RFC6902 a
 * subsequent add operation on the "granted" path will have the effect to
 * replace the previous granted license with a new one.
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class LicenseAddPatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    ItemService itemService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException, AuthorizeException {
        Boolean grant = null;
        // we are friendly with the client and accept also a string representation for the boolean
        if (operation.getValue() instanceof String) {
            grant = BooleanUtils.toBooleanObject((String) operation.getValue());
        } else {
            grant = (Boolean) operation.getValue();
        }
        if (grant == null) {
            throw new IllegalArgumentException(
                    "Value is not a valid boolean expression (permitted value: on/off, true/false and yes/no");
        }
        Item item = resource.getItem();
        EPerson submitter = context.getCurrentUser();
        // remove any existing DSpace license (just in case the user accepted it previously)
        try {
            itemService.removeDSpaceLicense(context, item);
        } catch (IOException e) {
            throw new PatchException("IOException in LicenseAddPatchOperation#perform trying to remove the DSpace " +
                    "license from an item", e);
        }
        if (grant) {
            String license = LicenseUtils.getLicenseText(context.getCurrentLocale(), resource.getCollection(), item,
                    submitter);
            try {
                LicenseUtils.grantLicense(context, item, license, null);
            } catch (IOException e) {
                throw new PatchException("IOException in LicenseAddPatchOperation#perform trying to gran a license " +
                        "to an item", e);
            }
        }
        return resource;
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
                && operation.getPath().endsWith(LICENSE_STEP_OPERATION_ENTRY)
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_ADD));
    }
}