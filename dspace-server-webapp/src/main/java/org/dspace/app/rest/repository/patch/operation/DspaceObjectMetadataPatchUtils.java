/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.repository.patch.operation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.exception.UnprocessableEntityException;
import org.dspace.app.rest.model.MetadataValueRest;
import org.dspace.app.rest.model.patch.JsonValueEvaluator;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Util class for shared methods between the Metadata Operations
 *
 * @author Maria Verdonck (Atmire) on 18/11/2019
 */
@Component
public final class DspaceObjectMetadataPatchUtils {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MetadataFieldService metadataFieldService;

    /**
     * Path in json body of patch that uses this operation
     */
    protected static final String METADATA_PATH = "/metadata";

    private DspaceObjectMetadataPatchUtils() {
    }

    /**
     * Adds metadata to the dso (appending if index is 0 or left out, prepending if -)
     *
     * @param context       context patch is being performed in
     * @param dso           dso being patched
     * @param dsoService    service doing the patch in db
     * @param metadataField md field being patched
     * @param metadataValue value of md element
     * @param index         determines whether we're prepending (-) or appending (0) md value
     */
    public void addValue(Context context, DSpaceObject dso, DSpaceObjectService dsoService, MetadataField metadataField,
                         MetadataValueRest metadataValue, String index) {
        int indexInt = 0;
        try {
            this.checkMetadataFieldNotNull(metadataField);
            if (index != null && index.equals("-")) {
                dsoService.addMetadata(context, dso, metadataField.getMetadataSchema().getName(),
                        metadataField.getElement(), metadataField.getQualifier(), metadataValue.getLanguage(),
                        metadataValue.getValue(), metadataValue.getAuthority(), metadataValue.getConfidence());
            } else {
                dsoService.addAndShiftRightMetadata(context, dso, metadataField.getMetadataSchema().getName(),
                        metadataField.getElement(), metadataField.getQualifier(), metadataValue.getLanguage(),
                        metadataValue.getValue(), metadataValue.getAuthority(), metadataValue.getConfidence(),
                        indexInt);
            }
        } catch (SQLException e) {
            throw new DSpaceBadRequestException("SQLException in DspaceObjectMetadataPatchUtils.addVallue trying to " +
                    "add metadata to dso.", e);
        }
    }

    /**
     * Moves metadata of the dso from indexFrom to indexTo
     *
     * @param context       context patch is being performed in
     * @param dso           dso being patched
     * @param dsoService    service doing the patch in db
     * @param metadataField md field being patched
     * @param indexFrom     index we're moving metadata from
     * @param indexTo       index we're moving metadata to
     */
    protected void moveValue(Context context, DSpaceObject dso, DSpaceObjectService dsoService,
                             MetadataField metadataField, String indexFrom, String indexTo) {
        try {
            this.checkMetadataFieldNotNull(metadataField);
            dsoService.moveMetadata(context, dso, metadataField.getMetadataSchema().getName(),
                    metadataField.getElement(), metadataField.getQualifier(), Integer.parseInt(indexFrom),
                    Integer.parseInt(indexTo));
        } catch (SQLException e) {
            throw new DSpaceBadRequestException("SQLException in DspaceObjectMetadataPatchUtils.move trying to " +
                    "move metadata in dso.", e);
        }
    }

