package org.dspace.authority;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.dspace.authority.service.ItemService;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.content.MetadataField;
import org.dspace.content.MetadataSchema;
import org.dspace.core.Context;
import org.dspace.storage.rdbms.DatabaseManager;
import org.dspace.storage.rdbms.TableRowIterator;

import java.sql.SQLException;

/**
 * @author philip at atmire.com
 */
public class ItemServiceImpl implements ItemService {
    @Override
    public ItemIterator findByMetadataFieldAuthority(Context context, String mdString, String authority, boolean archivedOnly) throws SQLException {

        MetadataField mdf = getMetadataField(context, mdString);
        String query = "SELECT item.* "  + getByMetadataFieldAuthorityQuery(authority,archivedOnly);

        TableRowIterator rows = null;
        if (Item.ANY.equals(authority)) {
            rows = DatabaseManager.queryTable(context, "item", query, mdf.getFieldID());
        } else {
            rows = DatabaseManager.queryTable(context, "item", query, mdf.getFieldID(), authority);
        }
        return new ItemIterator(context, rows);
    }

    @Override
    public int countByMetadataFieldAuthority(Context context, String mdString, String authority, boolean archivedOnly) throws SQLException {

        MetadataField mdf = getMetadataField(context, mdString);
        String query = "SELECT count(item.*) as COUNT "  + getByMetadataFieldAuthorityQuery(authority,archivedOnly);

        TableRowIterator rows = null;
        if (Item.ANY.equals(authority)) {
            rows = DatabaseManager.queryTable(context, "item", query, mdf.getFieldID()) ;
        } else {
            rows = DatabaseManager.queryTable(context, "item", query, mdf.getFieldID(), authority);
        }
        if (rows.hasNext()) {
            return rows.next().getIntColumn("COUNT");
        }
        return 0;
    }

    private String getByMetadataFieldAuthorityQuery(String authority, boolean archivedOnly) {


        String query = "FROM metadatavalue,item WHERE item.item_id = metadatavalue.resource_id AND metadata_field_id = ?";

        if(archivedOnly) {
            query = "FROM metadatavalue,item WHERE item.in_archive=TRUE " +
                    "AND item.item_id = metadatavalue.resource_id AND metadata_field_id = ?";
        }
        if (!Item.ANY.equals(authority)) {
            query += " AND metadatavalue.authority = ?";

        }
        return query;
    }


    private MetadataField getMetadataField(Context context, String mdString) throws SQLException {
        String[] elements = getElementsFilled(mdString);
        String schema = elements[0], element = elements[1], qualifier = elements[2];
        MetadataSchema mds = MetadataSchema.find(context, schema);
        if (mds == null) {
            throw new IllegalArgumentException("No such metadata schema: " + schema);
        }
        MetadataField mdf = MetadataField.findByElement(context, mds.getSchemaID(), element, qualifier);
        if (mdf == null) {
            throw new IllegalArgumentException(
                    "No such metadata field: schema=" + schema + ", element=" + element + ", qualifier=" + qualifier);
        }
        return mdf;
    }

    @Override
    public ItemIterator findAllIncludeNotArchived(Context context) throws SQLException {
        String myQuery = "SELECT * FROM item";

        TableRowIterator rows = DatabaseManager.queryTable(context, "item", myQuery);

        return new ItemIterator(context, rows);
    }

    public static String[] getElementsFilled(String fieldName) {
        String[] elements = getElements(fieldName);
        for (int i = 0; i < elements.length; i++) {
            if (StringUtils.isBlank(elements[i])) {
                elements[i] = null;
            }
        }
        return elements;
    }

    public static String[] getElements(String fieldName) {
        String[] tokens = StringUtils.split(fieldName, ".");

        int add = 4 - tokens.length;
        if (add > 0) {
            tokens = (String[]) ArrayUtils.addAll(tokens, new String[add]);
        }

        return tokens;
    }
}
