package org.dspace.external.model;

import java.util.LinkedList;
import java.util.List;

public class ExternalDataObject {
    private String source;
    private List<MockMetadataValue> metadata;
    private String displayValue;

    public ExternalDataObject() {

    }
    public ExternalDataObject(String source, List<MockMetadataValue> metadata, String displayValue) {
        this.source = source;
        this.metadata = metadata;
        this.displayValue = displayValue;
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
}
