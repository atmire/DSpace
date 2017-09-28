package org.dspace.app.rest.model;

import org.dspace.app.rest.DiscoveryRestController;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by raf on 26/09/2017.
 */
public class SearchConfigurationRestTest {

    SearchConfigurationRest searchConfigurationRest;

    @Before
    public void setUp() throws Exception{
        searchConfigurationRest = new SearchConfigurationRest();
    }

    @Test
    public void testConstructorDoesNotReturnNull() throws Exception{
        assertNotNull(searchConfigurationRest);
    }

    @Test
    public void testGetTypeReturnsCorrectValue() throws Exception{
        assertEquals(SearchConfigurationRest.NAME, searchConfigurationRest.getType());
    }

    @Test
    public void testGetCategoryReturnsCorrectValue() throws Exception{
        assertEquals(SearchConfigurationRest.CATEGORY, searchConfigurationRest.getCategory());
    }

    @Test
    public void testGetControllerReturnsCorrectValue() throws Exception{
        assertEquals(DiscoveryRestController.class, searchConfigurationRest.getController());
    }

    @Test
    public void testSetScopeNullReturnsNull() throws Exception{
        searchConfigurationRest.setScope(null);
        assertNull(searchConfigurationRest.getScope());
    }

    @Test
    public void testSetScopeProperValueReturnsCorrectValue() throws Exception{
        searchConfigurationRest.setScope("scope");
        assertEquals("scope", searchConfigurationRest.getScope());
    }

    @Test
    public void testSetConfigurationNameNullReturnsNull() throws Exception{
        searchConfigurationRest.setConfigurationName(null);
        assertNull(searchConfigurationRest.getConfigurationName());
    }

    @Test
    public void testSetConfigurationNameProperValueReturnsCorrectValue() throws Exception{
        searchConfigurationRest.setConfigurationName("configuration name");
        assertEquals("configuration name", searchConfigurationRest.getConfigurationName());
    }

    @Test
    public void testFiltersNotNullAfterConstructor() throws Exception{
        assertNotNull(searchConfigurationRest.getFilters());
    }

    @Test
    public void testSortOptionsNotNullAfterConstructor() throws Exception{
        assertNotNull(searchConfigurationRest.getSortOptions());
    }

    @Test
    public void testAddFilterToEmptyListAndListContainsThatFilter() throws Exception{
        SearchConfigurationRest.Filter filter = new SearchConfigurationRest.Filter();
        filter.setFilter("filter");
        searchConfigurationRest.addFilter(filter);
        assertEquals(filter, searchConfigurationRest.getFilters().get(0));
    }

    @Test
    public void testAddSortOptionToEmptyListAndListContainsThatSortOption() throws Exception{
        SearchConfigurationRest.SortOption sortOption = new SearchConfigurationRest.SortOption();
        sortOption.setName("sort option");
        searchConfigurationRest.addSortOption(sortOption);
        assertEquals(sortOption, searchConfigurationRest.getSortOptions().get(0));
    }

    @Test
    public void testAddMultipleFiltersToListAndListIsConstructedProperly() throws Exception{
        SearchConfigurationRest.Filter filter = new SearchConfigurationRest.Filter();
        filter.setFilter("filter");
        searchConfigurationRest.addFilter(filter);
        SearchConfigurationRest.Filter filter2 = new SearchConfigurationRest.Filter();
        filter.setFilter("filter2");
        searchConfigurationRest.addFilter(filter2);

        assertEquals(2, searchConfigurationRest.getFilters().size());

        assertTrue(searchConfigurationRest.getFilters().get(0) == filter || searchConfigurationRest.getFilters().get(0) == filter2);
        assertTrue(searchConfigurationRest.getFilters().get(1) == filter || searchConfigurationRest.getFilters().get(1) == filter2);

    }

    @Test
    public void testAddMultipleSortOptionsToListAndListIsConstructedProperly() throws Exception{
        SearchConfigurationRest.SortOption sortOption = new SearchConfigurationRest.SortOption();
        sortOption.setName("sort option");
        searchConfigurationRest.addSortOption(sortOption);
        SearchConfigurationRest.SortOption sortOption2 = new SearchConfigurationRest.SortOption();
        sortOption2.setName("sort option2");
        searchConfigurationRest.addSortOption(sortOption2);

        assertEquals(2, searchConfigurationRest.getSortOptions().size());

        assertTrue(searchConfigurationRest.getSortOptions().get(0) == sortOption || searchConfigurationRest.getSortOptions().get(0) == sortOption2);
        assertTrue(searchConfigurationRest.getSortOptions().get(1) == sortOption || searchConfigurationRest.getSortOptions().get(1) == sortOption2);

    }

