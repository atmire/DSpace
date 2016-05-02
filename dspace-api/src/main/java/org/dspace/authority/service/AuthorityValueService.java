/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.service;

import java.sql.*;
import java.util.*;
import org.apache.solr.common.*;
import org.dspace.authority.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.core.*;

/**
 * This service contains all methods for using authority values
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public interface AuthorityValueService
{
    /**
     * Create an authority value object. This object is not yet stored in the authority cache.
     * @param context The context
     * @param authorityKey The authority key
     * @param content The metadata value
     * @param field The metadata field
     * @return The new authority value object
     */
    public AuthorityValue createAuthorityValue(Context context, String authorityKey, String content, String field);

    public AuthorityValue updateAuthorityValue(AuthorityValue value);

    public AuthorityValue findAuthorityValueByID(Context context, String authorityID);

    public List<AuthorityValue> findAuthorityValuesByExactValue(Context context, String field, String value);

    public List<AuthorityValue> findAllAuthorityValues(Context context);

    public AuthorityValue getAuthorityValueFromSolrDoc(SolrDocument solrDocument);

    public List<AuthorityValue> retrieveExternalResults(String field, String text, int max);

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
    public AuthorityValue storeMetadataInAuthorityCache(Context context, Item item, String metadataField, MetadataValue value);

    /**
     * Replace an item's DCValue with this authority
     * @param context
     * @param item
     * @param value
     * @param authorityValue
     * @throws SQLException
     * @throws AuthorizeException
     */
    public void updateItemMetadataWithAuthority(Context context, Item item, MetadataValue value, AuthorityValue authorityValue) throws SQLException, AuthorizeException;

    public void deleteAuthorityValueById(String id) throws Exception;
}
