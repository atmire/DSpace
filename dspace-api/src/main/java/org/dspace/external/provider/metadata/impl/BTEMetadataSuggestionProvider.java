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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gr.ekt.bte.core.DataLoader;
import gr.ekt.bte.core.Record;
import gr.ekt.bte.core.RecordSet;
import gr.ekt.bte.core.Value;
import gr.ekt.bte.dataloader.FileDataLoader;
import gr.ekt.bte.exceptions.MalformedSourceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.logging.log4j.Logger;
import org.dspace.app.itemimport.BTEBatchImportService;
import org.dspace.content.Bitstream;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.dto.MetadataValueDTO;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.external.provider.impl.BTEDataProvider;
import org.dspace.external.provider.metadata.MetadataSuggestionProvider;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.storage.bitstore.factory.StorageServiceFactory;
import org.dspace.storage.bitstore.service.BitstreamStorageService;
import org.dspace.submit.listener.MetadataListener;
import org.dspace.submit.lookup.SubmissionLookupDataLoader;
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

    /**
     * Field name in a {@link Record} from a {@link DataLoader} where the title is in
     */
    private String fieldForTitle;

    // Services
    protected BitstreamStorageService bitstreamStorageService
        = StorageServiceFactory.getInstance().getBitstreamStorageService();

    protected BTEBatchImportService bteBatchImportService
        = DSpaceServicesFactory.getInstance().getServiceManager()
                               .getServiceByName("org.dspace.app.itemimport.BTEBatchImportService",
                                   BTEBatchImportService.class);

    protected List<MetadataListener> listeners = DSpaceServicesFactory.getInstance().getServiceManager()
                                                                      .getServicesByType(MetadataListener.class);

    protected final ItemService itemService = ContentServiceFactory.getInstance().getItemService();

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

    @Override
    public List<ExternalDataObject> metadataQuery(Item item, int start, int limit) {
        List<ExternalDataObject> result = new ArrayList<>();
        for (MetadataListener listener : listeners) {
            for (DataLoader dataLoader : listener.getDataloadersMap().values()) {
                SubmissionLookupDataLoader submissionLookupDataLoader = (SubmissionLookupDataLoader) dataLoader;
                Map<String, Set<String>> identifierKeys = this.getIdentifierKeysMap(item, listener);
                if (!identifierKeys.isEmpty()) {
                    try (Context context = new Context()) {
                        context.turnOffAuthorisationSystem();
                        List<Record> records = submissionLookupDataLoader.getByIdentifier(context, identifierKeys);
                        context.restoreAuthSystemState();
                        if (records != null) {
                            result.addAll(this.constructExternalDataObjectListFromRecordList(records));
                        }
                    } catch (HttpException | IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Creates the mapping between a key name (ex. doi) and a set of values corresponding with that key from the
     * item's metadata value (all it's values in dc.identifier.doi)
     *
     * @param item     Item with metadata containing identifiers
     * @param listener Config bean with the mapping between key name and the corresponding mdField string
     * @return Mapping between a key name and the corresponding set of values of dat key in the given item's metadata
     */
    private Map<String, Set<String>> getIdentifierKeysMap(Item item, MetadataListener listener) {
        Map<String, Set<String>> identifierKeys = new HashMap<>();
        for (Map.Entry<String, String> mdFieldToIdentifierFieldName : listener.getMetadata().entrySet()) {
            List<MetadataValue> metadataValues =
                itemService.getMetadataByMetadataString(item, mdFieldToIdentifierFieldName.getKey());

            if (!metadataValues.isEmpty()) {
                Set<String> keySet = new HashSet<>();
                for (MetadataValue metadataValue : metadataValues) {
                    keySet.add(metadataValue.getValue());
                }
                identifierKeys.put(mdFieldToIdentifierFieldName.getValue(), keySet);
            }
        }
        return identifierKeys;
    }

    /**
     * Constructs the external data object list based on a record list.
     * Each external data object's metadata gets populated from a record in the list (see @link this
     * .fillInExternalDataObjectMdWithRecord()} and the display value is extracted from the record with
     * {@link this.fieldForTile} as fieldName in the record.
     *
     * @param records List of records to be converted to list of external data objects
     * @return List of external data objects, populate with data from the given list of records
     */
    private List<ExternalDataObject> constructExternalDataObjectListFromRecordList(List<Record> records) {
        List<ExternalDataObject> externalDataObjects = new ArrayList<>();
        for (Record rec : records) {
            externalDataObjects.add(constructExternalDataObjectFromRecord(rec));
        }
        return externalDataObjects;
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
            externalDataObjects.add(constructExternalDataObjectFromRecord(rec));
        }
        return externalDataObjects;
    }

    /**
     * Constructs a single external data object from a single record.
     * The external data object's metadata gets populated from a record (see @link this
     * .fillInExternalDataObjectMdWithRecord()} and the display value is extracted from the record with
     * {@link this.fieldForTile} as fieldName in the record.
     *
     * @param record Record to be converted in an external data object
     * @return External data object corresponding to the given record
     */
    private ExternalDataObject constructExternalDataObjectFromRecord(Record record) {
        ExternalDataObject externalDataObject = new ExternalDataObject();
        if (super.getExternalDataProvider() != null &&
            StringUtils.isNotBlank(super.getExternalDataProvider().getSourceIdentifier())) {
            externalDataObject.setSource(super.getExternalDataProvider().getSourceIdentifier());
        }
        List<Value> titles = this.findTitlesInRecord(record);
        if (titles != null && !titles.isEmpty()) {
            externalDataObject.setDisplayValue(titles.get(0).getAsString());
            externalDataObject.setValue(titles.get(0).getAsString());
        }
        return this.fillInExternalDataObjectMdWithRecord(record, externalDataObject);
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
