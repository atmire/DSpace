/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;


import org.dspace.app.rest.model.SearchFacetValueRest;
import org.dspace.discovery.DiscoverResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * This class' purpose is to test the DiscoverFacetValueConverter
 */
@RunWith(MockitoJUnitRunner.class)
public class DiscoverFacetValueConverterTest {

    @InjectMocks
    private DiscoverFacetValueConverter discoverFacetValueConverter;

    private DiscoverResult.FacetResult value;

    @Before
    public void setUp() throws Exception{
        value = new DiscoverResult.FacetResult("testAsFilterQuery", "testDisplayedValue", "authority", "testSortValue", Long.parseLong("1"), "testFieldType");
    }
    @Test
    public void testConvert() throws Exception{

        SearchFacetValueRest searchFacetValueRest = discoverFacetValueConverter.convert(value);

        assertEquals(searchFacetValueRest.getLabel(), "testDisplayedValue");
        assertEquals(searchFacetValueRest.getFilterValue(), "testAsFilterQuery");
        assertEquals(searchFacetValueRest.getFilterType(), "authority");
        assertEquals(searchFacetValueRest.getAuthorityKey(), "authority");
        assertEquals(searchFacetValueRest.getSortValue(), "testSortValue");
        assertEquals(searchFacetValueRest.getCount(), Long.parseLong("1"));
    }
}
