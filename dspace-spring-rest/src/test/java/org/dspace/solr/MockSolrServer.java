/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.solr;

import java.io.File;
import java.io.IOException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.dspace.app.rest.test.AbstractDSpaceTest;

/**
 * Abstract class to mock a service that uses SOLR
 */
public class MockSolrServer {

    private String coreName;

    private SolrServer solrServer = null;

    private static CoreContainer container = null;

    public MockSolrServer(final String coreName) throws Exception {
        this.coreName = coreName;
        initSolrServer();
    }

    public SolrServer getSolrServer() {
        return solrServer;
    }

    protected void initSolrServer() throws Exception {
        initSolrContainer();

        solrServer = new EmbeddedSolrServer(container, coreName);

        //Start with an empty index
        try {
            solrServer.deleteByQuery("*:*");
            solrServer.commit();
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void initSolrContainer() {
        if(container == null) {
            container = new CoreContainer(AbstractDSpaceTest.TEST_DSPACE_DIR + File.separator + "solr");
            container.load();
        }
    }

    public void destroy() throws Exception {
        if(solrServer != null) {
            solrServer.shutdown();
        }
    }

}
