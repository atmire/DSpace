/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority;

import org.apache.log4j.Logger;
import org.dspace.authority.factory.AuthorityServiceFactory;
import org.dspace.authority.service.CachedAuthorityService;
import org.dspace.authority.service.ItemService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.ItemIterator;
import org.dspace.content.Metadatum;
import org.dspace.core.Context;
import org.dspace.utils.DSpace;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

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

    protected final CachedAuthorityService cachedAuthorityService;

    private final ItemService itemService;

    public MigrateToNewAuthorities(Context context) {
        print = new PrintWriter(System.out);
        this.context = context;
        this.cachedAuthorityService = AuthorityServiceFactory.getInstance().getCachedAuthorityService();
        this.itemService = new DSpace().getServiceManager().getServicesByType(ItemService.class).get(0);
    }

    public static void main(String[] args) {
        Context c = null;
        try {
            c = new Context();
            c.turnOffAuthorisationSystem();

            MigrateToNewAuthorities migrateToNewAuthorities = new MigrateToNewAuthorities(c);

            migrateToNewAuthorities.run();

            c.restoreAuthSystemState();
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

//            if(!authority.getSolrId().equals(authority.getId())){
                try {
                    cachedAuthorityService.deleteAuthorityValueFromCacheById(authority.getSolrId());
                    cachedAuthorityService.writeAuthorityValueToCache(authority);

                    updateItemsWithAuthority(authority);
                } catch (Exception e) {
                    System.out.println("an exception occurred while updating items containing metadata with authority id " + authority.getId());
                }

//            }
        }
    }

    private void updateItemsWithAuthority(AuthorityValue authority) throws SQLException, AuthorizeException {
        String field = authority.getField().replaceAll("_", "\\.");
        ItemIterator itemIterator = null;
        try {
            itemIterator = itemService.findByMetadataFieldAuthority(context, field, authority.getSolrId(), false);

            while (itemIterator.hasNext()) {
                Item next = itemIterator.next();
                List<Metadatum> metadata = next.getMetadata(field, authority.getSolrId());

                if(metadata.size()>0) {
                    String authorityBefore = metadata.get(0).authority;
                    cachedAuthorityService.updateItemMetadataWithAuthority(context, next, metadata.get(0), authority); //should be only one

                    if (!authorityBefore.equals(metadata.get(0).authority)) {
                        System.out.println("Updated metadata authority of item with id " + next.getID() + " and metadata value " + metadata.get(0).value);
                    }
                    next.update();
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
    }


}
