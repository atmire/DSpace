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

import org.dspace.content.Collection;
import org.dspace.core.Context;
import org.dspace.discovery.indexobject.IndexableCollection;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public interface IndexableCollectionService extends IndexableDSpaceObjectService<IndexableCollection> {

    public List<String> getCollectionLocations(Context context, Collection collection) throws SQLException;
}
