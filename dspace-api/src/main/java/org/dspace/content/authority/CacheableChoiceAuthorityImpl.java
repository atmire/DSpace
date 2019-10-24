/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.dspace.content.Collection;
import org.dspace.content.authority.converter.AuthorityConverter;
import org.dspace.content.authority.service.CacheableAuthority;
import org.dspace.core.ConfigurationManager;
import org.dspace.external.model.ExternalDataObject;
import org.dspace.mock.MockMetadataValue;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.utils.DSpace;

/**
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class CacheableChoiceAuthorityImpl implements CacheableAuthority, ChoiceAuthority {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(CacheableChoiceAuthorityImpl.class);

    /**
     * This converter deals with the conversion from ExternalDataObjects and SolrDocs to AuthorityValue objects
     */
    private AuthorityConverter authorityConverter;
    /**
     * This map contains the metadata field to category mapping for the AuthorityValue objects
     */
    private Map<String, String> metadataToCategoryMap;


    /**
     * Non-Static CommonsHttpSolrServer for processing indexing events.
     */
    protected HttpSolrClient solr = null;

    /**
     * This method will check if the local solr variable is null. If it is, we'll retrieve the Solr server from the
     * solr.authority.server property and initiate this
     * @return A properly configured solr server
     * @throws SolrServerException  If something goes wrong
     * @throws IOException          If something goes wrong
     */
    public HttpSolrClient getSolr()
        throws SolrServerException, IOException {
        if (solr == null) {

            String solrService = ConfigurationManager.getProperty("solr.authority.server");

            log.debug("Solr authority URL: " + solrService);

            solr = new HttpSolrClient.Builder(solrService).build();
            solr.setBaseURL(solrService);

            SolrQuery solrQuery = new SolrQuery().setQuery("*:*");

            solr.query(solrQuery);
        }

        return solr;
    }

    public Choices getMatches(String field, String text, Collection collection, int start, int limit, String locale,
                              boolean bestMatch) {
        if (limit == 0) {
            limit = 10;
        }

        SolrQuery queryArgs = new SolrQuery();
        String query = constructQuery(text, bestMatch);
        queryArgs.setQuery(query);

        queryArgs.addFilterQuery("category:" + getMetadataToCategoryMap().get(field));
        queryArgs.set(CommonParams.START, start);
        //We add one to our facet limit so that we know if there are more matches
        int maxNumberOfSolrResults = limit + 1;
        queryArgs.set(CommonParams.ROWS, maxNumberOfSolrResults);
        try {
            QueryResponse searchResponse = getSolr().query(queryArgs);
            return resolveResults(searchResponse, field, text, start, limit);
        } catch (SolrServerException | IOException e) {
            log.error(e.getMessage(), e);
        }
        return new Choices(false);
    }

    private Choices resolveResults(QueryResponse searchResponse, String field,
                                   String text, int start, int limit) {
        Choices result;
        try {
            int max = 0;
            boolean hasMore = false;
            SolrDocumentList authDocs = searchResponse.getResults();
            ArrayList<Choice> choices = new ArrayList<>();
            if (authDocs != null) {
                max = (int) searchResponse.getResults().getNumFound();
                int maxDocs = Math.min(limit, max);
                for (int i = 0; i < maxDocs; i++) {
                    SolrDocument solrDocument = authDocs.get(i);
                    if (solrDocument != null) {
                        AuthorityValue val = getAuthorityConverter().createFromSolr(solrDocument);
                        Choice choice = new Choice(val.getId(), val.getValue(), val.getValue());
                        choice.setMetadata(val.getMetadata());
                        choices.add(choice);
                    }
                }


                hasMore = (authDocs.size() == (limit + 1));
            }

            result = new Choices(choices.toArray(new Choice[choices.size()]), start,
                                 hasMore ? max : choices.size() + start, Choices.CF_ACCEPTED, hasMore);
        } catch (Exception e) {
            log.error("Error while retrieving authority values {field: " + field + ", prefix:" + text + "}", e);
            result = new Choices(true);
        }

        return result;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private String constructQuery(String text, boolean bestMatch) {
        String query = null;
        if (text == null || text.trim().equals("")) {
            query = "*:*";
        } else {
            if (bestMatch) {
                query = "value_keyword:\"" + ClientUtils.escapeQueryChars(text) + "\"";
            } else {
                if (StringUtils.containsWhitespace(text)) {
                    String[] splittedString = StringUtils.split(text, " ");
                    query = "";
                    for (int i = 0; i < splittedString.length - 1; i++) {
                        query += "value:" + splittedString[i];
                    }
                    String lastString = splittedString[splittedString.length - 1];
                    if (CharUtils.compare(lastString.charAt(lastString.length() - 1), ' ') == 0) {
                        query += "value:" + lastString;
                    } else {
                        query += "value:" + lastString + "*";
                    }
                } else {
                    query = "value:{" + text + "}*";
                }
            }
        }
        return query;
    }

    @Override
    public Choices getMatches(String field, String text, Collection collection, int start, int limit, String locale) {
        return getMatches(field, text, collection, start, limit, locale, false);
    }

    @Override
    public Choices getBestMatch(String field, String text, Collection collection, String locale) {
        Choices matches = getMatches(field, text, collection, 0, 1, locale, false);
        if (matches.values.length != 0 && !matches.values[0].value.equalsIgnoreCase(text)) {
            matches = new Choices(false);
        }
        return matches;
    }

    @Override
    public String getLabel(String field, String key, String locale) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("requesting label for key " + key + " using locale " + locale);
            }
            SolrQuery queryArgs = new SolrQuery();
            queryArgs.setQuery("id:" + key);
            queryArgs.setRows(1);
            QueryResponse searchResponse = solr.query(queryArgs);
            SolrDocumentList docs = searchResponse.getResults();
            if (!docs.isEmpty()) {
                String label = (String) docs.get(0).getFieldValue("value");
                return label;
            }
        } catch (Exception e) {
            log.error("error occurred while trying to get label for key " + key, e);
        }

        return key;
    }

    @Override
    public void cacheAuthorityValue(String metadataField, ExternalDataObject externalDataObject)
        throws IOException, SolrServerException {

        AuthorityValue authorityValue = getAuthorityConverter()
            .createFromExternalData(getMetadataToCategoryMap().get(metadataField), externalDataObject);
        cacheAuthorityValue(authorityValue);
    }

    @Override
    public void cacheAuthorityValue(AuthorityValue authorityValue)
        throws IOException, SolrServerException {
        SolrInputDocument solrInputDocument = parseAuthorityValueToInputDoc(authorityValue);
        getSolr().add(solrInputDocument);
    }

    private SolrInputDocument parseAuthorityValueToInputDoc(AuthorityValue authorityValue) {
        SolrInputDocument solrInputDocument = new SolrInputDocument();
        solrInputDocument.addField("id", authorityValue.getId());
        solrInputDocument.addField("category", authorityValue.getCategory());
        solrInputDocument.addField("value", authorityValue.getValue());
        solrInputDocument.addField("last_modified_date", authorityValue.getLastModified());
        solrInputDocument.addField("creation_date", authorityValue.getCreationDate());
        solrInputDocument.addField("source", authorityValue.getSource());
        solrInputDocument.addField("external_source_identifier", authorityValue.getExternalSourceIdentifier());
        for (MockMetadataValue mockMetadataValue : authorityValue.getMetadata()) {
            solrInputDocument.addField("metadata_" + mockMetadataValue.getFieldString('_'),
                                       mockMetadataValue.getValue());
        }
        return solrInputDocument;
    }

    private AuthorityConverter getAuthorityConverter() {
        if (authorityConverter == null) {
            authorityConverter = new DSpace().getServiceManager()
                                             .getServiceByName("authorityConverter", AuthorityConverter.class);
        }
        return authorityConverter;
    }

    private Map<String, String> getMetadataToCategoryMap() {
        if (metadataToCategoryMap == null) {
            metadataToCategoryMap = new HashMap<>();
            String prefix = "authority.category.";
            ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();
            List<String> propKeys = configurationService.getPropertyKeys(prefix);
            for (String key : propKeys) {
                String category = configurationService.getProperty(key);
                String field = StringUtils.replaceAll(StringUtils.substringAfter(key, prefix), "\\.", "_");
                metadataToCategoryMap.put(field, category);
            }
        }
        return metadataToCategoryMap;

    }
}
