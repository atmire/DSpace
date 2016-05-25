/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority;

import java.io.*;
import java.sql.*;
import java.util.*;
import org.apache.log4j.*;
import org.dspace.authority.factory.*;
import org.dspace.authority.service.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.content.factory.*;
import org.dspace.content.service.*;
import org.dspace.core.*;

/**
 * Script MigrateToNewAuthorities is used to migrate the authority solr index of DSpace 5 and below to the changes
 * introduced in DSpace 6.
 * When MigrateToNewAuthorities is run, the authority keys of all indexed authorities are regenerated.
 * All authorities are reindexed.
 * Metadata values containing the old authority keys will be updated to reference the new authority key.
 *
 * @author kevinvandevelde at atmire.com
 * @author philip at atmire.com
 */
public class MigrateToNewAuthorities {

    protected PrintWriter print = null;
    private Context context;

    private static Logger log = Logger.getLogger(UpdateAuthorities.class);

    protected final ItemService itemService;
    protected final CachedAuthorityService cachedAuthorityService;


    public MigrateToNewAuthorities(Context context) {
        print = new PrintWriter(System.out);
        this.context = context;
        this.cachedAuthorityService = AuthorityServiceFactory.getInstance().getCachedAuthorityService();

        this.itemService = ContentServiceFactory.getInstance().getItemService();
    }

    public static void main(String[] args) {
        Context c = null;
        try {
            c = new Context();

            MigrateToNewAuthorities migrateToNewAuthorities = new MigrateToNewAuthorities(c);

            migrateToNewAuthorities.run();

            c.complete();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (c != null && c.isValid()) {
                c.abort();
            }
        }
    }

    public void run() {
        List<AuthorityValue> authorities = cachedAuthorityService.findAllCachedAuthorityValues(context);

        for (AuthorityValue authority : authorities) {

            if(!authority.getSolrId().equals(authority.getId())){
                try {
                    cachedAuthorityService.deleteAuthorityValueFromCacheById(authority.getSolrId());
                    cachedAuthorityService.updateAuthorityValueInCache(authority);

                    updateItemsWihAuthority(authority);
                } catch (Exception e) {
                    print.println("an exception occurred while updating items with authority " + authority.getId());
                }

            }
        }
    }

    private void updateItemsWihAuthority(AuthorityValue authority) throws SQLException, AuthorizeException {
        String field = authority.getField().replaceAll("_", "\\.");
        Iterator<Item> itemIterator = itemService.findByMetadataFieldAuthority(context, field, authority.getSolrId(), false);

        while (itemIterator.hasNext()) {
            Item next = itemIterator.next();
            List<MetadataValue> metadata = itemService.getMetadata(next, field, authority.getSolrId());

            if(metadata.size()>0) {
                  String valueBefore = metadata.get(0).getValue();
                cachedAuthorityService.updateItemMetadataWithAuthority(context, next, metadata.get(0), authority); //should be only one

                if (!valueBefore.equals(metadata.get(0).getValue())) {
                    print.println("Updated item with id " + next.getID());
                }
                itemService.update(context, next);
            }
        }
    }


}
