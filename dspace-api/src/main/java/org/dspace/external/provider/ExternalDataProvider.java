package org.dspace.external.provider;

import java.util.List;
import java.util.Optional;

import org.dspace.external.model.ExternalDataObject;

public interface ExternalDataProvider {

    public String getSourceIdentifier();

    Optional<ExternalDataObject> getExternalDataObject(String id);
    List<ExternalDataObject> searchExternalDataObjects(String query, int start, int limit);
    public boolean supports(String source);
}
