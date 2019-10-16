/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.mock;

import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.content.MetadataValue;
import org.dspace.content.authority.Choices;

/**
 * Metadata Value is bound to a database, the dissemination crosswalk require mock metadata just need for desimanation
 * This class provides a wrapper for this.
 * This class should only be used for the dissemniation metadata values that aren't to be written to the database
 *
 * @author kevinvandevelde at atmire.com
 */
public class MockMetadataValue {

    private String schema;
    private String element;
    private String qualifier;
    private String language;
    private String value;
    private String authority;
    private int confidence = Choices.CF_UNSET;

    public MockMetadataValue(MetadataValue metadataValue) {
        MetadataField metadataField = metadataValue.getMetadataField();
        MetadataSchema metadataSchema = metadataField.getMetadataSchema();
        schema = metadataSchema.getName();
        element = metadataField.getElement();
        qualifier = metadataField.getQualifier();
        language = metadataValue.getLanguage();
        value = metadataValue.getValue();
        authority = metadataValue.getAuthority();
        confidence = metadataValue.getConfidence();
    }

    public MockMetadataValue() {
    }

    /**
     * Constructor for the MockMetadataValue class
     * @param schema        The schema to be assigned to this MockMetadataValue object
     * @param element       The element to be assigned to this MockMetadataValue object
     * @param qualifier     The qualifier to be assigned to this MockMetadataValue object
     * @param language      The language to be assigend to this MockMetadataValue object
     * @param value         The value to be assigned to this MockMetadataValue object
     * @param authority     The authority to be assigned to this MockMetadataValue object
     * @param confidence    The confidence to be assigned to this MockMetadataValue object
     */
    public MockMetadataValue(String schema, String element, String qualifier, String language, String value,
                             String authority, int confidence) {
        this.schema = schema;
        this.element = element;
        this.qualifier = qualifier;
        this.language = language;
        this.value = value;
        this.authority = authority;
        this.confidence = confidence;
    }
    /**
     * Constructor for the MockMetadataValue class
     * @param schema        The schema to be assigned to this MockMetadataValue object
     * @param element       The element to be assigned to this MockMetadataValue object
     * @param qualifier     The qualifier to be assigned to this MockMetadataValue object
     * @param language      The language to be assigend to this MockMetadataValue object
     * @param value         The value to be assigned to this MockMetadataValue object
     */
    public MockMetadataValue(String schema, String element, String qualifier, String language, String value) {
        this.schema = schema;
        this.element = element;
        this.qualifier = qualifier;
        this.language = language;
        this.value = value;
    }

    public MockMetadataValue(String metadataField, String value) {
        String[] metadataFieldParts = metadataField.split("_");
        this.schema = metadataFieldParts[0];
        this.element = metadataFieldParts[1];
        if (metadataFieldParts.length > 2) {
            this.qualifier = metadataFieldParts[2];
        }
        if (metadataFieldParts.length > 3) {
            this.language = metadataFieldParts[3];
        }
        this.value = value;
    }

    public String getSchema() {
        return schema;
    }

    public String getElement() {
        return element;
    }

    public String getQualifier() {
        return qualifier;
    }

    public String getValue() {
        return value;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    /**
     * This method will parse the schema.element.qualifier into a string with the given separator. It will not include
     * the qualifier incase this is null
     * @param separator     The separator to be used
     * @return              The parsed metadataField String
     */
    public String getFieldString(char separator) {
        if (qualifier == null) {
            return schema + separator + element;
        } else {
            return schema + separator + element + separator + qualifier;
        }
    }
}
