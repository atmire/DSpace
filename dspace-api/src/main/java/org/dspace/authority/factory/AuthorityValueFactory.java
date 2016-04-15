package org.dspace.authority.factory;

import org.apache.solr.common.SolrDocument;
import org.dspace.authority.AuthorityValue;
import org.dspace.services.factory.DSpaceServicesFactory;

import java.util.List;

/**
 * User: kevin (kevin at atmire.com)
 * Date: 7/04/16
 * Time: 16:47
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

    public abstract AuthorityValue createEmptyAuthorityValue(String type);

    public abstract AuthorityValue createEmptyAuthorityValueFromHeader(String type);

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

    public abstract List<AuthorityValue> retrieveExternalResults(String field, String text, int max);
}
