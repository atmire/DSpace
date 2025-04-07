/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.importer.external.metadatamapping.contributor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.dspace.importer.external.metadatamapping.MetadataFieldConfig;
import org.dspace.importer.external.metadatamapping.MetadataFieldMapping;
import org.dspace.importer.external.metadatamapping.MetadatumDTO;

/**
 * Wrapper class to combine the abstract into a single field if it is separated out into multiple
 * <AbstractText> fields
 *
 */
public class CombinedAbstractMetadatumContributor<T> implements MetadataContributor<T> {

    private MetadataFieldMapping<T, MetadataContributor<T>> metadataFieldMapping;
    private MetadataContributor contributor;
    private MetadataFieldConfig field;
    private String separator;

    /**
     * Set the metadatafieldMapping used in the transforming of a record to actual metadata
     *
     * @param metadataFieldMapping the new mapping.
     */
    @Override
    public void setMetadataFieldMapping(MetadataFieldMapping<T, MetadataContributor<T>> metadataFieldMapping) {
        this.metadataFieldMapping = metadataFieldMapping;
        this.contributor.setMetadataFieldMapping(metadataFieldMapping);
    }

    /**
     * Uses the configured contributor to do the actual processing. Afterward, the list of metadatum is
     * joined into a single field.
     *
     * @param t the object we are trying to translate
     * @return a collection of metadata composed from the MetadataContributor
     */
    @Override
    public Collection<MetadatumDTO> contributeMetadata(T t) {
        LinkedList<MetadatumDTO> metadatums = (LinkedList<MetadatumDTO>) contributor.contributeMetadata(t);
        List<MetadatumDTO> combinedValue = new LinkedList<>();

        StringBuilder value = new StringBuilder();
        for (MetadatumDTO metadatum : metadatums) {
            value.append(metadatum.getValue());
            if (!metadatum.equals(metadatums.getLast())) {
                value.append(this.separator);
            }
        }
        combinedValue.add(metadataFieldMapping.toDCValue(field, value.toString()));
        return combinedValue;
    }

    /* Getters and Setters */

    public MetadataFieldConfig getField() {
        return field;
    }

    public void setField(MetadataFieldConfig field) {
        this.field = field;
    }

    public MetadataContributor getContributor() {
        return contributor;
    }

    public void setContributor(MetadataContributor contributor) {
        this.contributor = contributor;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
