/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.factory;

import java.util.*;
import org.dspace.authority.*;
import org.dspace.authority.indexer.*;
import org.dspace.authority.service.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.dspace.services.factory.*;

/**
 * Abstract factory to get services for the authority package, use AuthorityServiceFactory.getInstance() to retrieve an implementation
 *
 * @author kevinvandevelde at atmire.com
 * @author philip at atmire.com
 */
public abstract class AuthorityServiceFactory {

    public abstract AuthoritySearchService getAuthoritySearchService();

    public abstract CachedAuthorityService getCachedAuthorityService();

    public abstract List<AuthorityIndexerInterface> createAuthorityIndexers(Context context);

    public abstract List<AuthorityIndexerInterface> createAuthorityIndexers(Context context, Item item);

    public abstract List<AuthorityIndexerInterface> createUninitialisedAuthorityIndexers();

    public static AuthorityServiceFactory getInstance()
    {
        return DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName("authorityServiceFactory", AuthorityServiceFactory.class);
    }
}
