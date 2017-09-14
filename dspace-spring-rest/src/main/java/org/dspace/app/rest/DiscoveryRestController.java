package org.dspace.app.rest;

import org.dspace.app.rest.model.DiscoveryRest;
import org.dspace.app.rest.parameter.SearchFilter;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TODO TOM UNIT TEST
 */
@RestController
@RequestMapping("/api/"+ DiscoveryRest.CATEGORY)
public class DiscoveryRestController {

    @RequestMapping(method = RequestMethod.GET, value = "/search")
    public void getSearchConfiguration(@RequestParam(name = "scope", required = false) String scope,
                                       @RequestParam(name = "configuration", required = false) String configuration) {
        //TODO
    }

    @RequestMapping(method = RequestMethod.GET, value = "/search/objects")
    public void getSearchObjects(@RequestParam(name = "query", required = false) String query,
                                 @RequestParam(name = "dsoType", required = false) String dsoType,
                                 @RequestParam(name = "scope", required = false) String scope,
                                 @RequestParam(name = "configuration", required = false) String configuration,
                                 List<SearchFilter> searchFilters,
                                 Pageable page) {
        //TODO
    }

    @RequestMapping(method = RequestMethod.GET, value = "/facets")
    public void getFacetsConfiguration(@RequestParam(name = "scope", required = false) String scope,
                                       @RequestParam(name = "configuration", required = false) String configuration) {
        //TODO
    }

    @RequestMapping(method = RequestMethod.GET, value = "/facets/{name}")
    public void getFacetValues(@PathVariable("name") String facetName,
                               @RequestParam(name = "query", required = false) String query,
                               @RequestParam(name = "dsoType", required = false) String dsoType,
                               @RequestParam(name = "scope", required = false) String scope,
                               List<SearchFilter> searchFilters,
                               Pageable page) {
        //TODO
    }

}
