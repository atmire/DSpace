/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority.service;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.external.model.ExternalDataObject;

/**
 * This serves as an interface to expose methods that allow us to write AuthorityValue objects to Solr
 */
public interface CacheableAuthority {

    /**
     * This method takes a String metadatafield and an ExternalDataObject and it'll convert these into an
     * AuthorityValue object. This AuthorityValue object will be parsed to a Solr input document and this will then
     * be saved into Solr
     * @param metadataField         The metadata field for which the ExternalDataObject exists
     * @param externalDataObject    The ExternalDataObject to be converted and saved
     * @throws IOException          If something goes wrong
     * @throws SolrServerException  If something goes wrong
     */
    void cacheAuthorityValue(String metadataField, ExternalDataObject externalDataObject)
        throws IOException, SolrServerException;

}
