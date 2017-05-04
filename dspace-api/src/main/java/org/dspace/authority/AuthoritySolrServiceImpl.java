/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.log4j.*;
import org.apache.solr.client.solrj.*;
import org.apache.solr.client.solrj.impl.*;
import org.apache.solr.client.solrj.response.*;
import org.apache.solr.common.*;
import org.dspace.authority.indexer.*;
import org.dspace.core.*;

/**
 * AuthoritySolrServiceImpl is responsible for requests to the authority solr core.
 * e.g. querying the authority solr core, indexing authorities in solr.
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class AuthoritySolrServiceImpl implements AuthorityIndexingService, AuthoritySearchService {

    private static final Logger log = Logger.getLogger(AuthoritySolrServiceImpl.class);

    protected AuthoritySolrServiceImpl()
    {

    }

    /**
     * Non-Static CommonsHttpSolrServer for processing indexing events.
     */
    protected HttpSolrServer solr = null;

    protected HttpSolrServer getSolr() throws MalformedURLException, SolrServerException {
        if (solr == null) {

            String solrService = ConfigurationManager.getProperty("solr.authority.server");

            log.debug("Solr authority URL: " + solrService);

            solr = new HttpSolrServer(solrService);
            solr.setBaseURL(solrService);

            SolrQuery solrQuery = new SolrQuery().setQuery("*:*");

            solr.query(solrQuery);
        }

        return solr;
    }

    /**
     * Index an authority value.
     * @param value
     */
    @Override
    public void indexContent(AuthorityValue value) {
        SolrInputDocument doc = value.getSolrInputDocument();

        try{
            writeDocument(doc);
            commit();
        }catch (Exception e){
            log.error("Error while writing authority value to the index: " + value.toString(), e);
        }
    }

    /**
     * Index an authority value with its solr ID.
     * @param value
     */
    @Override
    public void indexContentWithSolrId(AuthorityValue value) {
        SolrInputDocument doc = value.getSolrInputDocument();
        doc.setField("id", value.getSolrId());

        try{
            writeDocument(doc);
            commit();
        }catch (Exception e){
            log.error("Error while writing authority value to the index: " + value.toString(), e);
        }
    }

    /**
     * Empty the authority index.
     * @throws Exception
     */
    @Override
    public void cleanIndex() throws Exception {
        try{
            getSolr().deleteByQuery("*:*");
        } catch (Exception e){
            log.error("Error while cleaning authority solr server index", e);
            throw new Exception(e);
        }
    }

    /**
     * Save all changes since the last commit in the authority index.
     */
    @Override
    public void commit() {
        try {
            getSolr().commit();
        } catch (SolrServerException e) {
            log.error("Error while committing authority solr server", e);
        } catch (IOException e) {
            log.error("Error while committing authority solr server", e);
        }
    }

    @Override
    public boolean isConfiguredProperly() {
        boolean solrReturn = false;
        try {
            solrReturn = (getSolr()!=null);
        } catch (Exception e) {
            log.error("Authority solr is not correctly configured, check \"solr.authority.server\" property in the dspace.cfg", e);
        }
        return solrReturn;
    }

    /**
     * Delete an authority index by it's authority ID
     * @param id
     * the authority ID
     * @throws Exception
     */
    @Override
    public void deleteAuthorityValueById(String id) throws Exception {
        try{
            getSolr().deleteByQuery("id:\"" +id + "\"");
            commit();
        } catch (Exception e){
            log.error("Error while cleaning authority solr server index", e);
            throw new Exception(e);
        }
    }

    /**
     * Write the document to the solr index
     * @param doc the solr document
     * @throws IOException if IO error
     */
    protected void writeDocument(SolrInputDocument doc) throws IOException {

        try {
            getSolr().add(doc);
        } catch (Exception e) {
            try {
                log.error("An error occurred for document: " + doc.getField("id").getFirstValue() + ", source: " + doc.getField("source").getFirstValue() + ", authority_category: " + doc.getField("authority_category").getFirstValue() + ", full-text: " + doc.getField("full-text").getFirstValue(), e);
            } catch (Exception e1) {
                //shouldn't happen
            }
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public QueryResponse search(SolrQuery query) throws SolrServerException, MalformedURLException {
        return getSolr().query(query);
    }

    /**
     * Retrieves all the metadata fields which are indexed in the authority control
     * @return a list of metadata fields
     * @throws Exception if error
     */
    @Override
    public List<String> getAllIndexedCategories() throws Exception {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("*:*");
        solrQuery.setFacet(true);
        solrQuery.addFacetField("authority_category");

        QueryResponse response = getSolr().query(solrQuery);

        List<String> results = new ArrayList<String>();
        FacetField facetField = response.getFacetField("authority_category");
        if(facetField != null){
            List<FacetField.Count> values = facetField.getValues();
            if(values != null){
                for (FacetField.Count facetValue : values) {
                    if (facetValue != null && facetValue.getName() != null) {
                        results.add(facetValue.getName());
                    }
                }
            }
        }
        return results;
    }
}