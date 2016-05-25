/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.indexer;

import org.dspace.authority.AuthorityValue;
import org.apache.log4j.Logger;
import org.dspace.authority.factory.AuthorityServiceFactory;
import org.dspace.authority.service.*;
import org.dspace.core.Context;

import java.util.Map;

/**
 * Class used to reindex dspace authorities in solr
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public class AuthorityIndexClient {

    private static Logger log = Logger.getLogger(AuthorityIndexClient.class);

    protected static final CachedAuthorityService cachedAuthorityService = AuthorityServiceFactory.getInstance().getCachedAuthorityService();

    public static void main(String[] args) throws Exception {

        //Populate our solr
        Context context = new Context();
        //Ensure that we can update items if we are altering our authority control
        context.turnOffAuthorisationSystem();

        if(!cachedAuthorityService.isConfigurationValid()){
                    //Cannot index, configuration not valid
            System.out.println("Cannot index authority values since the configuration isn't valid. Check dspace logs for more information.");

            return;
        }
        
        System.out.println("Retrieving all data");
        log.info("Retrieving all data");

        //Get all our values from the input forms
        Map<String, AuthorityValue> toIndexValues = cachedAuthorityService.getAllCachedAuthorityValues(context);


        log.info("Cleaning the old index");
        System.out.println("Cleaning the old index");
        cachedAuthorityService.cleanCache();
        log.info("Writing new data");
        System.out.println("Writing new data");
        for(String id : toIndexValues.keySet()){
            cachedAuthorityService.writeAuthorityValueToCache(toIndexValues.get(id));
            cachedAuthorityService.commitAuthorityCache();
        }

        context.complete();
        System.out.println("All done !");
        log.info("All done !");
    }

}
