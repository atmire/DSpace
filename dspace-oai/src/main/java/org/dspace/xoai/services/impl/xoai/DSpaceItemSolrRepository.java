/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.xoai.services.impl.xoai;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.base.Function;
import com.lyncode.builder.ListBuilder;
import com.lyncode.xoai.dataprovider.core.ListItemIdentifiersResult;
import com.lyncode.xoai.dataprovider.core.ListItemsResults;
import com.lyncode.xoai.dataprovider.data.Item;
import com.lyncode.xoai.dataprovider.data.ItemIdentifier;
import com.lyncode.xoai.dataprovider.exceptions.IdDoesNotExistException;
import com.lyncode.xoai.dataprovider.filter.ScopedFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.DiscoverResult;
import org.dspace.discovery.IndexableObject;
import org.dspace.discovery.SearchUtils;
import org.dspace.xoai.data.DSpaceSolrItem;
import org.dspace.xoai.filter.DSpaceDiscoveryFilter;
import org.dspace.xoai.filter.DateFromFilter;
import org.dspace.xoai.filter.DateUntilFilter;
import org.dspace.xoai.services.api.CollectionsService;
import org.dspace.xoai.services.api.HandleResolver;
import org.dspace.xoai.services.api.solr.SolrQueryResolver;
import org.dspace.xoai.solr.DSpaceSolrSearch;
import org.dspace.xoai.solr.exceptions.DSpaceSolrException;
import org.dspace.xoai.solr.exceptions.SolrSearchEmptyException;

/**
 * @author Lyncode Development Team (dspace at lyncode dot com)
 */
public class DSpaceItemSolrRepository extends DSpaceItemRepository {
    private static final Logger log = LogManager.getLogger(DSpaceItemSolrRepository.class);
    private final SolrClient server;
    private final SolrQueryResolver solrQueryResolver;

    public DSpaceItemSolrRepository(SolrClient server, CollectionsService collectionsService,
                                    HandleResolver handleResolver, SolrQueryResolver solrQueryResolver) {
        super(collectionsService, handleResolver);
        this.server = server;
        this.solrQueryResolver = solrQueryResolver;
    }

    @Override
    public Item getItem(String identifier) throws IdDoesNotExistException {
        if (identifier == null) {
            throw new IdDoesNotExistException();
        }
        String parts[] = identifier.split(Pattern.quote(":"));
        if (parts.length == 3) {
            try {
                SolrQuery params = new SolrQuery("item.handle:" + parts[2]);
                return new DSpaceSolrItem(DSpaceSolrSearch.querySingle(server, params));
            } catch (SolrSearchEmptyException | IOException ex) {
                throw new IdDoesNotExistException(ex);
            }
        }
        throw new IdDoesNotExistException();
    }

    @Override
    public ListItemIdentifiersResult getItemIdentifiers(
        List<ScopedFilter> filters, int offset, int length) {
        try
        {
            boolean isDiscoveryFilter = false;
            DSpaceDiscoveryFilter discoveryFilter = null;
            for (ScopedFilter filter : filters) {
                if (filter.getCondition().getFilter() instanceof DSpaceDiscoveryFilter) {
                    isDiscoveryFilter = true;
                    discoveryFilter = (DSpaceDiscoveryFilter) filter.getCondition().getFilter();
                }
            }
            if (isDiscoveryFilter && discoveryFilter != null) {
                return this.getDiscoveryIdentifierResult(discoveryFilter, filters, offset, length);

            } else {
            QueryResult queryResult = retrieveItems(filters, offset, length);
            List<ItemIdentifier> identifierList = new ListBuilder<Item>()
                .add(queryResult.getResults())
                .build(new Function<Item, ItemIdentifier>() {
                    @Override
                    public ItemIdentifier apply(Item elem) {
                        return elem;
                    }
                });
            return new ListItemIdentifiersResult(queryResult.hasMore(), identifierList, queryResult.getTotal());
            }
        }
        catch (DSpaceSolrException | IOException ex)
        {
            log.error(ex.getMessage(), ex);
            return new ListItemIdentifiersResult(false, new ArrayList<>());
        }
    }

    @Override
    public ListItemsResults getItems(List<ScopedFilter> filters, int offset,
                                     int length) {
        try
        {
            boolean isDiscoveryFilter = false;
            DSpaceDiscoveryFilter discoveryFilter = null;
            for (ScopedFilter filter : filters) {
                if (filter.getCondition().getFilter() instanceof DSpaceDiscoveryFilter) {
                    isDiscoveryFilter = true;
                    discoveryFilter = (DSpaceDiscoveryFilter) filter.getCondition().getFilter();
                }
            }
            if (isDiscoveryFilter && discoveryFilter != null) {
                return this.getDiscoveryResult(discoveryFilter, filters, offset, length);

            } else {

            QueryResult queryResult = retrieveItems(filters, offset, length);
            return new ListItemsResults(queryResult.hasMore(), queryResult.getResults(), queryResult.getTotal());
            }
        }
        catch (DSpaceSolrException | IOException ex)
        {
            log.error(ex.getMessage(), ex);
            return new ListItemsResults(false, new ArrayList<>());
        }
    }

    private QueryResult retrieveItems(List<ScopedFilter> filters, int offset, int length)
            throws DSpaceSolrException, IOException {
        List<Item> list = new ArrayList<>();
        SolrQuery params = new SolrQuery(solrQueryResolver.buildQuery(filters))
            .setRows(length)
            .setStart(offset);
        SolrDocumentList solrDocuments = DSpaceSolrSearch.query(server, params);
        for (SolrDocument doc : solrDocuments) {
            list.add(new DSpaceSolrItem(doc));
        }
        return new QueryResult(list, (solrDocuments.getNumFound() > offset + length),
                               (int) solrDocuments.getNumFound());
    }

