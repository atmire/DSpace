/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository.patch.operation;

import java.sql.SQLException;

import org.dspace.app.rest.model.MetadataValueRest;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.content.DSpaceObject;
import org.dspace.content.MetadataField;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for PATCH ADD operations on Dspace Objects' metadata
 * Usage: (can be done on other dso than Item also):
 * - ADD metadata (with schema.identifier.qualifier) value of a dso (here: Item) to end of list of md
 * <code>
 * curl -X PATCH http://${dspace.server.url}/api/core/items/<:id-item> -H "
 * Content-Type: application/json" -d '[{ "op": "add", "path": "
 * /metadata/schema.identifier.qualifier(/0|-)}", "value": "metadataValue"]'
 * </code>
 *
 * @author Maria Verdonck (Atmire) on 18/11/2019
 */
@Component
public class DSpaceObjectMetadataAddOperation<R extends DSpaceObject> extends PatchOperation<R> {

    @Autowired
    DSpaceObjectMetadataPatchUtils metadataPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException {
        DSpaceObjectService dsoService = ContentServiceFactory.getInstance().getDSpaceObjectService(resource);
        MetadataValueRest metadataValueToAdd = (MetadataValueRest)
            super.extractValuesFromOperation(operation, MetadataValueRest.class).get(0);
        MetadataField metadataField = metadataPatchUtils.getMetadataField(context, operation);
        String indexInPath = metadataPatchUtils.getIndexFromPath(operation.getPath());

        metadataPatchUtils.addValue(context, resource, dsoService, metadataField, metadataValueToAdd, indexInPath);
        return resource;
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return ((operation.getPath().startsWith(metadataPatchUtils.OPERATION_METADATA_PATH)
                || operation.getPath().equals(metadataPatchUtils.OPERATION_METADATA_PATH))
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_ADD)
                && objectToMatch instanceof DSpaceObject);
    }
}
