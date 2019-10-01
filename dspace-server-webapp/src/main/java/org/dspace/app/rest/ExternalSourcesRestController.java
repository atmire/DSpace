package org.dspace.app.rest;

import java.util.Arrays;

import org.dspace.app.rest.link.HalLinkService;
import org.dspace.app.rest.model.ExternalSourceEntryRest;
import org.dspace.app.rest.model.ExternalSourceRest;
import org.dspace.app.rest.model.hateoas.ExternalSourceEntryResource;
import org.dspace.app.rest.model.hateoas.ExternalSourceResource;
import org.dspace.app.rest.repository.ExternalSourceRestRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integration/externalsources")
public class ExternalSourcesRestController implements InitializingBean {

    @Autowired
    private DiscoverableEndpointsService discoverableEndpointsService;

    @Autowired
    private ExternalSourceRestRepository externalSourceRestRepository;

    @Autowired
    HalLinkService linkService;

    @Override
    public void afterPropertiesSet() throws Exception {
        discoverableEndpointsService
            .register(this, Arrays.asList(new Link("/api/integration/externalsources", "externalsources")));
    }

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<ExternalSourceResource> getExternalSources(Pageable pageable,
                                                                     PagedResourcesAssembler assembler) {
        Page<ExternalSourceRest> externalSourceRestPage = externalSourceRestRepository.getAllExternalSources(pageable);
        Page<ExternalSourceResource> externalSourceResources = externalSourceRestPage
            .map(externalSourceRest -> new ExternalSourceResource(externalSourceRest));
        externalSourceResources.forEach(linkService::addLinks);
        PagedResources<ExternalSourceResource> result = assembler.toResource(externalSourceResources);
        return result;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{authorityName}")
    public ExternalSourceResource getExternalSource(@PathVariable("authorityName") String authorityName) {
        ExternalSourceRest externalSourceRest = externalSourceRestRepository.getExternalSource(authorityName);
        ExternalSourceResource externalSourceResource = new ExternalSourceResource(externalSourceRest);
        linkService.addLinks(externalSourceResource);

        return externalSourceResource;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{authorityName}/entries")
    public PagedResources<ExternalSourceEntryResource> getExternalSourceEntries(
        @PathVariable("authorityName") String authorityName,
        @RequestParam(name = "query") String query,
        @RequestParam(name = "parent", required = false) String parent,
        Pageable pageable, PagedResourcesAssembler assembler) {

        Page<ExternalSourceEntryRest> externalSourceEntryRestPage = externalSourceRestRepository
            .getExternalSourceEntries(authorityName, query, parent, pageable);
        Page<ExternalSourceEntryResource> externalSourceEntryResources = externalSourceEntryRestPage
            .map(externalSourceEntryRest -> new ExternalSourceEntryResource(externalSourceEntryRest));
        externalSourceEntryResources.forEach(linkService::addLinks);
        PagedResources<ExternalSourceEntryResource> result = assembler.toResource(externalSourceEntryResources);
        return result;

    }

    @RequestMapping(method = RequestMethod.GET, value = "/{authorityName}/entryValues/{entryId}")
    public ExternalSourceEntryResource getExternalSourceEntryValue(@PathVariable("authorityName") String authorityName,
                                                                   @PathVariable("entryId") String entryId) {

        ExternalSourceEntryRest externalSourceEntryRest = externalSourceRestRepository
            .getExternalSourceEntryValue(authorityName, entryId);
        ExternalSourceEntryResource externalSourceEntryResource = new ExternalSourceEntryResource(
            externalSourceEntryRest);
        linkService.addLinks(externalSourceEntryResource);

        return externalSourceEntryResource;
    }
}
