package org.dspace.content.authority.converter;

import org.apache.solr.common.SolrDocument;
import org.dspace.content.authority.AuthorityValue;
import org.dspace.external.model.ExternalDataObject;

public interface AuthorityConverter {

    AuthorityValue createFromSolr(SolrDocument solrDocument);

    AuthorityValue createFromExternalData(String category, ExternalDataObject dataObject);

}
