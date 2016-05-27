/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.crosswalk;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.content.NonUniqueMetadataException;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.MetadataFieldService;
import org.dspace.content.service.MetadataSchemaService;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CrosswalkMetadataValidator {

    /** log4j logger */
    private static Logger log = Logger.getLogger(CrosswalkMetadataValidator.class);

    protected MetadataSchemaService metadataSchemaService;
    protected MetadataFieldService metadataFieldService;

    private String schemaChoice;
    private String fieldChoice;

//    private Map<Triple<String, String, String>, MetadataField> validatedMetadataFields;

    private Map<String, MetadataSchema> cachedMetadataSchemas;
    private Map<String, MetadataField> cachedMetadataFields;

    public CrosswalkMetadataValidator() {
        metadataSchemaService = ContentServiceFactory.getInstance().getMetadataSchemaService();
        metadataFieldService = ContentServiceFactory.getInstance().getMetadataFieldService();

//        validatedMetadataFields = new HashMap<>();

        try
        {
            cachedMetadataSchemas = metadataSchemaService.exportMetadataSchemaList();
            cachedMetadataFields = metadataFieldService.exportMetadataFieldList();
        } catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }

        // The two options, with three possibilities each: add, ignore, fail
        schemaChoice = ConfigurationManager.getProperty("oai", "harvester.unknownSchema");
        if (schemaChoice == null)
        {
            schemaChoice = "fail";
        }

        fieldChoice = ConfigurationManager.getProperty("oai", "harvester.unknownField");
        if (fieldChoice == null)
        {
            fieldChoice = "fail";
        }
    }

    /**
     * Scans metadata for elements not defined in this DSpace instance. It then takes action based
     * on a configurable parameter (fail, ignore, add).
     */
    public MetadataField checkMetadata(Context context, String schema, String element, String qualifier, boolean forceCreate) throws SQLException, AuthorizeException, CrosswalkException {
        String metadataFieldString = schema + "_" + element;
        if(StringUtils.isNotBlank(qualifier)) {
            metadataFieldString += "_" + qualifier;
        }

        // Verify that the schema exists
        MetadataSchema mdSchema = cachedMetadataSchemas.get(schema);
        MetadataField mdField = null;

        if (!cachedMetadataSchemas.containsKey(schema)) {
            // add a new schema, giving it a namespace of "unknown". Possibly a very bad idea.
            if (forceCreate && schemaChoice.equals("add")) {
                try {
                    mdSchema = metadataSchemaService.create(context, schema, String.valueOf(new Date().getTime()));
                    mdSchema.setNamespace("unknown" + mdSchema.getID());
                    metadataSchemaService.update(context, mdSchema);
                    cachedMetadataSchemas.put(schema, mdSchema);
                } catch (NonUniqueMetadataException e) {
                    // This case should not be possible
                    log.error(e.getMessage(), e);
                }
            }
            // ignore the offending schema, quietly dropping all of its metadata elements before they clog our gears
            else if (!schemaChoice.equals("ignore")) {
                throw new CrosswalkException("The '" + schema + "' schema has not been defined in this DSpace instance. ");
            }
        }

        if (cachedMetadataSchemas.containsKey(schema)) {
            // Verify that the element exists; this part is reachable only if the metadata schema is valid
            if (!cachedMetadataFields.containsKey(metadataFieldString)) {
                if (forceCreate && fieldChoice.equals("add")) {
                    try {
                        mdField = metadataFieldService.create(context, mdSchema, element, qualifier, null);
                        cachedMetadataFields.put(metadataFieldString, mdField);
                    } catch (NonUniqueMetadataException e) {
                        // This case should also not be possible
                        e.printStackTrace();
                    }
                } else if (!fieldChoice.equals("ignore")) {
                    throw new CrosswalkException("The '" + element + "." + qualifier + "' element has not been defined in this DSpace instance. ");
                }
            }
        }

        return cachedMetadataFields.get(metadataFieldString);
    }

//    private boolean validatedBefore(String schema, String element, String qualifier) {
//        return validatedMetadataFields.containsKey(createKey(schema, element, qualifier));
//    }

//    private ImmutableTriple<String, String, String> createKey(final String schema, final String element, final String qualifier) {
//        return new ImmutableTriple<String, String, String>(schema, element, qualifier);
//    }
}
