/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority;
import java.sql.*;
import java.util.*;
import java.util.Date;
import org.apache.commons.lang.*;
import org.apache.log4j.*;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.response.*;
import org.apache.solr.common.*;
import org.dspace.authority.factory.*;
import org.dspace.authority.indexer.*;
import org.dspace.authority.service.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.content.authority.*;
import org.dspace.content.service.*;
import org.dspace.core.*;
import org.dspace.core.LogManager;
import org.springframework.beans.factory.annotation.*;

/**
 * @author kevinvandevelde at atmire.com
 * @author philip at atmire.com
 */
public class CachedAuthorityServiceImpl implements CachedAuthorityService {

    private final Logger log = Logger.getLogger(CachedAuthorityServiceImpl.class);

    @Autowired(required = true)
    protected AuthorityServiceFactory authorityServiceFactory;

    @Autowired
    protected AuthorityValueFactory authorityValueFactory;

    @Autowired(required = true)
    protected ItemService itemService;

    @Autowired(required = true)
    protected AuthorityIndexingService indexingService;

    /** The prefix of the authority controlled field */
    protected static final String AC_PREFIX = "authority.controlled.";

    protected Set<String> authorityControlled;

    /**
     * Index all authorities that are available in the metadata of the provided item.
     * @param context
     * the DSpace context
     * @param item
     * The item for which the authorities will be indexed
     * @throws SQLException
     * @throws AuthorizeException
     */
    @Override
    public void writeItemAuthorityMetadataValuesToCache(Context context, Item item) throws SQLException, AuthorizeException {
        if(!isConfigurationValid()){
            //Cannot index, configuration not valid
            return;
        }


        for (AuthorityIndexerInterface indexers : authorityServiceFactory.createAuthorityIndexers(context, item)) {
            while (indexers.hasMore())
            {
                AuthorityValue authorityValue = indexers.nextValue();
                if(authorityValue != null)
                    indexingService.indexContent(authorityValue);
            }
            //Close up
            indexers.close();
        }
        //Commit to our server
        indexingService.commit();
    }

