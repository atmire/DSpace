/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority;

import org.dspace.authority.factory.AuthorityServiceFactory;
import org.dspace.authority.indexer.AuthorityIndexerInterface;
import org.dspace.authority.indexer.AuthorityIndexingService;
import org.dspace.authority.service.AuthorityService;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * Service implementation for the Metadata Authority
 * This class is responsible for all business logic calls for the Metadata Authority and is autowired by spring
 * This class should never be accessed directly.
 *
 * @author kevinvandevelde at atmire.com
 */
public class AuthorityServiceImpl implements AuthorityService{


    @Autowired(required = true)
    protected AuthorityIndexingService indexingService;
    @Autowired(required = true)
    protected AuthorityServiceFactory authorityServiceFactory;

    protected AuthorityServiceImpl()
    {

    }

    @Override
    public void indexItem(Context context, Item item) throws SQLException, AuthorizeException {
        if(!isConfigurationValid()){
            //Cannot index, configuration not valid
            return;
        }


        for (AuthorityIndexerInterface indexers : authorityServiceFactory.createAuthorityIndexers(context, item)) {
            while (indexers.hasMore())
            {
                AuthorityValue authorityValue = indexers.nextValue();
                if(authorityValue != null)
                    indexingService.indexContent(authorityValue);
            }
            //Close up
            indexers.close();
        }
        //Commit to our server
        indexingService.commit();
    }

    @Override
    public boolean isConfigurationValid()
    {
        if(!indexingService.isConfiguredProperly()){
            return false;
        }

        for (AuthorityIndexerInterface indexerInterface : authorityServiceFactory.createUninitialisedAuthorityIndexers())
        {
            if(!indexerInterface.isConfiguredProperly()){
                return false;
            }
        }
        return true;
    }

    public Map<String, AuthorityValue> getAllAuthorityValues(Context context) throws SQLException, AuthorizeException {
        Map<String, AuthorityValue> toIndexValues = new HashMap<>();

        for (AuthorityIndexerInterface indexerInterface : authorityServiceFactory.createAuthorityIndexers(context, true))
        {
            while (indexerInterface.hasMore()) {
                AuthorityValue authorityValue = indexerInterface.nextValue();
                if(authorityValue != null){
                    toIndexValues.put(authorityValue.getId(), authorityValue);
                }
            }
            //Close up
            indexerInterface.close();
        }
        return toIndexValues;
    }

    @Override
    public void cleanIndex() throws Exception
    {
        indexingService.cleanIndex();
    }

    @Override
    public void indexContent(AuthorityValue authorityValue)
    {
        indexingService.indexContent(authorityValue);
    }

    @Override
    public void commit() {
        indexingService.commit();
    }


}
