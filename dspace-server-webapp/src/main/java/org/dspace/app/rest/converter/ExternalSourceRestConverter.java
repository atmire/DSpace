package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.ExternalSourceRest;
import org.dspace.external.provider.ExternalDataProvider;
import org.springframework.stereotype.Component;

@Component
public class ExternalSourceRestConverter implements DSpaceConverter<ExternalDataProvider, ExternalSourceRest> {

    public ExternalSourceRest fromModel(ExternalDataProvider obj) {
        ExternalSourceRest externalSourceRest = new ExternalSourceRest();
        externalSourceRest.setId(obj.getSourceIdentifier());
        externalSourceRest.setName(obj.getSourceIdentifier());
        externalSourceRest.setHierarchical(false);
        return externalSourceRest;
    }

    public ExternalDataProvider toModel(ExternalSourceRest obj) {
        return null;
    }
}
