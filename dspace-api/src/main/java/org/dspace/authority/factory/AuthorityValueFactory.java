package org.dspace.authority.factory;

import org.apache.solr.common.SolrDocument;
import org.dspace.authority.AuthorityValue;
import org.dspace.services.factory.DSpaceServicesFactory;

import java.util.List;

/**
 * abstract factory to create AuthorityValues
 *
 * @author kevinvandevelde at atmire.com
 * @author philip at atmire.com
 */
public abstract class AuthorityValueFactory
{

    public abstract AuthorityValue createAuthorityValue(String field, String content);

    /**
     * Create a new authority value with the provided identifier. The idenitier can be an identifier from an external system (ORCID)
     * or NULL in case of a local value.
     * @param type
     * @param identifier
     * @return
     */
    public abstract AuthorityValue createAuthorityValue(String type, String identifier, String content);

    /**
     * Load an intsance from an authority value from the solr document
     * @param type
     * @param solrDocument
     * @return
     */
    public abstract AuthorityValue loadAuthorityValue(String type, SolrDocument solrDocument);


    public static AuthorityValueFactory getInstance()
    {
        return DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName("authorityValueFactory", AuthorityValueFactory.class);
    }

    /**
     * Retrieve external results for the provided authority field with the provided text.
     * @param field
     * the authority controlled field
     * @param text
     * the text used to find the external results
     * @param max
     * the maximum amount of results
     * @return
     * a list of authority values containing the external results
     */
    public abstract List<AuthorityValue> retrieveExternalResults(String field, String text, int max);
}
