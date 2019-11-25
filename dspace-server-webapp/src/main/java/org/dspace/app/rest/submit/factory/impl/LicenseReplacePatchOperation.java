/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.factory.impl;

import static org.dspace.app.rest.submit.AbstractRestProcessingStep.LICENSE_STEP_OPERATION_ENTRY;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.lang3.BooleanUtils;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.factories.impl.PatchOperation;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.LicenseUtils;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Submission "replace" patch operation
 *
 * {@link LicenseAddPatchOperation}
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class LicenseReplacePatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    ItemService itemService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation)
            throws SQLException, IOException, AuthorizeException {
        this.replace(context, resource, operation.getValue());
        return resource;
    }

    /**
     * TODO
     * @param context
     * @param source
     * @param value
     * @throws SQLException
     * @throws IOException
     * @throws AuthorizeException
     */
    private void replace(Context context, InProgressSubmission source, Object value)
            throws SQLException, IOException, AuthorizeException {
        Boolean grant = null;
        // we are friendly with the client and accept also a string representation for the boolean
        if (value instanceof String) {
            grant = BooleanUtils.toBooleanObject((String) value);
        } else {
            grant = (Boolean) value;
        }

        if (grant == null) {
            throw new IllegalArgumentException(
                "Value is not a valid boolean expression (permitted value: on/off, true/false and yes/no");
        }

        Item item = source.getItem();
        EPerson submitter = context.getCurrentUser();

        // remove any existing DSpace license (just in case the user
        // accepted it previously)
        itemService.removeDSpaceLicense(context, item);

        if (grant) {
            String license = LicenseUtils.getLicenseText(context.getCurrentLocale(), source.getCollection(), item,
                                                         submitter);

            LicenseUtils.grantLicense(context, item, license, null);
        }
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
                && operation.getPath().trim().endsWith(LICENSE_STEP_OPERATION_ENTRY)
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_REPLACE));
    }
}