    /**
     * Removes a metadata from the dso at a given index (or all of that type if no index was given)
     *
     * @param context       context patch is being performed in
     * @param dso           dso being patched
     * @param dsoService    service doing the patch in db
     * @param metadataField md field being patched
     * @param index         index at where we want to delete metadata
     */
    protected void removeValue(Context context, DSpaceObject dso, DSpaceObjectService dsoService,
                               MetadataField metadataField, String index) {

        try {
            this.checkMetadataFieldNotNull(metadataField);
            if (index == null) {
                // remove all metadata of this type
                dsoService.clearMetadata(context, dso, metadataField.getMetadataSchema().getName(),
                        metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
            } else {
                // remove metadata at index
                List<MetadataValue> metadataValues = dsoService.getMetadata(dso,
                        metadataField.getMetadataSchema().getName(), metadataField.getElement(),
                        metadataField.getQualifier(), Item.ANY);
                int indexInt = Integer.parseInt(index);
                if (indexInt >= 0 && metadataValues.size() > indexInt
                        && metadataValues.get(indexInt) != null) {
                    // remove that metadata
                    dsoService.removeMetadataValues(context, dso,
                            Arrays.asList(metadataValues.get(indexInt)));
                } else {
                    throw new UnprocessableEntityException("UnprocessableEntityException - There is no metadata of " +
                            "this type at that index");
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("This index (" + index + ") is not valid nr", e);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new UnprocessableEntityException("There is no metadata of this type at that index");
        } catch (SQLException e) {
            throw new DSpaceBadRequestException("SQLException in DspaceObjectMetadataPatchUtils.removeValue " +
                    "trying to remove metadata from dso.", e);
        }
    }

    /**
     * Replaces metadata in the dso; 4 cases:
     *      - If we replace everything: clears all metadata
     *      - If we replace for a single field: clearMetadata on the field & add the new ones
     *      - A single existing metadata value:
     *      Retrieve the metadatavalue object & make alterations directly on this object
     *      - A single existing metadata property:
     *      Retrieve the metadatavalue object & make alterations directly on this object
     *
     * @param context           context patch is being performed in
     * @param dso               dso being patched
     * @param dsoService        service doing the patch in db
     * @param metadataField     possible md field being patched (if null all md gets cleared)
     * @param metadataValue     value of md element
     * @param index             possible index of md being replaced
     * @param propertyOfMd      possible property of md being replaced
     * @param valueMdProperty   possible new value of property of md being replaced
     */
    protected void replaceValue(Context context, DSpaceObject dso, DSpaceObjectService dsoService,
                                MetadataField metadataField, MetadataValueRest metadataValue, String index,
                                String propertyOfMd, String valueMdProperty) {

        // replace entire set of metadata
        if (metadataField == null) {
            this.replaceAllMetadata(context, dso, dsoService);
            return;
        }

        // replace all metadata for existing key
        if (index == null) {
            this.replaceMetadataFieldMetadata(context, dso, dsoService, metadataField, metadataValue);
            return;
        }
        // replace single existing metadata value
        if (propertyOfMd == null) {
            this.replaceSingleMetadataValue(dso, dsoService, metadataField, metadataValue, index);
            return;
        }
        // replace single property of exiting metadata value
        this.replaceSinglePropertyOfMdValue(dso, dsoService, metadataField, index, propertyOfMd, valueMdProperty);
    }

    /**
     * Clears all metadata of dso
     * @param context           context patch is being performed in
     * @param dso               dso being patched
     * @param dsoService        service doing the patch in db
     */
    private void replaceAllMetadata(Context context, DSpaceObject dso, DSpaceObjectService dsoService) {
        try {
            dsoService.clearMetadata(context, dso, Item.ANY, Item.ANY, Item.ANY, Item.ANY);
        } catch (SQLException e) {
            throw new DSpaceBadRequestException("SQLException in DspaceObjectMetadataOperation.replace trying to " +
                    "remove and replace metadata from dso.", e);
        }
    }

    /**
     * Replaces all metadata for an existing single mdField with new value(s)
     * @param context           context patch is being performed in
     * @param dso               dso being patched
     * @param dsoService        service doing the patch in db
     * @param metadataField     md field being patched
     * @param metadataValue     value of md element
     */
    private void replaceMetadataFieldMetadata(Context context, DSpaceObject dso, DSpaceObjectService dsoService,
                                              MetadataField metadataField, MetadataValueRest metadataValue) {
        try {
            dsoService.clearMetadata(context, dso, metadataField.getMetadataSchema().getName(),
                    metadataField.getElement(), metadataField.getQualifier(), Item.ANY);
            dsoService.addAndShiftRightMetadata(context, dso, metadataField.getMetadataSchema().getName(),
                    metadataField.getElement(), metadataField.getQualifier(), metadataValue.getLanguage(),
                    metadataValue.getValue(), metadataValue.getAuthority(), metadataValue.getConfidence(), -1);
        } catch (SQLException e) {
            throw new DSpaceBadRequestException("SQLException in DspaceObjectMetadataOperation.replace trying to " +
                    "remove and replace metadata from dso.", e);
        }
    }

    /**
     * Replaces metadata value of a single metadataValue object
     *      Retrieve the metadatavalue object & make alerations directly on this object
     * @param dso               dso being patched
     * @param dsoService        service doing the patch in db
     * @param metadataField     md field being patched
     * @param metadataValue     new value of md element
     * @param index             index of md being replaced
     */
    private void replaceSingleMetadataValue(DSpaceObject dso, DSpaceObjectService dsoService,
                                            MetadataField metadataField, MetadataValueRest metadataValue,
                                            String index) {
        try {
            List<MetadataValue> metadataValues = dsoService.getMetadata(dso,
                    metadataField.getMetadataSchema().getName(), metadataField.getElement(),
                    metadataField.getQualifier(), Item.ANY);
            int indexInt = Integer.parseInt(index);
            if (indexInt >= 0 && metadataValues.size() > indexInt
                    && metadataValues.get(indexInt) != null) {
                // Alter this existing md
                MetadataValue existingMdv = metadataValues.get(indexInt);
                existingMdv.setAuthority(metadataValue.getAuthority());
                existingMdv.setConfidence(metadataValue.getConfidence());
                existingMdv.setLanguage(metadataValue.getLanguage());
                existingMdv.setValue(metadataValue.getValue());
                dsoService.setMetadataModified(dso);
            } else {
                throw new UnprocessableEntityException("There is no metadata of this type at that index");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("This index (" + index + ") is not valid nr", e);
        }
    }

    /**
     * Replaces single property of a specific mdValue object
     * @param dso               dso being patched
     * @param dsoService        service doing the patch in db
     * @param metadataField     md field being patched
     * @param index             index of md being replaced
     * @param propertyOfMd      property of md being replaced
     * @param valueMdProperty   new value of property of md being replaced
     */
    private void replaceSinglePropertyOfMdValue(DSpaceObject dso, DSpaceObjectService dsoService,
                                                MetadataField metadataField,
                                                String index, String propertyOfMd, String valueMdProperty) {
        try {
            List<MetadataValue> metadataValues = dsoService.getMetadata(dso,
                    metadataField.getMetadataSchema().getName(), metadataField.getElement(),
                    metadataField.getQualifier(), Item.ANY);
            int indexInt = Integer.parseInt(index);
            if (indexInt >= 0 && metadataValues.size() > indexInt && metadataValues.get(indexInt) != null) {
                // Alter only asked propertyOfMd
                MetadataValue existingMdv = metadataValues.get(indexInt);
                if (propertyOfMd.equals("authority")) {
                    existingMdv.setAuthority(valueMdProperty);
                }
                if (propertyOfMd.equals("confidence")) {
                    existingMdv.setConfidence(Integer.valueOf(valueMdProperty));
                }
                if (propertyOfMd.equals("language")) {
                    existingMdv.setLanguage(valueMdProperty);
                }
                if (propertyOfMd.equals("value")) {
                    existingMdv.setValue(valueMdProperty);
                }
                dsoService.setMetadataModified(dso);
            } else {
                throw new UnprocessableEntityException("There is no metadata of this type at that index");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not all numbers are valid numbers. " +
                    "(Index and confidence should be nr)", e);
        }
    }

    /**
     * Extract metadataValue from Operation by parsing the json and mapping it to a MetadataValueRest
     *
     * @param operation Operation whose value is begin parsed
     * @return MetadataValueRest extracted from json in operation value
     */
    protected MetadataValueRest extractMetadataValueFromOperation(Operation operation) {
        MetadataValueRest metadataValue = null;
        try {
            if (operation.getValue() != null) {
                if (operation.getValue() instanceof JsonValueEvaluator) {
                    JsonNode valueNode = ((JsonValueEvaluator) operation.getValue()).getValueNode();
                    if (valueNode.isArray()) {
                        metadataValue = objectMapper.treeToValue(valueNode.get(0), MetadataValueRest.class);
                    } else {
                        metadataValue = objectMapper.treeToValue(valueNode, MetadataValueRest.class);
                    }
                }
                if (operation.getValue() instanceof String) {
                    String valueString = (String) operation.getValue();
                    metadataValue = new MetadataValueRest();
                    metadataValue.setValue(valueString);
                }
            }
        } catch (IOException e) {
            throw new DSpaceBadRequestException("IOException in " +
                    "DspaceObjectMetadataOperation.extractMetadataValueFromOperation trying to map json from " +
                    "operation.value to MetadataValue class.", e);
        }
        if (metadataValue == null) {
            throw new DSpaceBadRequestException("Could not extract MetadataValue Object from Operation");
        }
        return metadataValue;
    }

    /**
     * Extracts the mdField String (schema.element.qualifier) from the operation and returns it
     *
     * @param operation The patch operation
     * @return The mdField (schema.element.qualifier) patch is being performed on
     */
    protected String extractMdFieldStringFromOperation(Operation operation) {
        String mdElement = StringUtils.substringBetween(operation.getPath(), METADATA_PATH + "/", "/");
        if (mdElement == null) {
            mdElement = StringUtils.substringAfter(operation.getPath(), METADATA_PATH + "/");
            if (mdElement == null) {
                throw new DSpaceBadRequestException("No metadata field string found in path: " + operation.getPath());
            }
        }
        return mdElement;
    }

    /**
     * Converts a metadataValue (database entity) to a REST equivalent of it
     *
     * @param md Original metadataValue
     * @return The REST equivalent
     */
    protected MetadataValueRest convertMdValueToRest(MetadataValue md) {
        MetadataValueRest dto = new MetadataValueRest();
        dto.setAuthority(md.getAuthority());
        dto.setConfidence(md.getConfidence());
        dto.setLanguage(md.getLanguage());
        dto.setPlace(md.getPlace());
        dto.setValue(md.getValue());
        return dto;
    }

    /**
     * Extracts which property of the metadata is being changed in the replace patch operation
     *
     * @param partsOfPath Parts of the path of the operation, separated with /
     * @return The property that is begin replaced of the metadata
     */
    protected String extractPropertyOfMdFromPath(String[] partsOfPath) {
        return (partsOfPath.length > 4) ? partsOfPath[4] : null;
    }

    /**
     * Extracts the new value of the metadata from the operation for the replace patch operation
     *
     * @param operation The patch operation
     * @return The new value of the metadata being replaced in the patch operation
     */
    protected String extractNewValueOfMd(Operation operation) {
        if (operation.getValue() instanceof String) {
            return (String) operation.getValue();
        }
        return null;
    }

    /**
     * Retrieves metadataField based on the metadata element found in the operation
     *
     * @param context   Context the retrieve metadataField from service with string
     * @param operation Operation of the patch
     * @return The metadataField corresponding to the md element string of the operation
     */
    protected MetadataField getMetadataField(Context context, Operation operation) throws SQLException {
        String mdElement = this.extractMdFieldStringFromOperation(operation);
        return metadataFieldService.findByString(context, mdElement, '.');
    }

    /**
     * Retrieved the index from the path of the patch operation, if one can be found
     *
     * @param path The string from the operation
     * @return The index in the path if there is one (path ex: /metadata/dc.title/1 (1 being the index))
     */
    protected String getIndexFromPath(String path) {
        String[] partsOfPath = path.split("/");
        // Index of md being patched
        String indexInPath = (partsOfPath.length > 3) ? partsOfPath[3] : null;
        return indexInPath;
    }

    protected void checkMetadataFieldNotNull(MetadataField metadataField) {
        if (metadataField == null) {
            throw new DSpaceBadRequestException("There was no metadataField found in path of operation");
        }
    }
}
