package org.dspace.importer.external.metadatamapping.contributor;

import org.apache.axiom.om.OMElement;
import org.dspace.importer.external.metadatamapping.MetadataFieldConfig;
import org.dspace.importer.external.metadatamapping.MetadataFieldMapping;
import org.dspace.importer.external.metadatamapping.MetadatumDTO;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lotte on 22/04/16.
 */
public class UrlMetadatumContributor extends SimpleXpathMetadatumContributor {
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    @Required
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Collection<MetadatumDTO> contributeMetadata(OMElement t) {
        Collection<MetadatumDTO> values = super.contributeMetadata(t);
        for(MetadatumDTO metadatumDTO : values) {
            metadatumDTO.setValue(baseUrl + metadatumDTO.getValue());
        }
        return values;
    }
}