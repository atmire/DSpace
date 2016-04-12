/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.factory;

import org.dspace.authority.AuthoritySearchService;
import org.dspace.authority.indexer.AuthorityIndexerInterface;
import org.dspace.authority.service.AuthorityService;
import org.dspace.authority.service.AuthorityValueService;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.services.factory.DSpaceServicesFactory;

import java.util.List;

/**
 * Abstract factory to get services for the authority package, use AuthorityServiceFactory.getInstance() to retrieve an implementation
 *
 * @author kevinvandevelde at atmire.com
 */
public abstract class AuthorityServiceFactory {

    public abstract AuthorityValueService getAuthorityValueService();

    public abstract AuthoritySearchService getAuthoritySearchService();

    public abstract AuthorityService getAuthorityService();

    public abstract List<AuthorityIndexerInterface> createAuthorityIndexers(Context context, boolean useCache);

    public abstract List<AuthorityIndexerInterface> createAuthorityIndexers(Context context);

    public abstract List<AuthorityIndexerInterface> createAuthorityIndexers(Context context, Item item);

    public abstract List<AuthorityIndexerInterface> createUninitialisedAuthorityIndexers();

    public static AuthorityServiceFactory getInstance()
    {
        return DSpaceServicesFactory.getInstance().getServiceManager().getServiceByName("authorityServiceFactory", AuthorityServiceFactory.class);
    }
}
