package org.dspace.app.rest.model;

import org.dspace.app.rest.ExternalSourcesRestController;

public class ExternalSourceEntryRest extends BaseObjectRest<String> {

    public static final String NAME = "externalSourceEntry";
    public static final String PLURAL_NAME = "externalSourceEntries";
    public static final String CATEGORY = RestAddressableModel.INTEGRATION;

    @Override
    public String getCategory() {
        return CATEGORY;
    }

    @Override
    public Class getController() {
        return ExternalSourcesRestController.class;
    }

    @Override
    public String getType() {
        return NAME;
    }

    private String id;
    private String display;
    private String value;
    private String externalSource;
    private MetadataRest metadata = new MetadataRest();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExternalSource() {
        return externalSource;
    }

    public void setExternalSource(String externalSource) {
        this.externalSource = externalSource;
    }

    public MetadataRest getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataRest metadata) {
        this.metadata = metadata;
    }
}
