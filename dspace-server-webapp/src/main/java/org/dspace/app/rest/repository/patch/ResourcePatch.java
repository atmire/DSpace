/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository.patch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.factories.impl.PatchOperation;
import org.dspace.app.util.DCInputsReaderException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The base class for resource PATCH operations.
 */
@Component
public class ResourcePatch<M> {

    @Autowired
    private List<PatchOperation> patchOperations;

    /**
     * Handles the patch operations. Patch implementations are provided by subclasses.
     * The default methods throw an UnprocessableEntityException.
     *
     * @param context       Context of patch operation
     * @param dso           the dso resource to patch
     * @param operations    list of patch operations
     * @throws UnprocessableEntityException
     * @throws DSpaceBadRequestException
     */
    public void patch(Context context, M dso, List<Operation> operations) throws SQLException, IllegalAccessException, IOException, AuthorizeException, DCInputsReaderException {
        for (Operation operation: operations) {
            performPatchOperation(context, dso, operation);
        }
    }

    /**
     * Checks with all possible patch operations whether they support this operation
     *      (based on instanceof restModel and operation.path
     * @param context       Context of patch operation
     * @param dso           the dso resource to patch
     * @param operation     the patch operation
     * @throws DSpaceBadRequestException
     */
    protected void performPatchOperation(Context context, M dso, Operation operation) throws SQLException, DCInputsReaderException, IOException, AuthorizeException, IllegalAccessException {
        for (PatchOperation patchOperation: patchOperations) {
            if (patchOperation.supports(dso, operation)) {
                patchOperation.perform(context, dso, operation);
                return;
            }
        }
        throw new DSpaceBadRequestException(
                "This operation is not supported."
        );
    }

}
