package org.dspace.external.service;

import java.util.List;

import org.dspace.external.model.ExternalDataObject;

public interface ExternalDataService {

    public List<String> getExternalSources();

    public ExternalDataObject getExternalDataObject(String source, String identifier);

    public List<ExternalDataObject> searchExternalDataObjects(String source, String query, int start, int limit);

}
