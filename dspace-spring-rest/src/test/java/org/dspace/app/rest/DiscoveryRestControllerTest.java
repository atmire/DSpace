package org.dspace.app.rest;

import org.dspace.app.rest.converter.DiscoverSearchSupportConverter;
import org.dspace.app.rest.model.SearchSupportRest;
import org.dspace.app.rest.model.hateoas.SearchSupportResource;
import org.dspace.app.rest.repository.DiscoveryRestRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.when;

/**
 * Created by raf on 26/09/2017.
 */
public class DiscoveryRestControllerTest {

//    @InjectMocks
//    DiscoveryRestController discoveryRestController;
//
//    @Mock
//    DiscoveryRestRepository discoveryRestRepository = new DiscoveryRestRepository();
//
//    @Mock
//    DiscoverSearchSupportConverter discoverSearchSupportConverter = new DiscoverSearchSupportConverter();
//
//    @Before
//    public void setUp(){
//        discoveryRestController = new DiscoveryRestController();
//    }
//    @Test
//    public void testSetup() throws Exception{
//        when(discoverSearchSupportConverter.convert()).thenReturn(new SearchSupportRest());
//        when(discoveryRestController.getSearchSupport("","")).thenReturn(new SearchSupportResource(new SearchSupportRest()));
//        Object temp = discoveryRestController;
//        discoveryRestController.getSearchSupport("t", "t");
//        String t;
//    }
}
