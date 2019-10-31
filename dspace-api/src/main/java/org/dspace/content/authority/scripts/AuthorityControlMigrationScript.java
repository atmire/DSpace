/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority.scripts;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.authority.AuthorityValue;
import org.dspace.content.authority.CacheableChoiceAuthorityImpl;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataValueService;
import org.dspace.core.Context;
import org.dspace.mock.MockMetadataValue;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.utils.DSpace;

public class AuthorityControlMigrationScript {

    private final static Logger log = LogManager.getLogger();

    /**
     * Non-Static CommonsHttpSolrServer for processing indexing events.
     */
    protected HttpSolrClient solr = null;

    private ItemService itemService;
    private MetadataValueService metadataValueService;
    private ConfigurationService configurationService;
    private CacheableChoiceAuthorityImpl cacheableChoiceAuthority;

    private AuthorityControlMigrationScript() throws IOException, SolrServerException {
        itemService = ContentServiceFactory.getInstance().getItemService();
        metadataValueService = ContentServiceFactory.getInstance().getMetadataValueService();
        configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();
        cacheableChoiceAuthority = new DSpace().getServiceManager()
                               .getServiceByName("cacheableChoiceAuthority", CacheableChoiceAuthorityImpl.class);
        solr = cacheableChoiceAuthority.getSolr();
    }

    public static void main(String[] argv) throws SQLException, IOException, SolrServerException, AuthorizeException {
        AuthorityControlMigrationScript authorityControlMigrationScript = new AuthorityControlMigrationScript();
        authorityControlMigrationScript.run();
    }

    private void run() throws SQLException, IOException, SolrServerException, AuthorizeException {
        boolean hasMore = true;
        int maxNbOfResults = 5;
        int start = 0;
        List<SolrDocument> solrDocumentsToDelete = new LinkedList<>();
        List<SolrDocument> solrDocumentsToUpdate = new LinkedList<>();

        SolrQuery solrQuery = constructBaseSolrQuery(maxNbOfResults);
        while (hasMore) {
            solrQuery.setStart(start);

            QueryResponse response = solr.query(solrQuery);
            for (SolrDocument solrDocument : response.getResults()) {
                if (StringUtils.equalsIgnoreCase(String.valueOf(solrDocument.get("authority_type")), "person")) {
                    solrDocumentsToDelete.add(solrDocument);
                } else {
                    solrDocumentsToUpdate.add(solrDocument);
                }
            }
            processActionLists(solrDocumentsToDelete, solrDocumentsToUpdate);

            hasMore = response.getResults().getNumFound() > (start + maxNbOfResults);
            start += maxNbOfResults;

            solrDocumentsToDelete.clear();
            solrDocumentsToUpdate.clear();
        }
    }

    private void processActionLists(List<SolrDocument> solrDocumentsToDelete,
                                    List<SolrDocument> solrDocumentsToUpdate)
        throws SQLException, AuthorizeException, IOException, SolrServerException {
        for (SolrDocument solrDocument : solrDocumentsToDelete) {
            processDelete(solrDocument);
        }
        for (SolrDocument solrDocument : solrDocumentsToUpdate) {
            processUpdate(solrDocument);
        }
    }

    private void processDelete(SolrDocument solrDocument) throws IOException, SolrServerException {
        String authorityFromDocument = String.valueOf(solrDocument.get("id"));
        solr.deleteById(authorityFromDocument);

    }

    private SolrQuery constructBaseSolrQuery(int maxNbOfResults) {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("field:*");
        solrQuery.setRows(maxNbOfResults);
        return solrQuery;
    }

    private void processUpdate(SolrDocument solrDocument)
        throws SQLException, AuthorizeException, IOException, SolrServerException {

        String orcidId = String.valueOf(solrDocument.get("orcid_id"));
        String givenName = String.valueOf(solrDocument.get("first_name"));
        String familyName = String.valueOf(solrDocument.get("last_name"));
        String value = String.valueOf(solrDocument.get("value"));
        String authorityFromDocument = String.valueOf(solrDocument.get("id"));
        String field = String.valueOf(solrDocument.get("field"));
        String mdString = StringUtils.replace(field, "_", ".");
        Date creationDate = (Date) solrDocument.get("creation_date");
        String category = configurationService.getProperty("authority.category." + mdString);
        if (StringUtils.isBlank(category)) {
            log.error("Authority configuration for: " + mdString + " is not correct. Exitting script");
            System.exit(1);
        }

        AuthorityValue authorityValue = constructAuthorityValue(orcidId, givenName, familyName, value, creationDate,
                                                                category);

        Context context = new Context();
        context.turnOffAuthorisationSystem();
        handleItems(authorityFromDocument, mdString, authorityValue, context);

        processDelete(solrDocument);
        cacheableChoiceAuthority.cacheAuthorityValue(authorityValue);

        context.complete();

    }

    private void handleItems(String authorityFromDocument, String mdString, AuthorityValue authorityValue,
                             Context context) throws SQLException, AuthorizeException {
        Iterator<Item> itemIterator = itemService
            .findByMetadataFieldAuthority(context, mdString, authorityFromDocument);

        while (itemIterator.hasNext()) {
            Item item = itemIterator.next();
            for (MetadataValue metadataValue : item.getMetadata()) {
                if (StringUtils.equals(metadataValue.getAuthority(), authorityFromDocument)) {
                    metadataValue.setAuthority(authorityValue.getId());
                    metadataValueService.update(context, metadataValue);
                }
            }
            itemService.updateLastModified(context, item);

        }
    }

    private AuthorityValue constructAuthorityValue(String orcidId, String givenName, String familyName, String value,
                                                   Date creationDate, String category) {
        AuthorityValue authorityValue = new AuthorityValue();
        authorityValue.setMetadata(constructMockMetadataList(orcidId, givenName, familyName));
        authorityValue.setExternalSourceIdentifier(orcidId);
        authorityValue.setSource("orcidv2");
        authorityValue.setValue(value);
        authorityValue.setLastModified(new Date());
        authorityValue.setCreationDate(creationDate);
        authorityValue.setCategory(category);
        return authorityValue;
    }

    private List<MockMetadataValue> constructMockMetadataList(String orcidId, String givenName, String familyName) {
        List<MockMetadataValue> metadataValues = new LinkedList<>();
        metadataValues.add(new MockMetadataValue("dc", "identifier", "orcid", null, orcidId));
        metadataValues.add(new MockMetadataValue("dc", "identifier", "uri", null, "https://orcid.org/" + orcidId));
        metadataValues.add(new MockMetadataValue("person", "givenName", null, null, givenName));
        metadataValues.add(new MockMetadataValue("person", "familyName", null, null, familyName));
        return metadataValues;
    }
}
