/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.indexer;

import java.sql.*;
import java.util.*;
import org.apache.commons.collections.*;
import org.apache.log4j.*;
import org.dspace.authority.*;
import org.dspace.authority.service.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.content.service.*;
import org.dspace.core.*;
import org.dspace.services.*;
import org.springframework.beans.factory.*;
import org.springframework.beans.factory.annotation.*;

/**
 * DSpaceAuthorityIndexer is used in IndexClient, which is called by the AuthorityConsumer and the indexing-script.
 * <p>
 * An instance of DSpaceAuthorityIndexer is bound to a list of items.
 * This can be one item or all items too depending on the init() method.
 * <p>
 * DSpaceAuthorityIndexer lets you iterate over each metadata value
 * for each metadata field defined in dspace.cfg with 'authority.author.indexer.field'
 * for each item in the list.
 * <p>
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class DSpaceAuthorityIndexer implements AuthorityIndexerInterface, InitializingBean {

    private static final Logger log = Logger.getLogger(DSpaceAuthorityIndexer.class);

    protected Iterator<Item> itemIterator;
    protected Item currentItem;
    /**
     * The list of metadata fields which are to be indexed *
     */
    protected List<String> metadataFields;
    protected int currentFieldIndex;
    protected int currentMetadataIndex;
    protected AuthorityValue nextValue;
    protected Context context;
    @Autowired(required = true)
    protected AuthorityValueService authorityValueService;
    @Autowired(required = true)
    protected ItemService itemService;
    protected boolean useCache;
    protected Map<String, AuthorityValue> cache;
    

    @Autowired(required = true)
    protected ConfigurationService configurationService;

    @Override
    public void afterPropertiesSet() throws Exception {
        int counter = 1;
        String field;
        metadataFields = new ArrayList<>();
        while ((field = configurationService.getProperty("authority.author.indexer.field." + counter)) != null) {
            metadataFields.add(field);
            counter++;
        }
    }


    @Override
    public void init(Context context, Item item) {
        ArrayList<Item> itemList = new ArrayList<>();
        itemList.add(item);
        this.itemIterator = itemList.iterator();
        currentItem = this.itemIterator.next();
        initialize(context);
    }

    @Override
    public void init(Context context) {
        init(context, false);
    }

    @Override
    public void init(Context context, boolean useCache) {
        try {
            this.itemIterator = itemService.findAllIncludeNotArchived(context);
            if(this.itemIterator.hasNext()) {
                currentItem = this.itemIterator.next();
            }
        } catch (SQLException e) {
            log.error("Error while retrieving all items in the metadata indexer");
        }
        initialize(context);
        this.useCache = useCache;
    }

    protected void initialize(Context context) {
        this.context = context;

        currentFieldIndex = 0;
        currentMetadataIndex = 0;
        useCache = false;
        cache = new HashMap<>();
    }

    @Override
    public AuthorityValue nextValue() {
        return nextValue;
    }


    @Override
    public boolean hasMore() throws SQLException, AuthorizeException {
        if (currentItem == null) {
            return false;
        }

        // 1. iterate over the metadata values

        String metadataField = metadataFields.get(currentFieldIndex);
        List<MetadataValue> values = itemService.getMetadataByMetadataString(currentItem, metadataField);
        if (currentMetadataIndex < values.size()) {
            nextValue = authorityValueService.storeMetadataInAuthorityCache(context, currentItem, metadataField, values.get(currentMetadataIndex));

            currentMetadataIndex++;
            return true;
        } else {

            // 2. iterate over the metadata fields

            if ((currentFieldIndex + 1) < metadataFields.size()) {
                currentFieldIndex++;
                //Reset our current metadata index since we are moving to another field
                currentMetadataIndex = 0;
                return hasMore();
            } else {

                // 3. iterate over the items

                if (itemIterator.hasNext()) {
                    currentItem = itemIterator.next();
                    //Reset our current field index
                    currentFieldIndex = 0;
                    //Reset our current metadata index
                    currentMetadataIndex = 0;
                } else {
                    currentItem = null;
                }
                return hasMore();
            }
        }
    }



    @Override
    public void close() {
        itemIterator = null;
        cache.clear();
    }

    @Override
    public boolean isConfiguredProperly() {
        boolean isConfiguredProperly = true;
        if(CollectionUtils.isEmpty(metadataFields)){
            log.warn("Authority indexer not properly configured, no metadata fields configured for indexing. Check the \"authority.author.indexer.field\" properties.");
            isConfiguredProperly = false;
        }
        return isConfiguredProperly;
    }
}
