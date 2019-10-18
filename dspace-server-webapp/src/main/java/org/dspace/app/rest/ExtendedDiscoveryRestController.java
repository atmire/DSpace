package org.dspace.app.rest;

import org.dspace.app.rest.model.SearchSupportRest;
import org.dspace.app.rest.model.hateoas.SearchSupportResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExtendedDiscoveryRestController extends DiscoveryRestController {


    @Override
    public SearchSupportResource getSearchSupport(@RequestParam(name = "scope", required = false) String dsoScope,
                                                  @RequestParam(name = "configuration", required = false) String
                                                      configuration)
        throws Exception {

        return new SearchSupportResource(new SearchSupportRest());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/test/te/ze/yt")
    public SearchSupportResource getSearchSupporttest(@RequestParam(name = "scope", required = false) String dsoScope,
                                                      @RequestParam(name = "configuration", required = false) String
                                                          configuration)
        throws Exception {

        return null;
    }
}
