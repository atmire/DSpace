package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.ExternalSourceEntryRest;
import org.dspace.external.model.ExternalDataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExternalSourceEntryRestConverter implements DSpaceConverter<ExternalDataObject, ExternalSourceEntryRest> {

    @Autowired
    private MockMetadataConverter metadataConverter;

    public ExternalSourceEntryRest fromModel(ExternalDataObject externalDataObject) {
        ExternalSourceEntryRest externalSourceEntryRest = new ExternalSourceEntryRest();
        externalSourceEntryRest.setId(externalDataObject.getId());
        externalSourceEntryRest.setDisplay(externalDataObject.getDisplayValue());
        externalSourceEntryRest.setValue(externalDataObject.getValue());
        externalSourceEntryRest.setExternalSource(externalDataObject.getSource());
        externalSourceEntryRest.setMetadata(metadataConverter.convert(externalDataObject.getMetadata()));
        return externalSourceEntryRest;
    }

    public ExternalDataObject toModel(ExternalSourceEntryRest obj) {
        return null;
    }
}
