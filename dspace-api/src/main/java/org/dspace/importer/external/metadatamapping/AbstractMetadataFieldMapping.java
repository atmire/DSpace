/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.importer.external.metadatamapping;


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.dspace.importer.external.metadatamapping.contributor.MetadataContributor;
import org.dspace.importer.external.metadatamapping.service.MetadataProcessorService;
import org.springframework.beans.factory.annotation.Required;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by Roeland Dillen (roeland at atmire dot com)
 * Date: 19/09/12
 * Time: 10:09
 */
public abstract class AbstractMetadataFieldMapping<RecordType> implements MetadataFieldMapping<RecordType, MetadataContributor<RecordType>> {

    private Map<MetadataFieldConfig, MetadataContributor<RecordType>> metadataFieldMap;
    private MetadataContributor identifierContributor;
    /**
     * log4j logger
     */
    private static Logger log = Logger.getLogger(AbstractMetadataFieldMapping.class);

    private Map<MetadataFieldConfig, MetadataProcessorService> metadataProcessorMap;

    public void setMetadataProcessorMap(Map<MetadataFieldConfig, MetadataProcessorService> metadataProcessorMap)
    {
        this.metadataProcessorMap = metadataProcessorMap;
    }

    public MetadataProcessorService getMetadataProcessor(MetadataFieldConfig metadataField)
    {
        if(metadataProcessorMap != null)
        {
            return metadataProcessorMap.get(metadataField);
        }else{
            return null;
        }
    }

    public MetadataContributor getIdentifierContributor() {
        return identifierContributor;
    }

    @Required
    public void setIdentifierContributor(MetadataContributor identifierContributor) {
        this.identifierContributor = identifierContributor;
    }

    public MetadatumDTO toDCValue(MetadataFieldConfig field, String value) {
        MetadatumDTO dcValue = new MetadatumDTO();



        if (field == null) return null;
        MetadataProcessorService metadataProcessor = getMetadataProcessor(field);
        if(metadataProcessor != null)
        {
            value = metadataProcessor.processMetadataValue(value);
        }
        dcValue.setValue(value);
        dcValue.setElement(field.getElement());
        dcValue.setQualifier(field.getQualifier());
        dcValue.setSchema(field.getSchema());
        return dcValue;
    }

    private boolean reverseDifferent = false;

    private String AND = "AND";
    private String OR = "OR";
    private String NOT = "NOT";

    public String getAND() {
        return AND;
    }

    public void setAND(String AND) {
        this.AND = AND;
    }

    public String getOR() {
        return OR;
    }

    public void setOR(String OR) {
        this.OR = OR;
    }

    public String getNOT() {
        return NOT;
    }

    public void setNOT(String NOT) {
        this.NOT = NOT;
    }

    public Map<MetadataFieldConfig, MetadataContributor<RecordType>> getMetadataFieldMap() {
        return metadataFieldMap;
    }

    public void setMetadataFieldMap(Map<MetadataFieldConfig, MetadataContributor<RecordType>> metadataFieldMap) {
        this.metadataFieldMap = metadataFieldMap;
        for(MetadataContributor<RecordType> mc:metadataFieldMap.values()){
            mc.setMetadataFieldMapping(this);
        }

    }

    public Pair<String, String> getIdentifier(RecordType recordType, String source) {
        Iterator<MetadatumDTO> iterator = getIdentifierContributor().contributeMetadata(recordType).iterator();
        Pair<String, String> result = null;
        if (iterator.hasNext()) {
            result = new ImmutablePair<>(source, iterator.next().getValue());
        }

        return result;
    }

    @Override
    public Collection<MetadatumDTO> resultToDCValueMapping(RecordType record) {
        List<MetadatumDTO> values=new LinkedList<MetadatumDTO>();


        for(MetadataContributor<RecordType> query:getMetadataFieldMap().values()){
            try {
                values.addAll(query.contributeMetadata(record));
            } catch (Exception e) {
                log.error("Error", e);
            }

        }
        return values;

    }
}
