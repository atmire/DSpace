package org.dspace.content.authority.converter;

import java.util.Date;

import org.apache.solr.common.SolrDocument;
import org.dspace.content.authority.AuthorityValue;
import org.dspace.external.model.ExternalDataObject;

public class AuthorityConverterImpl implements AuthorityConverter {

    public AuthorityValue createFromSolr(SolrDocument solrDocument) {
        return null;
    }

    public AuthorityValue createFromExternalData(String category, ExternalDataObject externalDataObject) {
        AuthorityValue authorityValue = new AuthorityValue();
        authorityValue.setLastModified(new Date());
        authorityValue.setSource(externalDataObject.getSource());
        authorityValue.setExternalSourceIdentifier(externalDataObject.getSource());
        authorityValue.setCategory(category);
        authorityValue.setValue(externalDataObject.getValue());
        authorityValue.setCreationDate(new Date());
        authorityValue.setMetadata(externalDataObject.getMetadata());
        return authorityValue;
    }
}
