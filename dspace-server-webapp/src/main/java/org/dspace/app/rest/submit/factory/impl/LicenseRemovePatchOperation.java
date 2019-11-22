/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.factory.impl;

import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.factories.impl.PatchOperation;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.SQLException;

import static org.dspace.app.rest.submit.AbstractRestProcessingStep.LICENSE_STEP_OPERATION_ENTRY;

/**
 * Submission License "remove" patch operation.
 *
 * To remove a previous granted license:
 *
 * Example: <code>
 * curl -X PATCH http://${dspace.server.url}/api/submission/workspaceitems/31599 -H "Content-Type:
 * application/json" -d '[{ "op": "remove", "path": "/sections/license/granted"}]'
 * </code>
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
@Component
public class LicenseRemovePatchOperation<R extends InProgressSubmission> extends PatchOperation<R> {

    @Autowired
    ItemService itemService;

    @Autowired
    SubmitPatchUtils submitPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException, IOException, AuthorizeException {
        this.remove(context, resource);
        return resource;
    }

    /**
     * TODO
     * @param context
     * @param source
     * @throws SQLException
     * @throws IOException
     * @throws AuthorizeException
     */
    private void remove(Context context, InProgressSubmission source) throws SQLException, IOException, AuthorizeException {
        Item item = source.getItem();
        itemService.removeDSpaceLicense(context, item);
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return (submitPatchUtils.checkIfInProgressSubmissionAndStartsWithSections(objectToMatch, operation)
                && operation.getPath().endsWith(LICENSE_STEP_OPERATION_ENTRY)
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_REMOVE));
    }
}
