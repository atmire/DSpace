/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository.patch.operation;

import java.sql.SQLException;

import org.dspace.app.rest.model.patch.MoveOperation;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.content.DSpaceObject;
import org.dspace.content.MetadataField;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class for PATCH MOVE operations on Dspace Objects' metadata
 * Usage: (can be done on other dso than Item also):
 * - MOVE metadata (with schema.identifier.qualifier) value of a dso (here: Item)
 * from given index in from to given index in path
 * <code>
 * curl -X PATCH http://${dspace.url}/api/core/items/<:id-item> -H "
 * Content-Type: application/json" -d '[{ "op": "move",
 * "from": "/metadata/schema.identifier.qualifier/indexToCopyFrom"
 * "path": "/metadata/schema.identifier.qualifier/indexToCopyTo"}]'
 * </code>
 *
 * @author Maria Verdonck (Atmire) on 18/11/2019
 */
@Component
public class DspaceObjectMetadataMoveOperation<R extends DSpaceObject> extends PatchOperation<R> {

    @Autowired
    DspaceObjectMetadataPatchUtils metadataPatchUtils;

    @Override
    public R perform(Context context, R resource, Operation operation) throws SQLException {
        DSpaceObjectService dsoService = ContentServiceFactory.getInstance().getDSpaceObjectService(resource);
        MetadataField metadataField = metadataPatchUtils.getMetadataField(context, operation);
        String indexInPath = metadataPatchUtils.getIndexFromPath(operation.getPath());
        String indexToMoveFrom = metadataPatchUtils.getIndexFromPath(((MoveOperation) operation).getFrom());

        metadataPatchUtils.moveValue(context, resource, dsoService, metadataField, indexInPath, indexToMoveFrom);
        return resource;
    }

    @Override
    public boolean supports(Object objectToMatch, Operation operation) {
        return ((operation.getPath().startsWith(metadataPatchUtils.METADATA_PATH)
                || operation.getPath().equals(metadataPatchUtils.METADATA_PATH))
                && operation.getOp().trim().equalsIgnoreCase(OPERATION_MOVE)
                && objectToMatch instanceof DSpaceObject);
    }
}
