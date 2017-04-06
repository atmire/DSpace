/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.factory;

import org.apache.solr.common.SolrDocument;
import org.dspace.authority.AuthorityCategory;
import org.dspace.authority.AuthorityValue;
import org.dspace.utils.DSpace;

import java.util.List;

/**
 * abstract factory to create AuthorityValues
 *
 * @author kevinvandevelde at atmire.com
 * @author philip at atmire.com
 */
public abstract class AuthorityValueFactory
{

    public abstract AuthorityValue createAuthorityValueFromField(String field, String content);
    public abstract AuthorityValue createAuthorityValue(AuthorityCategory category, String content);

    /**
     * Create a new authority value with the provided identifier. The identifier can be an identifier from an external system (ORCID)
     * or NULL in case of a local value.
     * @param category
     * the authority category
     * @param identifier
     * the external identifier
     * @return
     * the created AuthorityValue
     */
    public abstract AuthorityValue createAuthorityValue(AuthorityCategory category, String identifier, String content);

    /**
     * Load an instance of an authority value from a provided solr document
     * @param category
     * the authority category
     * @param solrDocument
     * the solr document
     * @return
     * the created AuthorityValue
     */
    public abstract AuthorityValue loadAuthorityValue(AuthorityCategory category, SolrDocument solrDocument);


    public static AuthorityValueFactory getInstance()
    {
        return new DSpace().getServiceManager().getServiceByName("authorityValueFactory", AuthorityValueFactory.class);
    }

    public abstract AuthorityValue loadAuthorityValue(AuthorityCategory category, SolrDocument solrDocument, String identifier);

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
    public abstract List<AuthorityValue> retrieveExternalResults(AuthorityCategory category, String text, int max);

    public abstract boolean isAuthorityControlledField(String field);

    public abstract List<String> getFieldKeys(AuthorityCategory category);

    public abstract AuthorityCategory getCategory(String field);

    public abstract List<String> getFieldKeys();
}