    @Test
    public void testSortOptionsSetNameNullReturnsNull() throws Exception{
        SearchConfigurationRest.SortOption sortOption = new SearchConfigurationRest.SortOption();
        sortOption.setName(null);
        assertNull(sortOption.getName());
    }

    @Test
    public void testSortOptionsSetNameProperValueReturnsCorrectValue() throws Exception{
        SearchConfigurationRest.SortOption sortOption = new SearchConfigurationRest.SortOption();
        sortOption.setName("name");
        assertEquals("name", sortOption.getName());
    }

    @Test
    public void testSortOptionsGetNameIsNullAfterConstructor() throws Exception{
        SearchConfigurationRest.SortOption sortOption = new SearchConfigurationRest.SortOption();
        assertNull(sortOption.getName());
    }

    @Test
    public void testSortOptionsSetMetadataNullReturnsNull() throws Exception{
        SearchConfigurationRest.SortOption sortOption = new SearchConfigurationRest.SortOption();
        sortOption.setMetadata(null);
        assertNull(sortOption.getMetadata());
    }

    @Test
    public void testSortOptionsSetMetadataProperValueReturnsCorrectValue() throws Exception{
        SearchConfigurationRest.SortOption sortOption = new SearchConfigurationRest.SortOption();
        sortOption.setMetadata("metadata");
        assertEquals("metadata", sortOption.getMetadata());
    }

    @Test
    public void testSortOptionsGetMetadataIsNullAfterConstructor() throws Exception{
        SearchConfigurationRest.SortOption sortOption = new SearchConfigurationRest.SortOption();
        assertNull(sortOption.getMetadata());
    }

    @Test
    public void testOperatorConstructorNullParameterMakesObjectWithNullOperator() throws Exception{
        SearchConfigurationRest.Filter.Operator operator = new SearchConfigurationRest.Filter.Operator(null);
        assertNotNull(operator);
        assertNull(operator.getOperator());
    }

    @Test
    public void testOperatorConstructorWithProperValueReturnsCorrectValue() throws Exception{
        SearchConfigurationRest.Filter.Operator operator = new SearchConfigurationRest.Filter.Operator("operator");
        assertEquals("operator", operator.getOperator());
    }

    @Test
    public void testFilterSetFilterNullReturnsNull() throws Exception{
        SearchConfigurationRest.Filter filter = new SearchConfigurationRest.Filter();
        filter.setFilter(null);
        assertNull(filter.getFilter());
    }

    @Test
    public void testFilterSetFilterProperValueReturnsCorrectValue() throws Exception{
        SearchConfigurationRest.Filter filter = new SearchConfigurationRest.Filter();
        filter.setFilter("name");
        assertEquals("name", filter.getFilter());
    }

    @Test
    public void testFilterGetFilterIsNullAfterConstructor() throws Exception{
        SearchConfigurationRest.Filter filter = new SearchConfigurationRest.Filter();
        assertNull(filter.getFilter());
    }

    @Test
    public void testFilterGetOperatorsAfterConstructorReturnsEmptyList() throws Exception{
        SearchConfigurationRest.Filter filter = new SearchConfigurationRest.Filter();
        assertTrue(filter.getOperators().isEmpty());
    }

    @Test
    public void testFilterAddOperatorAddsProperlyAndIsIncludedInGetOperators() throws Exception{
        SearchConfigurationRest.Filter filter = new SearchConfigurationRest.Filter();
        SearchConfigurationRest.Filter.Operator operator = new SearchConfigurationRest.Filter.Operator("operator");
        filter.addOperator(operator);
        assertEquals(operator, filter.getOperators().get(0));
    }

    @Test
    public void testFilterAddDefaultOperatorsToListPopulatesList() throws Exception{
        SearchConfigurationRest.Filter filter = new SearchConfigurationRest.Filter();
        filter.addDefaultOperatorsToList();
        assertTrue(!filter.getOperators().isEmpty());
    }
}