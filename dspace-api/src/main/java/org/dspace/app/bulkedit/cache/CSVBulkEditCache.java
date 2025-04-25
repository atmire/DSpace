/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.bulkedit.cache;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

import org.dspace.app.bulkedit.DSpaceCSV;
import org.dspace.app.bulkedit.DSpaceCSVLine;
import org.dspace.app.bulkedit.MetadataImportException;
import org.dspace.core.Context;

public interface CSVBulkEditCache {
    void resetCache();
    void populateMetadataReferenceMap(Map<String, List<String>> values, UUID uuid);
    void populateEntityRelationMap(String refUUID, String relationField, String originId);
    UUID resolveEntityRef(Context context, String reference) throws MetadataImportException;
    void populateReferenceMaps(DSpaceCSVLine line, Integer rowNumber, UUID uuid);
    Integer getRowCount();
    void setRowCount(Integer rowCount);
    void increaseRowCount();
    void resetRowCount();
    UUID evaluateOriginId(@Nullable UUID originId);
    void validateExpressedRelations(Context c, DSpaceCSV csv) throws MetadataImportException;
    boolean isAuthorityControlledField(String field, String separator);
    boolean isAuthorityControlledField(String field);
    String getCleanMdField(String field, String originalSeparator, String newSeparator);
}
