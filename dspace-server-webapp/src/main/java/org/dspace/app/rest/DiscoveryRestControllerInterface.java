package org.dspace.app.rest;

import java.util.List;

import org.dspace.app.rest.model.SearchResultsRest;
import org.dspace.app.rest.model.hateoas.FacetConfigurationResource;
import org.dspace.app.rest.model.hateoas.FacetsResource;
import org.dspace.app.rest.model.hateoas.SearchConfigurationResource;
import org.dspace.app.rest.model.hateoas.SearchResultsResource;
import org.dspace.app.rest.model.hateoas.SearchSupportResource;
import org.dspace.app.rest.parameter.SearchFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/" + SearchResultsRest.CATEGORY)
public interface DiscoveryRestControllerInterface {

    @RequestMapping(method = RequestMethod.GET)
    public SearchSupportResource getSearchSupport(@RequestParam(name = "scope", required = false) String dsoScope,
                                                  @RequestParam(name = "configuration", required = false) String
                                                      configuration)
        throws Exception;

    @RequestMapping(method = RequestMethod.GET, value = "/search")
    public SearchConfigurationResource getSearchConfiguration(
        @RequestParam(name = "scope", required = false) String dsoScope,
        @RequestParam(name = "configuration", required = false) String configuration) throws Exception;

    @RequestMapping(method = RequestMethod.GET, value = "/search/facets")
    public FacetsResource getFacets(@RequestParam(name = "query", required = false) String query,
                                    @RequestParam(name = "dsoType", required = false) String dsoType,
                                    @RequestParam(name = "scope", required = false) String dsoScope,
                                    @RequestParam(name = "configuration", required = false) String configuration,
                                    List<SearchFilter> searchFilters,
                                    Pageable page) throws Exception;

    @RequestMapping(method = RequestMethod.GET, value = "/search/objects")
    public SearchResultsResource getSearchObjects(@RequestParam(name = "query", required = false) String query,
                                                  @RequestParam(name = "dsoType", required = false) String dsoType,
                                                  @RequestParam(name = "scope", required = false) String dsoScope,
                                                  @RequestParam(name = "configuration", required = false) String
                                                      configuration,
                                                  List<SearchFilter> searchFilters,
                                                  Pageable page) throws Exception;

    @RequestMapping(method = RequestMethod.GET, value = "/facets")
    public FacetConfigurationResource getFacetsConfiguration(
        @RequestParam(name = "scope", required = false) String dsoScope,
        @RequestParam(name = "configuration", required = false) String configuration,
        Pageable pageable) throws Exception;

    @RequestMapping(method = RequestMethod.GET, value = "/facets/{name}")
    public ResourceSupport getFacetValues(@PathVariable("name") String facetName,
                                          @RequestParam(name = "prefix", required = false) String prefix,
                                          @RequestParam(name = "query", required = false) String query,
                                          @RequestParam(name = "dsoType", required = false) String dsoType,
                                          @RequestParam(name = "scope", required = false) String dsoScope,
                                          @RequestParam(name = "configuration", required = false) String
                                              configuration,
                                          List<SearchFilter> searchFilters,
                                          Pageable page) throws Exception;
}
