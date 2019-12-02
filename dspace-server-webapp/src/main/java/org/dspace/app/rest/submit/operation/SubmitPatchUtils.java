/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.operation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.dspace.app.rest.model.MetadataValueRest;
import org.dspace.app.rest.model.patch.LateObjectEvaluator;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.app.rest.repository.patch.operation.DspaceObjectMetadataPatchUtils;
import org.dspace.content.DSpaceObject;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Utility methods used by the submission PATCH operations
 * @author Maria Verdonck (Atmire) on 22/11/2019
 */
@Component
public class SubmitPatchUtils<M extends Object> {

    private static final String SUBMIT_SECTIONS_PATH = "/sections";

    @Autowired
    DspaceObjectMetadataPatchUtils metadataPatchUtils;

    /**
     * Return part of the path needed for the patch operation (ex dc.title/2 => the md being changed and the index)
     * @param fullpath      Full path of the patch
     * @return              Absolute path of the patch, needed to perform the patch operation
     */
    public String getAbsolutePath(String fullpath) {
        String[] path = fullpath.substring(1).split("/", 3);
        String absolutePath = "";
        if (path.length > 2) {
            absolutePath = path[2];
        }
        return absolutePath;
    }

    protected void setDeclaredField(Context context, DSpaceObject source, Object value, MetadataField metadataField,
                                    String namedField, List<MetadataValue> metadataByMetadataString, String index,
                                    DSpaceObjectService dSpaceObjectService)
            throws IllegalAccessException {
        int indexInt = Integer.parseInt(index);
        // check field
        String raw = (String) value;
        for (Field field : MetadataValueRest.class.getDeclaredFields()) {
            JsonProperty jsonP = field.getDeclaredAnnotation(JsonProperty.class);
            if (jsonP != null && jsonP.access().equals(JsonProperty.Access.READ_ONLY)) {
                continue;
            } else {
                if (field.getName().equals(namedField)) {
                    int idx = 0;
                    MetadataValueRest obj = new MetadataValueRest();
                    for (MetadataValue mv : metadataByMetadataString) {

                        if (idx == indexInt) {
                            obj.setAuthority(mv.getAuthority());
                            obj.setConfidence(mv.getConfidence());
                            obj.setLanguage(mv.getLanguage());
                            obj.setValue(mv.getValue());
                            if (field.getType().isAssignableFrom(Integer.class)) {
                                obj.setConfidence(Integer.parseInt(raw));
                            } else {
                                boolean accessible = field.isAccessible();
                                field.setAccessible(true);
                                field.set(obj, raw);
                                field.setAccessible(accessible);
                            }
                            break;
                        }

                        idx++;
                    }
                    metadataPatchUtils.replaceValue(context, source, dSpaceObjectService, metadataField, obj, index,
                            null, null);
                }
            }
        }
    }

    protected List<M> evaluateArrayObject(LateObjectEvaluator value, Class arrayClassForEvaluation) {
        List<M> results = new ArrayList<M>();
        M[] list = null;
        if (value != null) {
            LateObjectEvaluator object = (LateObjectEvaluator) value;
            list = (M[]) object.evaluate(arrayClassForEvaluation);
        }

        for (M t : list) {
            results.add(t);
        }
        return results;
    }

    protected M evaluateSingleObject(LateObjectEvaluator value, Class classForEvaluation) {
        M single = null;
        if (value != null) {
            LateObjectEvaluator object = (LateObjectEvaluator) value;
            single = (M) object.evaluate(classForEvaluation);
        }
        return single;
    }

    /**
     * Checks whether the Submission PATCH operation's path starts with SUBMIT_SECTIONS_PATH and whether its being
     *      performed on a InProgressSubmission object, used by supports() method of submit patch operations
     * @param objectToMatch     Object PATCH is being performed on
     * @param operation         Operation of patch
     * @return                  True if operation.path starts with SUBMIT_SECTIONS_PATH AND objectToMatch is an
     *                              InProgressSubmission object, otherwise false
     */
    protected boolean checkIfInProgressSubmissionAndStartsWithSections(Object objectToMatch, Operation operation) {
        return (objectToMatch instanceof InProgressSubmission &&
                (operation.getPath().startsWith(SUBMIT_SECTIONS_PATH)
                        || operation.getPath().equals(SUBMIT_SECTIONS_PATH)));
    }
}
