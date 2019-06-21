/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery.indexobject.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.core.Context;
import org.dspace.discovery.IndexableObject;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public interface IndexableObjectService<T extends IndexableObject> {

    Iterator<T> findAll(Context context) throws SQLException;

    String getType();

    /**
     * Create Lucene document with all the shared fields initialized.
     *
     * T the indexableObject that we want to index
     *
     * @return initialized Lucene document
     */
    SolrInputDocument buildDocument(Context context, T indexableObject) throws SQLException, IOException;

    void writeDocument(Context context, T indexableObject, SolrInputDocument solrInputDocument)
            throws SQLException, IOException, SolrServerException;

    void delete(IndexableObject indexableObject) throws IOException, SolrServerException;

    void delete(String indexableObjectIdentifier) throws IOException, SolrServerException;

    void deleteAll() throws IOException, SolrServerException;

    IndexableObject findIndexableObject(Context context, String id) throws SQLException;
}
