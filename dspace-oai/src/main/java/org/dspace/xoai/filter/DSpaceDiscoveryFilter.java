package org.dspace.xoai.filter;

import com.lyncode.xoai.dataprovider.xml.xoaiconfig.parameters.ParameterValue;
import com.lyncode.xoai.dataprovider.xml.xoaiconfig.parameters.SimpleType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dspace.core.Constants;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.xoai.data.DSpaceItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bavo Van Geit
 * Date: 4/07/13
 * Time: 11:45
 */
public class DSpaceDiscoveryFilter extends DSpaceFilter
{
    private String query;
    private String[] filterQueries;

    private static Logger log = LogManager.getLogger(DSpaceDiscoveryFilter.class);

    @Override
    public org.dspace.xoai.filter.results.SolrFilterResult buildSolrQuery() {
        return null;
    }

    public boolean isShown(DSpaceItem item) {
        return false;  //TODO
    }

    public DiscoverQuery getDiscoverQuery() {
        DiscoverQuery discoverQuery = new DiscoverQuery();
        if(getDQuery() == null || getDQuery().isEmpty()){
            discoverQuery.setQuery(null);
        }
        else{
            discoverQuery.setQuery(getDQuery());
        }

        getFilterQueries();
        if(filterQueries!=null) {
            discoverQuery.addFilterQueries(filterQueries);
        }
        return discoverQuery;
    }

    private String[] getFilterQueries()
    {
        if (this.filterQueries == null)
        {
            List<String> _filterQueries = new ArrayList<>();
            ParameterValue filterQueries = getConfiguration().get("filterQueries");

            if(filterQueries.asParameterList().getValues()!=null) {
                for (ParameterValue val : filterQueries.asParameterList().getValues()) {
                    _filterQueries.add(val.asSimpleType().asString());
                }

            }
            _filterQueries.add("search.resourcetype:"+ Constants.ITEM);
            this.filterQueries = _filterQueries.toArray(new String[_filterQueries.size()]);
            if (log.isDebugEnabled()) {
                log.debug(_filterQueries);
            }
        }
        return filterQueries;
    }

    private String getDQuery()
    {
        if (query == null)
        {
            String _query = null;
            ParameterValue queryParam = getConfiguration().get("query");
            _query= ((SimpleType) queryParam).asString();

            if (_query == null || _query.trim().isEmpty())
                _query = "*:*";
            query = _query;
            if (log.isDebugEnabled()) {
                log.debug(query);
            }
        }
        return query;
    }
}
