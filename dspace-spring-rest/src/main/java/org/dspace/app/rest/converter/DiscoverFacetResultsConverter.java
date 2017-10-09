package org.dspace.app.rest.converter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dspace.app.rest.model.FacetResultsRest;
import org.dspace.app.rest.model.SearchFacetEntryRest;
import org.dspace.app.rest.model.SearchFacetValueRest;
import org.dspace.app.rest.model.SearchResultsRest;
import org.dspace.app.rest.parameter.SearchFilter;
import org.dspace.authority.AuthorityValue;
import org.dspace.authority.service.AuthorityValueService;
import org.dspace.core.Context;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DiscoverFacetResultsConverter {


    @Autowired
    private AuthorityValueService authorityValueService;


    public FacetResultsRest convert(Context context, String facetName, DiscoverQuery discoverQuery, String dsoScope, List<SearchFilter> searchFilters, DiscoverResult searchResult, DiscoveryConfiguration configuration, Pageable page){
        FacetResultsRest facetResultsRest = new FacetResultsRest();
        addToFacetResultList(facetName, searchResult, facetResultsRest);
        setRequestInformation(context, facetName, discoverQuery, dsoScope, searchFilters, searchResult, configuration, facetResultsRest, page);

        return facetResultsRest;
    }

    private void addToFacetResultList(String facetName, DiscoverResult searchResult, FacetResultsRest facetResultsRest) {
//        for(DiscoverResult.FacetResult facetResult : searchResult.getFacetResult(facetName)){
//            facetResultsRest.addToFacetResultList(facetResult);
//        }
            List<DiscoverResult.FacetResult> facetValues = searchResult.getFacetResult(facetName);

            SearchFacetEntryRest facetEntry = new SearchFacetEntryRest(facetName);
            int valueCount = 0;

            for (DiscoverResult.FacetResult value : CollectionUtils.emptyIfNull(facetValues)) {
                if(valueCount >= 10){
                    break;
                }
                SearchFacetValueRest valueRest = new SearchFacetValueRest();
                valueRest.setLabel(value.getDisplayedValue());
                valueRest.setFilterValue(value.getAsFilterQuery());
                valueRest.setFilterType(value.getFilterType());
                valueRest.setAuthorityKey(value.getAuthorityKey());
                valueRest.setSortValue(value.getSortValue());
                valueRest.setCount(value.getCount());
                facetEntry.addValue(valueRest);
                if(StringUtils.isBlank(facetEntry.getFacetType())) {
                    facetEntry.setFacetType(value.getFieldType());
                }

                valueCount++;
            }

            for(SearchFacetValueRest searchFacetValueRest : CollectionUtils.emptyIfNull(facetEntry.getValues())){
                facetResultsRest.addToFacetResultList(searchFacetValueRest);
            }


    }

    private void setRequestInformation(Context context, String facetName, DiscoverQuery discoverQuery, String dsoScope, List<SearchFilter> searchFilters, DiscoverResult searchResult, DiscoveryConfiguration configuration, FacetResultsRest facetResultsRest, Pageable page) {
        facetResultsRest.setName(facetName);
        facetResultsRest.setQuery(discoverQuery.getQuery());
        facetResultsRest.setScope(dsoScope);
//        facetResultsRest.setPage(page);
        facetResultsRest.setQuery(discoverQuery.getQuery());
        if(!searchResult.getFacetResult(facetName).isEmpty()){
            facetResultsRest.setType(searchResult.getFacetResult(facetName).get(0).getFieldType());
        }
        if(searchResult.getFacetResult(facetName).size() > 10 && searchResult.getFacetResult(facetName).get(10) != null){
            facetResultsRest.setHasMore(true);
        }

        SearchResultsRest.Sorting sort = new SearchResultsRest.Sorting(discoverQuery.getSortField(), discoverQuery.getSortOrder().toString());
        facetResultsRest.setSort(sort);

//        if(page != null && page.getSort() != null && page.getSort().iterator().hasNext()) {
//            Sort.Order order = page.getSort().iterator().next();
//            facetResultsRest.setSort(order.getProperty(), order.getDirection().name());
//        }

        for (SearchFilter searchFilter : CollectionUtils.emptyIfNull(searchFilters)) {

            facetResultsRest.addAppliedFilter(convertSearchFilter(context, searchFilter));
        }
    }

    private SearchResultsRest.AppliedFilter convertSearchFilter(Context context, SearchFilter searchFilter) {
        AuthorityValue authorityValue = null;
        if(searchFilter.hasAuthorityOperator()) {
            authorityValue = authorityValueService.findByUID(context, searchFilter.getValue());
        }

        SearchResultsRest.AppliedFilter appliedFilter;
        if (authorityValue == null) {
            appliedFilter = new SearchResultsRest.AppliedFilter(searchFilter.getName(), searchFilter.getOperator(),
                    searchFilter.getValue(), searchFilter.getValue());
        } else {
            appliedFilter = new SearchResultsRest.AppliedFilter(searchFilter.getName(), searchFilter.getOperator(),
                    searchFilter.getValue(), authorityValue.getValue());
        }

        return appliedFilter;
    }
}
