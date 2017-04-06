/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 * <p>
 * http://www.dspace.org/license/
 */
package org.dspace.authority;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.dspace.authority.factory.AuthorityServiceFactory;
import org.dspace.authority.factory.AuthorityValueFactory;
import org.dspace.authority.indexer.AuthorityIndexerInterface;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.authority.service.CachedAuthorityService;
import org.dspace.authority.service.ItemService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.content.authority.SolrAuthority;
import org.dspace.core.Context;
import org.dspace.core.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.*;

/**
 * @author philip at atmire.com
 */
public class CachedAuthorityServiceImpl implements CachedAuthorityService {

    private final Logger log = Logger.getLogger(CachedAuthorityServiceImpl.class);

    @Autowired
    protected AuthorityValueFactory authorityValueFactory;

    @Autowired(required = true)
    protected ItemService itemService;

    @Autowired(required = true)
    protected AuthorityIndexingService indexingService;


    /**
     * Index all authorities that are available in the metadata of the provided item.
     *
     * @param context the DSpace context
     * @param item    The item for which the authorities will be indexed
     * @throws SQLException
     * @throws AuthorizeException
     */
    @Override
    public void writeItemAuthorityMetadataValuesToCache(Context context, Item item) throws SQLException, AuthorizeException {
        if (!isConfigurationValid()) {
            //Cannot index, configuration not valid
            return;
        }


        for (AuthorityIndexerInterface indexers : AuthorityServiceFactory.getInstance().createAuthorityIndexers(context, item)) {
            while (indexers.hasMore()) {
                AuthorityValue authorityValue = indexers.nextValue();
                if (authorityValue != null)
                    indexingService.indexContent(authorityValue);
            }
            //Close up
            indexers.close();
        }
        //Commit to our server
        indexingService.commit();
    }

