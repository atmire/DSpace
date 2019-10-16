/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority.converter;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrDocument;
import org.dspace.content.authority.AuthorityValue;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.mock.MockMetadataValue;

public class AuthorityConverterImpl implements AuthorityConverter {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(AuthorityConverterImpl.class);

    @Override
    public AuthorityValue createFromSolr(SolrDocument solrDocument) {
        AuthorityValue authorityValue = new AuthorityValue();
        authorityValue.setSource(String.valueOf(solrDocument.getFieldValue("source")));
        authorityValue.setLastModified((Date) solrDocument.getFieldValue("last_modified_date"));
        authorityValue.setCreationDate((Date) solrDocument.getFieldValue("creation_date"));
        authorityValue
            .setExternalSourceIdentifier(String.valueOf(solrDocument.getFieldValue("external_source_identifier")));
        authorityValue.setCategory(String.valueOf(solrDocument.getFieldValue("category")));
        authorityValue.setValue(String.valueOf(solrDocument.getFieldValue("value")));

        List<MockMetadataValue> mockMetadataValueList = parseMetadataFromSolrDocument(solrDocument);
        authorityValue.setMetadata(mockMetadataValueList);
        return authorityValue;
    }

    private List<MockMetadataValue> parseMetadataFromSolrDocument(SolrDocument solrDocument) {
        List<MockMetadataValue> mockMetadataValueList = new LinkedList<>();
        for (String name : solrDocument.getFieldNames()) {
            if (StringUtils.startsWith(name, "metadata_") && !StringUtils.startsWith(name, "metadata_keyword_")) {
                String metadataFieldString = StringUtils.substringAfter(name, "metadata_");
                String value = String.valueOf(((List<String>) solrDocument.getFieldValue(name)).get(0));
                MockMetadataValue mockMetadataValue = new MockMetadataValue(metadataFieldString, value);
                mockMetadataValueList.add(mockMetadataValue);
            }
        }
        return mockMetadataValueList;
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
