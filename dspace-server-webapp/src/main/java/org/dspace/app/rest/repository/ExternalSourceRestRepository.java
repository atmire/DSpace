package org.dspace.app.rest.repository;

import java.util.List;

import org.dspace.app.rest.converter.ExternalSourceEntryRestConverter;
import org.dspace.app.rest.converter.ExternalSourceRestConverter;
import org.dspace.app.rest.model.ExternalSourceEntryRest;
import org.dspace.app.rest.model.ExternalSourceRest;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.external.provider.ExternalDataProvider;
import org.dspace.external.service.ExternalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component(ExternalSourceRest.CATEGORY + "." + ExternalSourceRest.NAME)
public class ExternalSourceRestRepository extends AbstractDSpaceRestRepository {

    @Autowired
    private ExternalDataService externalDataService;

    @Autowired
    private ExternalSourceRestConverter externalSourceRestConverter;

    @Autowired
    private ExternalSourceEntryRestConverter externalSourceEntryRestConverter;

    public Page<ExternalSourceRest> getAllExternalSources(Pageable pageable) {
        List<ExternalDataProvider> externalSources = externalDataService.getExternalDataProviders();
        Page<ExternalSourceRest> page = utils.getPage(externalSources, pageable).map(externalSourceRestConverter);
        return page;

    }

    public ExternalSourceRest getExternalSource(String authorityName) {
        ExternalDataProvider externalDataProvider = externalDataService.getExternalDataProvider(authorityName);
        return externalSourceRestConverter.fromModel(externalDataProvider);

    }

    public ExternalSourceEntryRest getExternalSourceEntryValue(String authorityName, String entryId) {
        ExternalDataObject externalDataObject = externalDataService.getExternalDataObject(authorityName, entryId);
        return externalSourceEntryRestConverter.fromModel(externalDataObject);

    }

    public Page<ExternalSourceEntryRest> getExternalSourceEntries(String authorityName, String query, String parent,
                                                                  Pageable pageable) {
        List<ExternalDataObject> externalDataObjects = externalDataService
            .searchExternalDataObjects(authorityName, query, pageable.getOffset(), pageable.getPageSize());
        Page<ExternalSourceEntryRest> page = utils.getPage(externalDataObjects, pageable)
                                                  .map(externalSourceEntryRestConverter);
        return page;
    }
}
