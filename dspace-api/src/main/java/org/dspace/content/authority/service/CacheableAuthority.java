package org.dspace.content.authority.service;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.dspace.external.model.ExternalDataObject;

public interface CacheableAuthority {

    void cacheAuthorityValue(String metadataField, ExternalDataObject externalDataObject)
        throws IOException, SolrServerException;

}
