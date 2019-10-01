package org.dspace.external.provider;

import java.util.List;

import org.dspace.external.model.ExternalDataObject;

public interface ExternalDataProvider {

    //TODO This name is confusing when paired with the externalDataService#getExternalDataObject parameters
    public String getSourceIdentifier();

    ExternalDataObject getExternalDataObject(String id);
    List<ExternalDataObject> searchExternalDataObjects(String query, int start, int limit);
    public boolean supports(String source);
}
