/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority.converter;

import java.util.Date;

import org.apache.solr.common.SolrDocument;
import org.dspace.content.authority.AuthorityValue;
import org.dspace.external.model.ExternalDataObject;

public class AuthorityConverterImpl implements AuthorityConverter {

    @Override
    public AuthorityValue createFromSolr(SolrDocument solrDocument) {
        return null;
    }

    @Override
    public AuthorityValue createFromExternalData(String category, ExternalDataObject externalDataObject) {
        AuthorityValue authorityValue = new AuthorityValue();
        authorityValue.setLastModified(new Date());
        authorityValue.setCreationDate(new Date());
        authorityValue.setSource(externalDataObject.getSource());
        authorityValue.setExternalSourceIdentifier(externalDataObject.getSource());
        authorityValue.setCategory(category);
        authorityValue.setValue(externalDataObject.getValue());
        authorityValue.setMetadata(externalDataObject.getMetadata());
        return authorityValue;
    }
}