    private class QueryResult {
        private List<Item> results;
        private boolean hasMore;
        private int total;

        private QueryResult(List<Item> results, boolean hasMore, int total) {
            this.results = results;
            this.hasMore = hasMore;
            this.total = total;
        }

        private List<Item> getResults() {
            return results;
        }

        private boolean hasMore() {
            return hasMore;
        }

        private int getTotal() {
            return total;
        }
    }

    private ListItemsResults getDiscoveryResult(DSpaceDiscoveryFilter filter, List<ScopedFilter> filters, int offset, int length)
    {
        StringBuilder filterQuery = new StringBuilder();

        addFromAndUntilDate(filters, filterQuery);

        boolean hasMore;
        int total;
        Context context= null;
        List<Item> list = new ArrayList<Item>();
        try {
            context = new Context();
            context.turnOffAuthorisationSystem();

            DiscoverQuery query = filter.getDiscoverQuery();
            query.addFilterQueries(filterQuery.toString());
            query.setMaxResults(length);
            query.setStart(offset);

            DiscoverResult queryResults = null;

            queryResults = SearchUtils.getSearchService().search(context, query);


            List<IndexableObject> resultList = queryResults.getIndexableObjects();
            total = (int) queryResults.getTotalSearchResults();
            hasMore = queryResults.getTotalSearchResults() > offset + length;

            StringBuilder oaiSolrQuery = new StringBuilder();
            // Process results of query into HarvestedItemInfo objects
            for (int i = 0; i < resultList.size(); i++) {
                IndexableObject dso = resultList.get(i);
                if (dso instanceof org.dspace.content.Item) {
                    org.dspace.content.Item item = (org.dspace.content.Item) dso;
                    if (oaiSolrQuery.length() > 0) {
                        oaiSolrQuery.append(" OR ");
                    }
                    oaiSolrQuery.append("item.id:" + item.getID());
                }
            }

            SolrQuery params = new SolrQuery(oaiSolrQuery.toString()).setRows(length);
            SolrDocumentList docs = DSpaceSolrSearch.query(server,params);
            for (SolrDocument doc : docs) {
                list.add(new DSpaceSolrItem(doc));
            }
            return new ListItemsResults(hasMore, list, total);
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage(), ex);
            return new ListItemsResults(false, list);
        } finally {
            if(context != null)
            {
                context.abort();
            }
        }
    }

    private ListItemIdentifiersResult getDiscoveryIdentifierResult(DSpaceDiscoveryFilter filter, List<ScopedFilter> filters, int offset, int length)
    {
        StringBuilder filterQuery = new StringBuilder();

        addFromAndUntilDate(filters, filterQuery);

        boolean hasMore;
        int total;
        Context context= null;
        List<ItemIdentifier> list = new ArrayList<ItemIdentifier>();

        try {
            context = new Context();
            context.turnOffAuthorisationSystem();

            DiscoverQuery query = filter.getDiscoverQuery();
            query.addFilterQueries(filterQuery.toString());
            query.setMaxResults(length);
            query.setStart(offset);

            DiscoverResult queryResults = null;

            queryResults = SearchUtils.getSearchService().search(context, query);


            List<IndexableObject> resultList = queryResults.getIndexableObjects();
            total = (int) queryResults.getTotalSearchResults();
            hasMore = queryResults.getTotalSearchResults() > offset + length;

            StringBuilder oaiSolrQuery = new StringBuilder();
            // Process results of query into HarvestedItemInfo objects
            for (int i = 0; i < resultList.size(); i++) {
                IndexableObject dso = resultList.get(i);
                if (dso instanceof org.dspace.content.Item) {
                    org.dspace.content.Item item = (org.dspace.content.Item) dso;
                    if (oaiSolrQuery.length() > 0) {
                        oaiSolrQuery.append(" OR ");
                    }
                    oaiSolrQuery.append("item.id:" + item.getID());
                }
            }
            SolrQuery params = new SolrQuery(oaiSolrQuery.toString()).setRows(length);
            SolrDocumentList docs = DSpaceSolrSearch.query(server,params);
            for (SolrDocument doc : docs)
            {
                list.add(new DSpaceSolrItem(doc));
            }
            return new ListItemIdentifiersResult(hasMore, list, (int) docs.getNumFound());
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage(), ex);
            return new ListItemIdentifiersResult(false, list);
        } finally {
            if(context != null)
            {
                context.abort();
            }
        }
    }

    private void addFromAndUntilDate(final List<ScopedFilter> filters, final StringBuilder filterQuery) {
        Date startDate = null;
        Date endDate = null;
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        for (ScopedFilter _filter : filters) {
            if (_filter.getCondition().getFilter() instanceof DateFromFilter) {
                startDate = ((DateFromFilter) _filter.getCondition().getFilter()).getDate();
            } else if (_filter.getCondition().getFilter() instanceof DateUntilFilter) {
                endDate = ((DateUntilFilter) _filter.getCondition().getFilter()).getDate();
            }
        }

        if(startDate!=null || endDate!=null) {
            filterQuery.append("lastModified:([");
            if (startDate != null) {
                filterQuery.append(ClientUtils.escapeQueryChars(formatDate.format(startDate)));
            } else {
                filterQuery.append(" *");
            }
            filterQuery.append(" TO ");
            if (endDate != null) {
                filterQuery.append(ClientUtils.escapeQueryChars(formatDate.format(endDate)));
            } else {
                filterQuery.append("* ");
            }
            filterQuery.append("]");

            if (endDate != null) {
                filterQuery.append(" NOT ").append(ClientUtils.escapeQueryChars(formatDate.format(endDate)));
            }
            filterQuery.append(")");
        }
    }
}
