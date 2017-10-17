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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.dspace.app.rest.test.AbstractDSpaceIntegrationTest;

/**
 * Abstract class to mock a service that uses SOLR
 */
public class MockSolrServer {

    private String coreName;

    private SolrServer solrServer = null;

    private static CoreContainer container = null;
    private static final ConcurrentMap<String, SolrServer> loadedCores = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, AtomicLong> usersPerCore = new ConcurrentHashMap<>();


    public MockSolrServer(final String coreName) throws Exception {
        this.coreName = coreName;
        initSolrServer();
    }

    public SolrServer getSolrServer() {
        return solrServer;
    }

    protected void initSolrServer() throws Exception {
        solrServer = loadedCores.get(coreName);
        if(solrServer == null) {
            solrServer = initSolrServerForCore(coreName);
        }

        usersPerCore.putIfAbsent(coreName, new AtomicLong(0));
        usersPerCore.get(coreName).incrementAndGet();
    }

    private static synchronized SolrServer initSolrServerForCore(final String coreName) {
        SolrServer server = loadedCores.get(coreName);
        if(server == null) {
            initSolrContainer();

            server = new EmbeddedSolrServer(container, coreName);

            //Start with an empty index
            try {
                server.deleteByQuery("*:*");
                server.commit();
            } catch (SolrServerException | IOException e) {
                e.printStackTrace();
            }

            loadedCores.put(coreName, server);
        }
        return server;
    }

    public void destroy() throws Exception {
        if(solrServer != null) {
            long remainingUsers = usersPerCore.get(coreName).decrementAndGet();
            if(remainingUsers <= 0) {
                solrServer.shutdown();
                usersPerCore.remove(coreName);
            }

            if(usersPerCore.isEmpty()) {
                destroyContainer();
            }
        }
    }

    private static synchronized void initSolrContainer() {
        if(container == null) {
            container = new CoreContainer(AbstractDSpaceIntegrationTest.TEST_DSPACE_DIR + File.separator + "solr");
            container.load();
        }
    }

    private static synchronized void destroyContainer() {
        container = null;
    }

}
