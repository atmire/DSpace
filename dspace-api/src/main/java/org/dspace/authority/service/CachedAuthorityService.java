/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.service;

import org.apache.solr.common.SolrDocument;
import org.dspace.authority.AuthorityCategory;
import org.dspace.authority.AuthorityValue;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.Metadatum;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Service interface class for the Metadata Authority
 * The implementation of this class is responsible for all business logic calls for the Metadata Authority and is autowired by spring.
 *
 * @author kevinvandevelde at atmire.com
 * @author philip at atmire.com
 */
public interface CachedAuthorityService {
    /**
     * Store all item metadata authorities that are available in the metadata of the provided item in the cache.
     * @param context
     * the DSpace context
     * @param item
     * The item for which the authorities will be indexed
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void writeItemAuthorityMetadataValuesToCache(Context context, Item item) throws SQLException, AuthorizeException;

    public boolean isConfigurationValid();

    /**
     * Retrieve all authority values found in the authority cache. Returns a map with as key the identifier of the authority value
     * and as value the authority value.
     *
     * @param context the dspace context
     * @return A map containing all authority values
     * @throws SQLException
     * @throws AuthorizeException
     */
    public Map<String, AuthorityValue> getAllCachedAuthorityValues(Context context) throws SQLException, AuthorizeException;

    /**
     * Empty the authority cache.
     * @throws Exception
     */
    public void cleanCache() throws Exception;

    /**
     * Index an authority value.
     * @param authorityValue
     */
    public void writeAuthorityValueToCache(AuthorityValue authorityValue);

    public void writeAuthorityValueToCacheWithSolrId(AuthorityValue authorityValue);

    /**
     * Commit all changes to the authority index since the last commit.
     */
    public void commitAuthorityCache();

    /**
     * Check if a metadata field is authority controlled
     * @param field
     * The metadata field
     * @return
     * true if the field is authority controlled
     */
    public boolean isAuthorityControlledField(String field);

    /**
     * Create an authority value object. This object is not yet stored in the authority cache.
     * @param context The context
     * @param authorityKey The authority key
     * @param content The metadata value
     * @param field The metadata field
     * @return The new authority value object
     */
     public AuthorityValue createNonCachedAuthorityValue(Context context, String authorityKey, String content, String field);
     public AuthorityValue createNonCachedAuthorityValue(Context context, String authorityKey, String content, AuthorityCategory category);

    /**
     * Update the index of an authority value
     * @param value
     * the authority value to index
     * @return
     * the updated authority value
     */
    public AuthorityValue updateAuthorityValueInCache(AuthorityValue value);

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
    public AuthorityValue findCachedAuthorityValueByAuthorityID(Context context, String authorityID);

    public AuthorityValue findCachedAuthorityValueByAuthorityID(Context context, String authorityID, AuthorityCategory category);
    /**
     * Find authority values by their value.
     *
     * Matches are case insensitive with this method.
     * For case-sensitive findings, use findCachedAuthorityValuesByExactValue.
     *
     * @param context
     * the dspace context
     * @param category
     * the authority field
     * @param value
     * the exact value
     * @return
     * a list of authority values with a value that exactly matches the provided value.
     */
    public List<AuthorityValue> findCachedAuthorityValuesByValue(Context context, AuthorityCategory category, String value);

    public List<AuthorityValue> findCachedAuthorityValuesByFieldValue(Context context, String field, String value);

    /**
     * Find authority values by their value. An authority is only returned if the authority's value exactly matches the provided value.
     *
     * Matches are case sensitive with this method.
     * For case-insensitive findings, use findCachedAuthorityValuesByValue.
     *
     * @param context
     * the dspace context
     * @param category
     * the authority field
     * @param value
     * the exact value
     * @return
     * a list of authority values with a value that exactly matches the provided value.
     */
    public List<AuthorityValue> findCachedAuthorityValuesByExactValue(Context context, AuthorityCategory category, String value);

    /**
     * Get all indexed authority values
     * @param context
     * the DSpace context
     * @return
     * a list of all indexed authority values
     */
    public List<AuthorityValue> findAllCachedAuthorityValues(Context context);

    public List<AuthorityValue> findCachedAuthorityValuesByCategory(Context context, AuthorityCategory category);
    public List<AuthorityValue> findCachedAuthorityValuesByCategory(Context context, AuthorityCategory category, String order, int start, int rows);
    public long getTotalNumberOfResultsForCategory(Context context, AuthorityCategory category);

    public List<AuthorityValue> findAllAuthorityValuesByCategory(Context context, AuthorityCategory category);
    /**
     * Get the authority value from the provided solr document
     * @param solrDocument
     * the solr document
     * @return
     * the authority value from the provided solr document
     */
    public AuthorityValue getAuthorityValueFromSolrDoc(SolrDocument solrDocument);
    public List<AuthorityValue> findCachedAuthorityValuesByCategoryStartsWith(Context context, AuthorityCategory category, String startsWith, String order, int start, int rows);
    public long getTotalNumberOfFilteredResults(Context context, AuthorityCategory category, String startsWith);

    /**
     * Retrieve external results for the provided authority field with the provided text.
     * @param category
     * the authority controlled field
     * @param text
     * the text used to find the external results
     * @param max
     * the maximum amount of results
     * @return
     * a list of authority values containing the external results
     */
    public List<AuthorityValue> retrieveExternalResults(AuthorityCategory category, String text, int max);

    /**
     * This method looks at the authority of a metadata.
     * If the authority can be found in solr, that value is reused.
     * Otherwise a new authority value will be generated that will be indexed in solr.
     * If the authority starts with AuthorityValueGenerator.GENERATE, a specific type of AuthorityValue will be generated.
     * Depending on the type this may involve querying an external REST service
     *
     * @param category Is one of the fields defined in dspace.cfg to be indexed.
     * @param value         Is one of the values of the given metadataField in one of the items being indexed.
     */
    public AuthorityValue writeMetadataInAuthorityCache(Context context, Item item, AuthorityCategory category, Metadatum value);

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
    public void updateItemMetadataWithAuthority(Context context, Item item, Metadatum value, AuthorityValue authorityValue) throws SQLException, AuthorizeException;

    /**
     * Delete an authority index by it's authority ID
     * @param id
     * the authority ID
     * @throws Exception
     */
    public void deleteAuthorityValueFromCacheById(String id) throws Exception;


    public List<AuthorityValue> findCachedAuthorityValuesByCategory(Context context, AuthorityCategory category, int page, int pageSize);

    AuthorityCategory getCategory(String field);

    List<String> getMetadataFields(AuthorityCategory category);

    List<String> getAllMetadataFields();

    AuthorityValue generateAuthorityValue(AuthorityCategory category, String displayValue);

    AuthorityValue getAuthorityValueFromSolrDoc(SolrDocument solrDocument, String orcidId);
}