    @Override
    public boolean isConfigurationValid() {
        if (!indexingService.isConfiguredProperly()) {
            return false;
        }

        for (AuthorityIndexerInterface indexerInterface : AuthorityServiceFactory.getInstance().createUninitialisedAuthorityIndexers()) {
            if (!indexerInterface.isConfiguredProperly()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieve all authority values found in the authority cache. Returns a map with as key the identifier of the authority value
     * and as value the authority value.
     *
     * @param context the dspace context
     * @return A map containing all authority values
     * @throws SQLException
     * @throws AuthorizeException
     */
    public Map<String, AuthorityValue> getAllCachedAuthorityValues(Context context) throws SQLException, AuthorizeException {
        Map<String, AuthorityValue> toIndexValues = new HashMap<>();

        for (AuthorityIndexerInterface indexerInterface : AuthorityServiceFactory.getInstance().createAuthorityIndexers(context)) {
            while (indexerInterface.hasMore()) {
                AuthorityValue authorityValue = indexerInterface.nextValue();
                if (authorityValue != null) {
                    toIndexValues.put(authorityValue.getId(), authorityValue);
                }
            }
            //Close up
            indexerInterface.close();
        }
        return toIndexValues;
    }

    /**
     * Empty the authority index.
     *
     * @throws Exception
     */
    @Override
    public void cleanCache() throws Exception {
        indexingService.cleanIndex();
    }

    /**
     * Index an authority value.
     *
     * @param authorityValue
     */
    @Override
    public void writeAuthorityValueToCache(AuthorityValue authorityValue) {
        indexingService.indexContent(authorityValue);
    }

    @Override
    public void writeAuthorityValueToCacheWithSolrId(AuthorityValue authorityValue) {
        indexingService.indexContentWithSolrId(authorityValue);
    }


    /**
     * Commit all changes to the authority index since the last commit.
     */
    @Override
    public void commitAuthorityCache() {
        indexingService.commit();
    }

    /**
     * Create an authority svalue object. This object is not yet stored in the authority cache.
     *
     * @param context      The context
     * @param authorityKey The authority key
     * @param content      The metadata value
     * @param field        The field
     * @return The new authority value object
     */
    @Override
    public AuthorityValue createNonCachedAuthorityValue(Context context, String authorityKey, String content, String field) {
        return createNonCachedAuthorityValue(context, authorityKey, content, getCategory(field));
    }

    public AuthorityValue createNonCachedAuthorityValue(Context context, String authorityKey, String content, AuthorityCategory category) {
        AuthorityValue nextValue = generateRaw(authorityKey, content, category);
        nextValue.updateLastModifiedDate();
        nextValue.setCreationDate(new Date());
        nextValue.setVerified(false);

        return nextValue;
    }

    /**
     * Create a raw authority value object.
     *
     * @param authorityKey The authority key
     * @param content      The metadata value
     * @param category     The category
     * @return
     */
    protected AuthorityValue generateRaw(String authorityKey, String content, AuthorityCategory category) {
        AuthorityValue nextValue;

        AuthorityKeyRepresentation authorityKeyRepresentation = new AuthorityKeyRepresentation(authorityKey);
        if (StringUtils.isNotBlank(authorityKeyRepresentation.getInternalIdentifier())) {
            nextValue = authorityValueFactory.createAuthorityValue(category, authorityKeyRepresentation.getInternalIdentifier(), content);
        } else {
            nextValue = authorityValueFactory.createAuthorityValue(category, content);
        }
        return nextValue;
    }

    /**
     * Update the index of an authority value
     *
     * @param value the authority value to index
     * @return the updated authority value
     */
    @Override
    public AuthorityValue updateAuthorityValueInCache(AuthorityValue value) {
        AuthorityValue updated = generateRaw(value.getId(), value.getValue(), value.getCategory());
        if (updated != null) {
            updated.setCreationDate(value.getCreationDate());
            //Check if we have newer information compared to what we have in the cache
            if (updated.hasTheSameInformationAs(value)) {
                updated.setLastModified(value.getLastModified());
            } else {
                updated.updateLastModifiedDate();
                writeAuthorityValueToCache(updated);
            }
        }
        return updated;
    }

    /**
     * Find an authority value by authority ID.
     * Item.ANY does not work here.
     *
     * @param context     the DSpace context
     * @param authorityID the authority ID
     * @return the authority value with the provided authority ID
     */
    @Override
    public AuthorityValue findCachedAuthorityValueByAuthorityID(Context context, String authorityID) {
        //Ensure that if we use the full identifier to match on
        String queryString = "id:\"" + authorityID + "\"";
        List<AuthorityValue> findings = find(context, queryString);
        return findings.size() > 0 ? findings.get(0) : null;
    }

    /**
     * Find an authority value by authority ID.
     * Item.ANY does not work here.
     *
     * @param context     the DSpace context
     * @param authorityID the authority ID
     * @param category    A field that should be present in the returning result. "field:*" check is simply done
     * @return the authority value with the provided authority ID
     */
    @Override
    public AuthorityValue findCachedAuthorityValueByAuthorityID(Context context, String authorityID, AuthorityCategory category) {
        //Ensure that if we use the full identifier to match on
        String queryString = "id:\"" + authorityID + "\" AND (field:* OR authority_category:*)";

        List<AuthorityValue> findings = find(context, queryString);
        return findings.size() > 0 ? findings.get(0) : null;
    }

    @Override
    public List<AuthorityValue> findCachedAuthorityValuesByValue(Context context, AuthorityCategory category, String value) {
        String queryString = "value:\"" + value + "\" AND authority_category:" + category.toString();
        return find(context, queryString);
    }

    @Override
    public List<AuthorityValue> findCachedAuthorityValuesByFieldValue(Context context, String field, String value){
        String queryString = field + ":\"" + value + "\"";
        return find(context, queryString);
    }

    /**
     * Find authority values by their value. An authority is only returned if the authority's value exactly matches the provided value.
     *
     * @param context  the dspace context
     * @param category the authority category
     * @param value    the exact value
     * @return a list of authority values with a value that exactly matches the provided value.
     */
    @Override
    public List<AuthorityValue> findCachedAuthorityValuesByExactValue(Context context, AuthorityCategory category, String value) {
        String queryString = "value_keyword:\"" + value + "\" AND authority_category:" + category.toString();
        return find(context, queryString);
    }

    /**
     * Get all indexed authority values
     *
     * @param context the DSpace context
     * @return a list of all indexed authority values
     */
    @Override
    public List<AuthorityValue> findAllCachedAuthorityValues(Context context) {
        String queryString = "*:*";
        int rows = Integer.MAX_VALUE;
        int start = 0;
        boolean hasMore = true;
        List<AuthorityValue> allAuthorityValues = new ArrayList<>();

        while (hasMore) {
            List<AuthorityValue> authorityValuesPart = find(context, queryString, start, rows);

            if (authorityValuesPart.size() < rows) {
                hasMore = false;
            }

            allAuthorityValues.addAll(authorityValuesPart);
            start += rows;
        }

        return allAuthorityValues;
    }

    @Override
    public List<AuthorityValue> findCachedAuthorityValuesByCategory(Context context, AuthorityCategory category) {
        return findCachedAuthorityValuesByCategory(context, category, null, 0, Integer.MAX_VALUE);

    }

    @Override
    public List<AuthorityValue> findCachedAuthorityValuesByCategory(Context context, AuthorityCategory category, String order, int start, int rows) {
        String queryString = "authority_category:" + category;
        SolrQuery.SortClause clause = null;

        if (StringUtils.isNotBlank(order)) {
            clause = new SolrQuery.SortClause("value_keyword", SolrQuery.ORDER.valueOf(order.toLowerCase()));
        }
        return find(context, queryString, start, rows, clause);
    }

    @Override
    public List<AuthorityValue> findAllAuthorityValuesByCategory(Context context, AuthorityCategory category) {
        String queryString = "authority_category:" + category;
        return find(context, queryString, 0,  Integer.MAX_VALUE);
    }

    /**
     * Retrieve external results for the provided authority field with the provided text.
     * The returned list will contain non cached AuthorityValue objects
     *
     * @return a list of authority values containing the external results
     */
    @Override
    public long getTotalNumberOfResultsForCategory(Context context, AuthorityCategory category) {
        String queryString = "authority_category:" + category;

        SolrQuery solrQuery = new SolrQuery();
        try {
            solrQuery.setQuery(filtered(queryString));
            solrQuery.setStart(0);
            solrQuery.setRows(0);

            log.debug("AuthorityValueFinder makes the query: " + queryString);
            QueryResponse queryResponse = SolrAuthority.getSearchService().search(solrQuery);
            return queryResponse.getResults().getNumFound();
        } catch (Exception e) {
            log.error(LogManager.getHeader(context, "Error while retrieving AuthorityValue from solr", "query: " + queryString), e);
        }
        return 0;
    }

    @Override
    public List<AuthorityValue> findCachedAuthorityValuesByCategoryStartsWith(Context context, AuthorityCategory category, String startsWith, String order, int start, int rows) {
        String queryString = "authority_category:" + category + " AND value_keyword:" + startsWith + "*";
        SolrQuery.SortClause clause = null;
        if (StringUtils.isNotBlank(order)) {
            clause = new SolrQuery.SortClause("value_keyword", SolrQuery.ORDER.valueOf(order.toLowerCase()));
            //queryString += " AND value " + order;
        }
        return find(context, queryString, start, rows, clause);
    }

    public long getTotalNumberOfFilteredResults(Context context, AuthorityCategory category, String startsWith) {
        String queryString = "authority_category:" + category + " AND value_keyword:" + startsWith + "*";
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(filtered(queryString));
            solrQuery.setStart(0);
            solrQuery.setRows(0);

            log.debug("AuthorityValueFinder makes the query: " + queryString);
            QueryResponse queryResponse = SolrAuthority.getSearchService().search(solrQuery);
            if (queryResponse != null && queryResponse.getResults() != null) {
                return queryResponse.getResults().getNumFound();
            }
        } catch (Exception e) {
            log.error(LogManager.getHeader(context, "Error while retrieving number of AuthorityValues from solr", "query: " + queryString), e);
        }

        return 0;
    }

    /**
     * Retrieve external results for the provided authority field with the provided text.
     * The returned list will contain non cached AuthorityValue objects
     *
     * @param category the authority controlled field
     * @param text     the text used to find the external results
     * @param max      the maximum amount of results
     * @return a list of authority values containing the external results
     */
    @Override
    public List<AuthorityValue> retrieveExternalResults(AuthorityCategory category, String text, int max) {
        return authorityValueFactory.retrieveExternalResults(category, text, max);

    }

    /**
     * Get the authority value from the provided solr document
     *
     * @param solrDocument the solr document
     * @return the authority value from the provided solr document
     */
    @Override
    public AuthorityValue getAuthorityValueFromSolrDoc(SolrDocument solrDocument) {
        AuthorityCategory category = AuthorityCategory.fromString((String) solrDocument.getFieldValue("authority_category"));
        if (category == null) {
            category = getCategory((String) solrDocument.getFieldValue("field"));
        }
        return authorityValueFactory.loadAuthorityValue(category, solrDocument);
    }

    protected List<AuthorityValue> find(Context context, String queryString) {
        return find(context, queryString, 0, 10);
    }

    /**
     * Query solr for authory values
     *
     * @param context     the DSpace context
     * @param queryString the solr query string
     * @param start       the offset
     * @param rows        the amount of returned authority values
     * @return
     */
    protected List<AuthorityValue> find(Context context, String queryString, int start, int rows) {

        return find(context, queryString, start, rows, null);
    }

    protected List<AuthorityValue> find(Context context, String queryString, int start, int rows, SolrQuery.SortClause sort) {
        List<AuthorityValue> findings = new ArrayList<AuthorityValue>();
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(filtered(queryString));
            solrQuery.setStart(start);
            solrQuery.setRows(rows);
            if (sort != null) {
                solrQuery.setSort(sort);
            }
            log.debug("AuthorityValueFinder makes the query: " + queryString);
            QueryResponse queryResponse = SolrAuthority.getSearchService().search(solrQuery);
            if (queryResponse != null && queryResponse.getResults() != null && 0 < queryResponse.getResults().getNumFound()) {
                for (SolrDocument document : queryResponse.getResults()) {
                    AuthorityValue authorityValue = getAuthorityValueFromSolrDoc(document, "orcid_id");
                    if (authorityValue != null) {
                        findings.add(authorityValue);
                        log.debug("AuthorityValueFinder found: " + authorityValue.getValue());
                    }
                }
            }
        } catch (Exception e) {
            log.error(LogManager.getHeader(context, "Error while retrieving AuthorityValue from solr", "query: " + queryString), e);
        }

        return findings;
    }

    protected String filtered(String queryString) throws InstantiationException, IllegalAccessException {
        String instanceFilter = "-deleted:true";
        if (StringUtils.isNotBlank(instanceFilter)) {
            queryString += " AND " + instanceFilter;
        }
        return queryString;
    }

    /**
     * This method looks at the authority of a metadata.
     * If the authority can be found in solr, that value is reused.
     * Otherwise a new authority value will be generated that will be indexed in solr.
     * If the authority starts with AuthorityValueGenerator.GENERATE, a specific type of AuthorityValue will be generated.
     * Depending on the type this may involve querying an external REST service
     *
     * @param category The category
     * @param value    Is one of the values of the given metadataField in one of the items being indexed.
     */
    public AuthorityValue writeMetadataInAuthorityCache(Context context, Item item, AuthorityCategory category, Metadatum value) {

        AuthorityValue nextValue = null;

        String content = value.value;
        String authorityKey = value.authority;
        //We only want to update our item IF our ID is not present or if we need to generate one.
        boolean requiresItemUpdate = StringUtils.isBlank(authorityKey) || AuthorityKeyRepresentation.isAuthorityKeyRepresentation(authorityKey);

        if (StringUtils.isNotBlank(authorityKey) && !AuthorityKeyRepresentation.isAuthorityKeyRepresentation(authorityKey)) {
            // !uid.startsWith(AuthorityValueGenerator.GENERATE) is not strictly necessary here but it prevents exceptions in solr
            nextValue = findCachedAuthorityValueByAuthorityID(context, authorityKey);
        }
        if (nextValue == null) {
            nextValue = createNonCachedAuthorityValue(context, authorityKey, content, category);
        }
        if (nextValue != null && requiresItemUpdate) {
            try {
                updateItemMetadataWithAuthority(context, item, value, nextValue);
                item.update();
            } catch (Exception e) {
                log.error("Error creating a metadata value's authority for item: " + item.getID(), e);
            }
        }

        return nextValue;
    }

    /**
     * Replace an item's metadata value with this authority
     *
     * @param context        the DSpace context
     * @param item           the item
     * @param value          the metadata value
     * @param authorityValue the authority value
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void updateItemMetadataWithAuthority(Context context, Item item, Metadatum value, AuthorityValue authorityValue) throws SQLException, AuthorizeException {
        Metadatum newValue = value.copy();
        newValue.value = authorityValue.getValue();
        newValue.authority = authorityValue.getId();
        item.replaceMetadataValue(value, newValue);

    }

    /**
     * Delete an authority index by it's authority ID
     *
     * @param id the authority ID
     * @throws Exception
     */
    @Override
    public void deleteAuthorityValueFromCacheById(String id) throws Exception {
        indexingService.deleteAuthorityValueById(id);
    }

//    @Override
//    public String getAuthorityTypeByField(String field) {
//        Map<String, AuthorityValueBuilder> authorityValueBuilders = authorityValueFactory.getAuthorityValueBuilders();
//
//        for (String authorityType : authorityValueBuilders.keySet()) {
//            AuthorityValueBuilder authorityValueBuilder = authorityValueBuilders.get(authorityType);
//
//            if (authorityValueBuilder.getMetadataFields().contains(field)) {
//                return authorityType;
//            }
//        }
//
//        return null;
//    }


    public List<AuthorityValue> findCachedAuthorityValuesByCategory(Context context, AuthorityCategory category, int page, int pageSize) {
        String queryString = "authority_category:" + category.toString();
        return find(context, queryString, page, pageSize);
    }

    private String fieldParameter(String schema, String element, String qualifier) {
        return schema + "_" + element + ((qualifier != null) ? "_" + qualifier : "");
    }

    /**
     * Check if a metadata field is authority controlled
     *
     * @param field The metadata field
     * @return true if the field is authority controlled
     */
    @Override
    public boolean isAuthorityControlledField(String field) {
        field = field.replace(".", "_");
        return authorityValueFactory.isAuthorityControlledField(field);
    }

    @Override
    public AuthorityCategory getCategory(String field) {
        field = field.replace(".", "_");
        return authorityValueFactory.getCategory(field);
    }

    public List<String> getMetadataFields(AuthorityCategory category) {
        List<String> keys = this.authorityValueFactory.getFieldKeys(category);
        List<String> fields = new ArrayList<>();
        for (String key : keys) {
            fields.add(key.replace("_", "."));
        }
        return fields;
    }

    public List<String> getAllMetadataFields() {
        List<String> keys = this.authorityValueFactory.getFieldKeys();
        List<String> fields = new ArrayList<>();
        for (String key : keys) {
            fields.add(key.replace("_", "."));
        }
        return fields;
    }


    public AuthorityValue generateAuthorityValue(AuthorityCategory category, String displayValue) {
        return authorityValueFactory.createAuthorityValue(category, displayValue);
    }

    public AuthorityValue getAuthorityValueFromSolrDoc(SolrDocument solrDocument, String identifierField) {
        String identifier = (String) solrDocument.getFieldValue(identifierField);

        AuthorityCategory category = AuthorityCategory.fromString((String) solrDocument.getFieldValue("authority_category"));
        if (category == null) {
            category = getCategory((String) solrDocument.getFieldValue("field"));
        }
        return authorityValueFactory.loadAuthorityValue(category, solrDocument, identifier);
    }
}
