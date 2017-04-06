package org.dspace.authority.service;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.ItemIterator;
import org.dspace.core.Context;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author philip at atmire.com
 */
public interface ItemService {
    ItemIterator findByMetadataFieldAuthority(Context context, String field, String id, boolean b) throws SQLException, AuthorizeException, IOException;

    int countByMetadataFieldAuthority(Context context, String mdString, String authority, boolean archivedOnly) throws SQLException;

    ItemIterator findAllIncludeNotArchived(Context context) throws SQLException;

}
