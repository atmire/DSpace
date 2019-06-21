/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery.indexobject.service;

import java.sql.SQLException;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.dspace.core.Context;
import org.dspace.discovery.indexobject.IndexableDSpaceObject;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public interface IndexableDSpaceObjectService<T extends IndexableDSpaceObject> extends IndexableObjectService<T> {

    List<String> getLocations(Context context, T indexableDSpaceObject) throws SQLException;

    void storeCommunityCollectionLocations(SolrInputDocument doc, List<String> locations);
}
