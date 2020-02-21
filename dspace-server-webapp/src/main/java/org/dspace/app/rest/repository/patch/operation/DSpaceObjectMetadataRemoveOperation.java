/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository.patch.operation;

import java.sql.SQLException;

import org.dspace.app.rest.model.patch.Operation;
import org.dspace.content.DSpaceObject;
import org.dspace.content.MetadataField;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for PATCH REMOVE operations on Dspace Objects' metadata
 * Usage: (can be done on other dso than Item also):
 * - REMOVE metadata (with schema.identifier.qualifier) value of a dso (here: Item)
 * > Without index: removes all md values of that schema.identifier.qualifier type
 * > With index: removes only that select md value
 * <code>
 * curl -X PATCH http://${dspace.server.url}/api/core/items/<:id-item> -H "
 * Content-Type: application/json" -d '[{ "op": "remove",
 * "path": "/metadata/schema.identifier.qualifier(/indexOfSpecificMdToRemove)"}]'
 * </code>
 *
 * @author Maria Verdonck (Atmire) on 18/11/2019
 */
@Component
public class DSpaceObjectMetadataRemoveOperation<R extends DSpaceObject> extends PatchOperation<R> {

    @Autowired
    DSpaceObjectMetadataPatchUtils metadataPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException {
        DSpaceObjectService dsoService = ContentServiceFactory.getInstance().getDSpaceObjectService(resource);
        String indexInPath = metadataPatchUtils.getIndexFromPath(operation.getPath());
        MetadataField metadataField = metadataPatchUtils.getMetadataField(context, operation);

        metadataPatchUtils.removeValue(context, resource, dsoService, metadataField, indexInPath);
        return resource;
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return ((operation.getPath().startsWith(metadataPatchUtils.OPERATION_METADATA_PATH)
                || operation.getPath().equals(metadataPatchUtils.OPERATION_METADATA_PATH))
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_REMOVE)
                && objectToMatch instanceof DSpaceObject);
    }
}
