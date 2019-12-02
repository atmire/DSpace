/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.operation;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.dspace.app.rest.model.MetadataValueRest;
import org.dspace.app.rest.model.patch.LateObjectEvaluator;
import org.dspace.app.rest.model.patch.Operation;
import org.dspace.content.DSpaceObject;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.service.DSpaceObjectService;
import org.dspace.core.Context;
import org.dspace.core.Utils;
import org.springframework.stereotype.Component;

/**
 * Utility methods used by the submission PATCH operations
 * @author Maria Verdonck (Atmire) on 22/11/2019
 */
@Component
public class SubmitPatchUtils<M extends Object> {

    private static final String SUBMIT_SECTIONS_PATH = "/sections";

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

    protected void replaceValue(Context context, DSpaceObject source, String target, List<MetadataValueRest> list,
                             DSpaceObjectService dSpaceObjectService)
            throws SQLException {
        String[] metadata = Utils.tokenize(target);

        dSpaceObjectService.clearMetadata(context, source, metadata[0], metadata[1], metadata[2], Item.ANY);
        for (MetadataValueRest ll : list) {
            dSpaceObjectService
                    .addMetadata(context, source, metadata[0], metadata[1], metadata[2], ll.getLanguage(),
                            ll.getValue(), ll.getAuthority(), ll.getConfidence());
        }
    }

    protected void replaceValue(Context context, DSpaceObject source, String target, MetadataValueRest object,
                                int index, DSpaceObjectService dSpaceObjectService)
            throws SQLException {
        String[] metadata = Utils.tokenize(target);
        dSpaceObjectService.replaceMetadata(context, source, metadata[0], metadata[1], metadata[2],
                object.getLanguage(), object.getValue(), object.getAuthority(),
                object.getConfidence(), index);
    }

    protected void addValue(Context context, DSpaceObject source, String target, MetadataValueRest object, int index,
                         DSpaceObjectService dSpaceObjectService)
            throws SQLException {
        String[] metadata = Utils.tokenize(target);
        if (index == -1) {
            dSpaceObjectService.addMetadata(context, source, metadata[0], metadata[1], metadata[2],
                    object.getLanguage(), object.getValue(), object.getAuthority(),
                    object.getConfidence());
        } else {
            dSpaceObjectService.addAndShiftRightMetadata(context, source, metadata[0], metadata[1], metadata[2],
                    object.getLanguage(), object.getValue(),
                    object.getAuthority(), object.getConfidence(), index);
        }
    }

    protected void moveValue(Context context, DSpaceObject source, String target, int from, int to,
                             DSpaceObjectService dSpaceObjectService) throws SQLException {
        String[] metadata = Utils.tokenize(target);
        dSpaceObjectService.moveMetadata(context, source, metadata[0], metadata[1], metadata[2],
                from, to);
    }

    protected void deleteValue(Context context, DSpaceObject source, String target, int index,
                               DSpaceObjectService dSpaceObjectService) throws SQLException {
        String[] metadata = Utils.tokenize(target);
        List<MetadataValue> mm = dSpaceObjectService.getMetadata(source, metadata[0], metadata[1], metadata[2],
                Item.ANY);
        dSpaceObjectService.clearMetadata(context, source, metadata[0], metadata[1], metadata[2], Item.ANY);
        if (index != -1) {
            int idx = 0;
            for (MetadataValue m : mm) {
                if (idx != index) {
                    dSpaceObjectService.addMetadata(context, source, metadata[0], metadata[1], metadata[2],
                            m.getLanguage(), m.getValue(), m.getAuthority(),
                            m.getConfidence());
                }
                idx++;
            }
        }
    }

    protected void setDeclaredField(Context context, DSpaceObject source, Object value, String metadata,
                                    String namedField, List<MetadataValue> metadataByMetadataString, int index,
                                    DSpaceObjectService dSpaceObjectService)
            throws IllegalAccessException, SQLException {
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

                        if (idx == index) {
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
                    replaceValue(context, source, metadata, obj, index, dSpaceObjectService);
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
