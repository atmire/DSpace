package org.dspace.authority.util;

import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRowIterator;

import java.sql.SQLException;

/**
 * Created by: Antoine Snyers (antoine at atmire dot com)
 * Date: 06 Mar 2014
 */
public class ItemUtils {

    public static ItemIterator findByAuthority(Context context, String authority)
            throws SQLException {
        String query = "SELECT item.* FROM metadatavalue,item " +
                "WHERE item.item_id = metadatavalue.resource_id "
                + "and resource_type_id=2 ";
        TableRowIterator rows = null;
        if (Item.ANY.equals(authority)) {
            rows = DatabaseManager.queryTable(context, "item", query);
        } else {
            query += " AND metadatavalue.authority = ?";
            rows = DatabaseManager.queryTable(context, "item", query, authority);
        }
        return new ItemIterator(context, rows);
    }

}
