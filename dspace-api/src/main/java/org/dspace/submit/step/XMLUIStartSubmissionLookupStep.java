/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.submit.step;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.core.Context;
import org.dspace.importer.external.MetadataSourceException;
import org.dspace.importer.external.datamodel.ImportRecord;
import org.dspace.importer.external.metadatamapping.MetadatumDTO;
import org.dspace.importer.external.service.ImportService;
import org.dspace.submit.AbstractProcessingStep;
import org.dspace.utils.DSpace;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;

/**
 * Created by jonas - jonas@atmire.com on 06/11/15.
 */
public class XMLUIStartSubmissionLookupStep extends AbstractProcessingStep {

    private static String publicationUrl = null;
    private static Logger log = Logger.getLogger(XMLUIStartSubmissionLookupStep.class);
    private ItemService itemService = ContentServiceFactory.getInstance().getItemService();

    public static final String NEXT_NO_IMPORT_BUTTON = "submit_next_no_import";

    @Override
    public int doProcessing(Context context, HttpServletRequest request, HttpServletResponse response, SubmissionInfo subInfo) throws ServletException, IOException, AuthorizeException, SQLException
    {
        if(request.getParameter(AbstractProcessingStep.NEXT_BUTTON) != null)
        {
            String publicationID = URLDecoder.decode(request.getParameter("publication_id"), "UTF-8");

            if (StringUtils.isNotBlank(publicationID))
            {
                ImportService importService = new DSpace().getServiceManager().getServiceByName("importService", ImportService.class);
                if(importService.ingest(publicationID)) {
                    Item item = subInfo.getSubmissionItem().getItem();
                    try {
                        ImportRecord record = importService.getRecord(getPublicationUrl(), publicationID);

                        //TODO for templates
                        for (MetadataValue metadatum : itemService.getMetadata(item, Item.ANY, Item.ANY, Item.ANY, Item.ANY)) {
                            itemService.clearMetadata(context, item, metadatum.getMetadataField().getMetadataSchema().getName(), metadatum.getMetadataField().getElement(), metadatum.getMetadataField().getQualifier(), metadatum.getLanguage());
                        }

                        for (MetadatumDTO metadatum : record.getValueList()) {
                            itemService.addMetadata(context, item, metadatum.getSchema(), metadatum.getElement(), metadatum.getQualifier(), null, metadatum.getValue());
                        }

                        itemService.update(context, item);

                    } catch (MetadataSourceException e) {
                        //TODO: Find a better way
                        log.error(e);
                    }
                }

            }
        }
            return STATUS_COMPLETE;
    }

    @Override
    public int getNumberOfPages(HttpServletRequest request, SubmissionInfo subInfo) throws ServletException {
        return 1;
    }

    public String getPublicationUrl(){
        if(publicationUrl==null){
            publicationUrl=configurationService.getProperty("publication-lookup.publication.url");
        }
        return publicationUrl;
    }
}
