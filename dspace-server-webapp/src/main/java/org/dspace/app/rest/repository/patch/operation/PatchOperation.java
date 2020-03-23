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
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.BooleanUtils;
import org.dspace.app.rest.exception.DSpaceBadRequestException;
import org.dspace.app.rest.model.MetadataValueRest;
import org.dspace.app.rest.model.patch.JsonValueEvaluator;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.springframework.data.rest.webmvc.json.patch.PatchException;

/**
 * Base class for all resource patch operations.
 */
public abstract class PatchOperation<M> {

    // All PATCH operations's string (in operation.getOp())
    protected static final String OPERATION_REPLACE = "replace";
    protected static final String OPERATION_ADD = "add";
    protected static final String OPERATION_COPY = "copy";
    protected static final String OPERATION_MOVE = "move";
    protected static final String OPERATION_REMOVE = "remove";

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Updates the rest model by applying the patch operation.
     *
     * @param context   context of patch operation
     * @param resource  the dso.
     * @param operation the patch operation.
     * @return the patched dso
     */
    public abstract M perform(Context context, M resource, Operation operation)
            throws SQLException, AuthorizeException, PatchException;

    /**
     * Throws PatchBadRequestException for missing operation value.
     *
     * @param value
     *            the value to test
     */
    public void checkOperationValue(Object value) {
        if (value == null) {
            throw new DSpaceBadRequestException("No value provided for the operation.");
        }
        if (value instanceof String && (((String) value).trim().isBlank())) {
            throw new DSpaceBadRequestException("Value can't be empty or just spaces.");
        }
    }

    /**
     * Allows clients to use either a boolean or a string representation of boolean value.
     *
     * @param value the operation value
     * @return the original or derived boolean value
     * @throws DSpaceBadRequestException
     */
    Boolean getBooleanOperationValue(Object value) {
        Boolean bool;

        if (value instanceof String) {
            bool = BooleanUtils.toBooleanObject((String) value);
            if (bool == null) {
                // make sure the string was converted to boolean.
                throw new DSpaceBadRequestException("Boolean value not provided.");
            }
        } else {
            bool = (Boolean) value;
        }
        return bool;
    }

    /**
     * Determines whether or not this Patch Operation can do this patch (Object of operation and path gets checked)
     * @param objectToMatch    Object whose class must be instance of type object
     *                              for which this PatchOperation was created
     * @param operation        Operation of the patch, should match this type of Patch Operation
     * @return                 True if this PatchOperation class can do the patch for this given dso type and Path
     */
    public abstract boolean supports(Object objectToMatch, Operation operation);

    /**
     * Extract multiple values from Operation by parsing the json and mapping it to a List<classForEvaluation>
     *
     * @param operation             Operation whose value is begin parsed
     * @param classForEvaluation    Class to where the operation values are mapped
     * @return List    List of objects extracted from json in operation value
     */
    public List extractValuesFromOperation(Operation operation, Class classForEvaluation) {
        List<M> values = new ArrayList<>();
        try {
            if (operation.getValue() != null) {
                if (operation.getValue() instanceof JsonValueEvaluator) {
                    JsonNode valueNode = ((JsonValueEvaluator) operation.getValue()).getValueNode();
                    if (valueNode.isArray()) {
                        for (final JsonNode objNode : valueNode) {
                            values.add(objectMapper.treeToValue(objNode, (Class<M>) classForEvaluation));
                        }
                    } else {
                        values.add(objectMapper.treeToValue(valueNode, (Class<M>) classForEvaluation));
                    }
                }
                if (operation.getValue() instanceof String) {
                    String valueString = (String) operation.getValue();
                    values.add((M) classForEvaluation.newInstance());
                    if (classForEvaluation.equals(MetadataValueRest.class)) {
                        ((MetadataValueRest) values.get(0)).setValue(valueString);
                    }
                }
            }
        } catch (IOException e) {
            throw new DSpaceBadRequestException(
                "IOException in PatchOperation.extractValuesFromOperation " +
                "trying to map json from operation.value to List<MetadataValue> class.", e);
        } catch (IllegalAccessException e) {
            throw new DSpaceBadRequestException(
                "IllegalAccessException in PatchOperation.extractValuesFromOperation " +
                "trying to map json from operation.value to List<" + classForEvaluation + "> class.", e);
        } catch (InstantiationException e) {
            throw new DSpaceBadRequestException(
                "InstantiationException in PatchOperation.extractValuesFromOperation " +
                "trying to map json from operation.value to List<" + classForEvaluation + "> class.", e);
        }
        if (values == null || values.isEmpty()) {
            throw new DSpaceBadRequestException("Could not extract " + classForEvaluation + " Objects from Operation");
        }
        return values;
    }

}
