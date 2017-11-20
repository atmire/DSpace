/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.converter;

import org.dspace.app.rest.model.SearchResultsRest;
import org.dspace.app.rest.parameter.SearchFilter;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.service.AuthorityValueService;
import org.dspace.core.Context;
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
public class SearchFilterToAppliedFilterConverterTest {

    @InjectMocks
    private SearchFilterToAppliedFilterConverter searchFilterToAppliedFilterConverter;

    private SearchFilter searchFilter;

    @Mock
    private AuthorityValueService authorityValueService;

    @Mock
    private Context context;

    @Before
    public void setUp() throws Exception{

    }
    @Test
    public void testConvertNoAuthorityOperator() throws Exception{
        searchFilter = new SearchFilter("searchFilterName", "searchFilterOperator", "searchFilterValue");
        SearchResultsRest.AppliedFilter appliedFilter = searchFilterToAppliedFilterConverter.convertSearchFilter(context, searchFilter);

        assertEquals(appliedFilter.getFilter(), "searchFilterName");
        assertEquals(appliedFilter.getOperator(), "searchFilterOperator");
        assertEquals(appliedFilter.getValue(), "searchFilterValue");
        assertEquals(appliedFilter.getLabel(), "searchFilterValue");
    }

    @Test
    public void testConvertAuthorityOperatorAuthorityValueNotNull() throws Exception{

        AuthorityValue authorityValue = new AuthorityValue();
        authorityValue.setValue("authorityValue");
        searchFilter = new SearchFilter("searchFilterName", "authority", "searchFilterValue");

        when(authorityValueService.findByUID(context, searchFilter.getValue())).thenReturn(authorityValue);

        SearchResultsRest.AppliedFilter appliedFilter = searchFilterToAppliedFilterConverter.convertSearchFilter(context, searchFilter);

        assertEquals(appliedFilter.getFilter(), "searchFilterName");
        assertEquals(appliedFilter.getOperator(), "authority");
        assertEquals(appliedFilter.getValue(), "searchFilterValue");
        assertEquals(appliedFilter.getLabel(), "authorityValue");
    }

    @Test
    public void testConvertAuthorityOperatorAuthorityValueNull() throws Exception{
        searchFilter = new SearchFilter("searchFilterName", "authority", "searchFilterValue");
        when(authorityValueService.findByUID(context, searchFilter.getValue())).thenReturn(null);

        SearchResultsRest.AppliedFilter appliedFilter = searchFilterToAppliedFilterConverter.convertSearchFilter(context, searchFilter);

        assertEquals(appliedFilter.getFilter(), "searchFilterName");
        assertEquals(appliedFilter.getOperator(), "authority");
        assertEquals(appliedFilter.getValue(), "searchFilterValue");
        assertEquals(appliedFilter.getLabel(), "searchFilterValue");
    }
}