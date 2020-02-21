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
 Class for PATCH REPLACE operations on Dspace Objects' metadata
 * Usage: (can be done on other dso than Item also):
 * - REPLACE metadata (with schema.identifier.qualifier) value of a dso (here: Item)
 *      from existing value to new given value
 * <code>
 * curl -X PATCH http://${dspace.server.url}/api/core/items/<:id-item> -H "
 * Content-Type: application/json" -d '[{ "op": "replace", "path": "
 * /metadata/schema.identifier.qualifier}", "value": "newMetadataValue"]'
 * </code>
 * @author Maria Verdonck (Atmire) on 18/11/2019
 */
@Component
public class DSpaceObjectMetadataReplaceOperation<R extends DSpaceObject> extends PatchOperation<R> {

    @Autowired
    DSpaceObjectMetadataPatchUtils metadataPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException {
        DSpaceObjectService dsoService = ContentServiceFactory.getInstance().getDSpaceObjectService(resource);
        MetadataField metadataField = metadataPatchUtils.getMetadataField(context, operation);
        String[] partsOfPath = operation.getPath().split("/");
        // Index of md being patched
        String indexInPath = (partsOfPath.length > 3) ? partsOfPath[3] : null;
        MetadataValueRest metadataValueToReplace = metadataPatchUtils.extractMetadataValueFromOperation(operation);
        // Property of md being altered
        String propertyOfMd = metadataPatchUtils.extractPropertyOfMdFromPath(partsOfPath);
        String newValueMdAttribute = metadataPatchUtils.extractNewValueOfMd(operation);

        metadataPatchUtils.replaceValue(context, resource, dsoService, metadataField, metadataValueToReplace,
                indexInPath, propertyOfMd, newValueMdAttribute);
        return resource;
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return ((operation.getPath().startsWith(metadataPatchUtils.OPERATION_METADATA_PATH)
                || operation.getPath().equals(metadataPatchUtils.OPERATION_METADATA_PATH))
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_REPLACE)
                && objectToMatch instanceof DSpaceObject);
    }
}