    @Override
    public boolean isConfigurationValid()
    {
        if(!indexingService.isConfiguredProperly()){
            return false;
        }

        for (AuthorityIndexerInterface indexerInterface : authorityServiceFactory.createUninitialisedAuthorityIndexers())
        {
            if(!indexerInterface.isConfiguredProperly()){
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieve all authority values found in the authority cache. Returns a map with as key the identifier of the authority value
     * and as value the authority value.
     *
     * @param context
     * the dspace context
     * @return
     * A map containing all authority values
     * @throws SQLException
     * @throws AuthorizeException
     */
    public Map<String, AuthorityValue> getAllCachedAuthorityValues(Context context) throws SQLException, AuthorizeException {
        Map<String, AuthorityValue> toIndexValues = new HashMap<>();

        for (AuthorityIndexerInterface indexerInterface : authorityServiceFactory.createAuthorityIndexers(context))
        {
            while (indexerInterface.hasMore()) {
                AuthorityValue authorityValue = indexerInterface.nextValue();
                if(authorityValue != null){
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
     * @throws Exception
     */
    @Override
    public void cleanCache() throws Exception
    {
        indexingService.cleanIndex();
    }

    /**
     * Index an authority value.
     * @param authorityValue
     */
    @Override
    public void writeAuthorityValueToCache(AuthorityValue authorityValue)
    {
        indexingService.indexContent(authorityValue);
    }

    /**
     * Commit all changes to the authority index since the last commit.
     */
    @Override
    public void commitAuthorityCache() {
        indexingService.commit();
    }

    /**
     * Check if a metadata field is authority controlled
     * @param field
     * The metadata field
     * @return
     * true if the field is authority controlled
     */
    @Override
    public boolean isAuthorityControlledField(String field) {
        if(authorityControlled==null){
            setAuthorizedMetadataFields();
        }

        return authorityControlled.contains(field);
    }

    /**
     * Set authority controlled fields
     *
     */
    private void setAuthorizedMetadataFields()
    {
        authorityControlled = new HashSet<String>();
        Enumeration propertyNames = ConfigurationManager.getProperties().propertyNames();
        while(propertyNames.hasMoreElements())
        {
            String key = ((String) propertyNames.nextElement()).trim();
            if (key.startsWith(AC_PREFIX)
                    && ConfigurationManager.getBooleanProperty(key, false))
            {
                authorityControlled.add(key.substring(AC_PREFIX.length()));
            }
        }
    }

    /**
     * Create an authority value object. This object is not yet stored in the authority cache.
     * @param context The context
     * @param authorityKey The authority key
     * @param content The metadata value
     * @param field The metadata field
     * @return The new authority value object
     */
    @Override
    public AuthorityValue createNonCachedAuthorityValue(Context context, String authorityKey, String content, String field) {
        AuthorityValue nextValue = generateRaw(authorityKey, content, field);
        nextValue.updateLastModifiedDate();
        nextValue.setCreationDate(new Date());
        nextValue.setField(field);

        return nextValue;
    }

    /**
     * Create a raw authority value object.
     * @param authorityKey The authority key
     * @param content The metadata value
     * @param field The metadata field
     * @return
     */
    protected AuthorityValue generateRaw(String authorityKey, String content, String field) {
        AuthorityValue nextValue;

        AuthorityKeyRepresentation authorityKeyRepresentation = new AuthorityKeyRepresentation(authorityKey);
        if (StringUtils.isNotBlank(authorityKeyRepresentation.getInternalIdentifier())) {
            nextValue = authorityValueFactory.createAuthorityValue(authorityKeyRepresentation.getAuthorityType(), authorityKeyRepresentation.getInternalIdentifier(), content);
        } else {
            nextValue = authorityValueFactory.createAuthorityValue(field, content);
        }
        return nextValue;
    }

    /**
     * Update the index of an authority value
     * @param value
     * the authority value to index
     * @return
     * the updated authority value
     */
    @Override
    public AuthorityValue updateAuthorityValueInCache(AuthorityValue value) {
        AuthorityValue updated = generateRaw(value.generateString(), value.getValue(), value.getField());
        if (updated != null)
        {
            updated.setCreationDate(value.getCreationDate());
            updated.setField(value.getField());
            //Check if we have newer information compared to what we have in the cache
            if (updated.hasTheSameInformationAs(value))
            {
                updated.setLastModified(value.getLastModified());
            }
            else
            {
                updated.updateLastModifiedDate();
                writeAuthorityValueToCache(updated);
            }
        }
        return updated;
    }

    /**
     * Find an authority value by authority ID.
     * Item.ANY does not work here.
     * @param context
     * the DSpace context
     * @param authorityID
     * the authority ID
     * @return
     * the authority value with the provided authority ID
     */
    @Override
    public AuthorityValue findCachedAuthorityValueByAuthorityID(Context context, String authorityID) {
        //Ensure that if we use the full identifier to match on
        String queryString = "id:\"" + authorityID + "\"";
        List<AuthorityValue> findings = find(context, queryString);
        return findings.size() > 0 ? findings.get(0) : null;
    }

    /**
     * Find authority values by their value. An authority is only returned if the authority's value exactly matches the provided value.
     * @param context
     * the dspace context
     * @param field
     * the authority field
     * @param value
     * the exact value
     * @return
     * a list of authority values with a value that exactly matches the provided value.
     */
    @Override
    public List<AuthorityValue> findCachedAuthorityValuesByExactValue(Context context, String field, String value) {
        String queryString = "value_keyword:\"" + value + "\" AND field:" + field;
        return find(context, queryString);
    }

    /**
     * Get all indexed authority values
     * @param context
     * the DSpace context
     * @return
     * a list of all indexed authority values
     */
    @Override
    public List<AuthorityValue> findAllCachedAuthorityValues(Context context) {
        String queryString = "*:*";
        int rows = 1000;
        int start = 0;
        boolean hasMore = true;
        List<AuthorityValue> allAuthorityValues = new ArrayList<>();

        while(hasMore){
            List<AuthorityValue> authorityValuesPart = find(context, queryString, start, rows);

            if (authorityValuesPart.size()<rows) {
                hasMore = false;
            }

            allAuthorityValues.addAll(authorityValuesPart);
            start+=rows;
        }

        return allAuthorityValues;
    }

    /**
     * Retrieve external results for the provided authority field with the provided text.
     * The returned list will contain non cached AuthorityValue objects
     *
     * @param field
     * the authority controlled field
     * @param text
     * the text used to find the external results
     * @param max
     * the maximum amount of results
     * @return
     * a list of authority values containing the external results
     */
    @Override
    public List<AuthorityValue> retrieveExternalResults(String field, String text, int max)
    {
        return authorityValueFactory.retrieveExternalResults(field, text, max);

    }

    /**
     * Get the authority value from the provided solr document
     * @param solrDocument
     * the solr document
     * @return
     * the authority value from the provided solr document
     */
    @Override
    public AuthorityValue getAuthorityValueFromSolrDoc(SolrDocument solrDocument) {
        String type = (String) solrDocument.getFieldValue("authority_type");
        return authorityValueFactory.loadAuthorityValue(type, solrDocument);
    }

    protected List<AuthorityValue> find(Context context, String queryString){
        return find(context,queryString,0,10);
    }

    /**
     * Query solr for authory values
     * @param context
     * the DSpace context
     * @param queryString
     * the solr query string
     * @param start
     * the offset
     * @param rows
     * the amount of returned authority values
     * @return
     */
    protected List<AuthorityValue> find(Context context, String queryString, int start, int rows) {
        List<AuthorityValue> findings = new ArrayList<AuthorityValue>();
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(filtered(queryString));
            solrQuery.setStart(start);
            solrQuery.setRows(rows);
            log.debug("AuthorityValueFinder makes the query: " + queryString);
            QueryResponse queryResponse = SolrAuthority.getSearchService().search(solrQuery);
            if (queryResponse != null && queryResponse.getResults() != null && 0 < queryResponse.getResults().getNumFound()) {
                for (SolrDocument document : queryResponse.getResults()) {
                    AuthorityValue authorityValue = getAuthorityValueFromSolrDoc(document);
                    findings.add(authorityValue);
                    log.debug("AuthorityValueFinder found: " + authorityValue.getValue());
                }
            }
        } catch (Exception e) {
            log.error(LogManager.getHeader(context, "Error while retrieving AuthorityValue from solr", "query: " + queryString),e);
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
     * @param metadataField Is one of the fields defined in dspace.cfg to be indexed.
     * @param value         Is one of the values of the given metadataField in one of the items being indexed.
     */
    public AuthorityValue writeMetadataInAuthorityCache(Context context, Item item, String metadataField, MetadataValue value)  {

        AuthorityValue nextValue = null;

        String content = value.getValue();
        String authorityKey = value.getAuthority();
        //We only want to update our item IF our ID is not present or if we need to generate one.
        boolean requiresItemUpdate = StringUtils.isBlank(authorityKey) || AuthorityKeyRepresentation.isAuthorityKeyRepresentation(authorityKey);

        if (StringUtils.isNotBlank(authorityKey) && !AuthorityKeyRepresentation.isAuthorityKeyRepresentation(authorityKey)) {
            // !uid.startsWith(AuthorityValueGenerator.GENERATE) is not strictly necessary here but it prevents exceptions in solr
            nextValue = findCachedAuthorityValueByAuthorityID(context, authorityKey);
        }
        if (nextValue == null) {
            nextValue = createNonCachedAuthorityValue(context, authorityKey, content, metadataField.replaceAll("\\.", "_"));
        }
        if (nextValue != null && requiresItemUpdate) {
            try {
                updateItemMetadataWithAuthority(context, item, value, nextValue);
                itemService.update(context, item);
            } catch (Exception e) {
                log.error("Error creating a metadata value's authority for item: " + item.getID(), e);
            }
        }

        return nextValue;
    }

    /**
     * Replace an item's metadata value with this authority
     * @param context
     * the DSpace context
     * @param item
     * the item
     * @param value
     * the metadata value
     * @param authorityValue
     * the authority value
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void updateItemMetadataWithAuthority(Context context, Item item, MetadataValue value, AuthorityValue authorityValue) throws SQLException, AuthorizeException {
        value.setValue(authorityValue.getValue());
        value.setAuthority(authorityValue.getId());
        itemService.update(context, item);
    }

    /**
     * Delete an authority index by it's authority ID
     * @param id
     * the authority ID
     * @throws Exception
     */
    @Override
    public void deleteAuthorityValueFromCacheById(String id) throws Exception {
        indexingService.deleteAuthorityValueById(id);
    }
}
