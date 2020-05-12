/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.external.provider.metadata.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.RecordSet;
import gr.ekt.bte.core.Value;
import gr.ekt.bte.dataloader.FileDataLoader;
import gr.ekt.bte.exceptions.MalformedSourceException;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.dspace.app.itemimport.BTEBatchImportService;
import org.dspace.content.Bitstream;
import org.dspace.content.dto.MetadataValueDTO;
import org.dspace.core.Context;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.external.provider.impl.BTEDataProvider;
import org.dspace.external.provider.metadata.MetadataSuggestionProvider;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.storage.bitstore.factory.StorageServiceFactory;
import org.dspace.storage.bitstore.service.BitstreamStorageService;
import org.dspace.utils.StreamUtils;

/**
 * A class that extracts data from a file with a fitting DataLoader and puts it in an ExternalDataObject according to
 * configured mappings
 *
 * @author Maria Verdonck (Atmire) on 12/05/2020
 */
public class BTEMetadataSuggestionProvider extends MetadataSuggestionProvider<BTEDataProvider> {

    /**
     * log4j category
     */
    private static Logger log = org.apache.logging.log4j.LogManager.getLogger(BTEMetadataSuggestionProvider.class);

    private String fieldForTitle;
    protected BitstreamStorageService bitstreamStorageService = StorageServiceFactory.getInstance()
                                                                                     .getBitstreamStorageService();
    protected BTEBatchImportService bteBatchImportService = DSpaceServicesFactory.getInstance().getServiceManager()
                                                                                 .getServiceByName(
                                                                                     "org.dspace.app.itemimport" +
                                                                                     ".BTEBatchImportService",
                                                                                     BTEBatchImportService.class);

    @Override
    public List<ExternalDataObject> bitstreamQuery(Bitstream bitstream) {
        try (Context context = new Context()) {
            List<FileDataLoader> fileDataLoaders =
                DSpaceServicesFactory.getInstance().getServiceManager().getServicesByType(FileDataLoader.class);
            context.turnOffAuthorisationSystem();
            InputStream inputStream = bitstreamStorageService.retrieve(context, bitstream);
            File fileTemp = StreamUtils.stream2file(inputStream);
            for (FileDataLoader fileDataLoader : fileDataLoaders) {
                fileDataLoader.setFilename(fileTemp.getAbsolutePath());
                RecordSet records = null;
                try {
                    records = fileDataLoader.getRecords();
                } catch (MalformedSourceException e) {
                    continue;
                }
                List<ExternalDataObject> result = this.constructExternalDataObjectListFromRecordSet(records);
                inputStream.close();
                return result;
            }
        } catch (SQLException | IOException e) {
            log.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    /**
     * Constructs the external data object list based on a record set.
     * Each external data object's metadata gets populated from a record in the set (see @link this
     * .fillInExternalDataObjectMdWithRecord()} and the display value is extracted from the record with
     * {@link this.fieldForTile} as fieldName in the record.
     *
     * @param records Set of records to be converted to list of external data objects
     * @return List of external data objects, populate with data from the given set of records
     */
    private List<ExternalDataObject> constructExternalDataObjectListFromRecordSet(RecordSet records) {
        List<ExternalDataObject> externalDataObjects = new ArrayList<>();
        for (Record rec : records) {
            ExternalDataObject externalDataObject = new ExternalDataObject();
            if (super.getExternalDataProvider() != null &&
                StringUtils.isNotBlank(super.getExternalDataProvider().getSourceIdentifier())) {
                externalDataObject.setSource(super.getExternalDataProvider().getSourceIdentifier());
            }
            List<Value> titles = this.findTitlesInRecord(rec);
            if (titles != null && !titles.isEmpty()) {
                externalDataObject.setDisplayValue(titles.get(0).getAsString());
                externalDataObject.setValue(titles.get(0).getAsString());
            }
            externalDataObjects.add(this.fillInExternalDataObjectMdWithRecord(rec, externalDataObject));
        }
        return externalDataObjects;
    }

    /**
     * Populate the given externalDataObject metadata with the values in the given record according to the record
     * field name to metadata field mapping found in {@link this.bteBatchImportService.outputMap} (found in bte.xml)
     *
     * @param rec                Record with fieldName-value pairs
     * @param externalDataObject External data object whose metadata will be populated
     * @return The populate external data object
     */
    private ExternalDataObject fillInExternalDataObjectMdWithRecord(Record rec, ExternalDataObject externalDataObject) {
        Map<String, String> mdFieldToFieldNameMapping = bteBatchImportService.getOutputMap();
        for (Map.Entry<String, String> fieldNameMdFieldEntry : mdFieldToFieldNameMapping.entrySet()) {
            if (rec.hasField(fieldNameMdFieldEntry.getValue())) {
                List<Value> values = rec.getValues(fieldNameMdFieldEntry.getValue());
                if (!values.isEmpty()) {
                    String mdField = fieldNameMdFieldEntry.getKey();
                    for (Value value : values) {
                        externalDataObject
                            .addMetadata(new MetadataValueDTO(mdField, null, value.getAsString()));
                    }
                }
            }
        }
        return externalDataObject;
    }

    /**
     * Finds the values for field key {@link this.fieldForTile} in the given record, to use as display value for the
     * ExternalDataObject
     *
     * @param rec Record containing data in fieldName-value pairs
     * @return List of values with {@link this.fieldForTile} as a fieldName from the given record
     */
    private List<Value> findTitlesInRecord(Record rec) {
        if (rec.hasField(this.fieldForTitle)) {
            return rec.getValues(this.fieldForTitle);
        } else {
            log.error("Recordset ( {} ) does not contain value for fallbackField {}", rec, this.fieldForTitle);
        }
        return new ArrayList<>();
    }

    /**
     * Generic getter for the fieldForTitle
     *
     * @return the fieldForTitle value of this BTEMetadataSuggestionProvider
     */
    public String getFieldForTitle() {
        return fieldForTitle;
    }

    /**
     * Generic setter for the fieldForTitle
     *
     * @param fieldForTitle The fieldForTitle to be set on this BTEMetadataSuggestionProvider
     */
    public void setFieldForTitle(String fieldForTitle) {
        this.fieldForTitle = fieldForTitle;
    }
}
