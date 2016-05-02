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
 * @author philip at atmire.com
 */
public class MigrateToNewAuthorities {

    protected PrintWriter print = null;
    private Context context;

    private static Logger log = Logger.getLogger(UpdateAuthorities.class);

    protected final ItemService itemService;
    protected final AuthorityValueService authorityValueService;

    public MigrateToNewAuthorities(Context context) {
        print = new PrintWriter(System.out);
        this.context = context;
        this.authorityValueService = AuthorityServiceFactory.getInstance().getAuthorityValueService();
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
        List<AuthorityValue> authorities = authorityValueService.findAllAuthorityValues(context);

        for (AuthorityValue authority : authorities) {

            if(!authority.getSolrId().equals(authority.getId())){
                try {
                    authorityValueService.deleteAuthorityValueById(authority.getSolrId());
                    authorityValueService.updateAuthorityValue(authority);

                    updateItemsWihAuthority(authority);
                } catch (Exception e) {
                    print.println("an exception occurred while updating items with authority " + authority.getId());
                }

            }
        }
    }

    private AuthorityValue createNewAuthority(AuthorityValue authority){
        return authorityValueService.createAuthorityValue(context, authority.generateString(), authority.getValue(), authority.getField());
    }

    private void updateItemsWihAuthority(AuthorityValue authority) throws SQLException, AuthorizeException {
        String field = authority.getField().replaceAll("_", "\\.");
        Iterator<Item> itemIterator = itemService.findByMetadataFieldAuthority(context, field, authority.getSolrId(), false);

        while (itemIterator.hasNext()) {
            Item next = itemIterator.next();
            List<MetadataValue> metadata = itemService.getMetadata(next, field, authority.getId());
            String valueBefore = metadata.get(0).getValue();
            authorityValueService.updateItemMetadataWithAuthority(context, next, metadata.get(0), authority); //should be only one

            if (!valueBefore.equals(metadata.get(0).getValue())) {
                print.println("Updated item with id " + next.getID());
            }
        }
    }


}
