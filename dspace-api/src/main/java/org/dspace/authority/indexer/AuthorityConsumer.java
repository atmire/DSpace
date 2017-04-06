/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.authority.indexer;

import java.util.*;
import org.apache.log4j.*;
import org.dspace.authority.factory.*;
import org.dspace.authority.service.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.event.*;

/**
 * Consumer that takes care of the indexing of authority controlled metadata fields for all items,
 * including workspace and workflow items.
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class AuthorityConsumer implements Consumer {

    private final Logger log = Logger.getLogger(AuthorityConsumer.class);

    /** A set of all item IDs installed which need their authority updated **/
    protected Set<Integer> itemsToUpdateAuthority = null;

    protected CachedAuthorityService cachedAuthorityService;


    @Override
    public void initialize() throws Exception {
        cachedAuthorityService = AuthorityServiceFactory.getInstance().getCachedAuthorityService();
    }

    @Override
    public void consume(Context ctx, Event event) throws Exception {
        if(itemsToUpdateAuthority == null){
            itemsToUpdateAuthority = new HashSet<>();
        }

        DSpaceObject dso = event.getSubject(ctx);
        if(dso instanceof Item){
            Item item = (Item) dso;
            itemsToUpdateAuthority.add(item.getID());
        }
    }

    @Override
    public void end(Context ctx) throws Exception {
        if(itemsToUpdateAuthority == null)
            return;

        try{
            ctx.turnOffAuthorisationSystem();
            for (Integer id : itemsToUpdateAuthority) {
                Item item = Item.find(ctx, id);
                cachedAuthorityService.writeItemAuthorityMetadataValuesToCache(ctx, item);
            }
        } catch (Exception e){
            log.error("Error while consuming the authority consumer", e);

        } finally {
            itemsToUpdateAuthority = null;
            ctx.restoreAuthSystemState();
        }
    }

    @Override
    public void finish(Context ctx) throws Exception {

    }
}
