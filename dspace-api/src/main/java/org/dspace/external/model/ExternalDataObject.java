package org.dspace.external.model;

import java.util.LinkedList;
import java.util.List;

import org.dspace.mock.MockMetadataValue;

public class ExternalDataObject {

    private String id;
    private String value;
    private String source;
    private List<MockMetadataValue> metadata;
    private String displayValue;

    public ExternalDataObject() {

    }
    public ExternalDataObject(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public List<MockMetadataValue> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<MockMetadataValue> metadata) {
        this.metadata = metadata;
    }

    public void addMetadata(MockMetadataValue mockMetadataValue) {
        if (metadata == null) {
            metadata = new LinkedList<>();
        }
        metadata.add(mockMetadataValue);
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
