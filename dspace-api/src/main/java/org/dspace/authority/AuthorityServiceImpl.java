/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority;

import java.sql.*;
import java.util.*;
import org.dspace.authority.factory.*;
import org.dspace.authority.indexer.*;
import org.dspace.authority.service.*;
import org.dspace.authorize.*;
import org.dspace.content.*;
import org.dspace.core.*;
import org.springframework.beans.factory.annotation.*;


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

    /** The prefix of the authority controlled field */
    protected static final String AC_PREFIX = "authority.controlled.";

    protected Set<String> authorityControlled;

    protected AuthorityServiceImpl()
    {

    }

    @Override
    public void processAuthorities(Context context, Item item) throws SQLException, AuthorizeException {
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
    public void cleanAuthorityIndex() throws Exception
    {
        indexingService.cleanIndex();
    }

    @Override
    public void indexAuthorityValue(AuthorityValue authorityValue)
    {
        indexingService.indexContent(authorityValue);
    }

    @Override
    public void commitAuthorityIndex() {
        indexingService.commit();
    }

    @Override
    public boolean isAuthorityControlledField(String field) {
        if(authorityControlled==null){
            setAuthorizedMetadataFields();
        }

        return authorityControlled.contains(field);
    }

    /**
     * Set authority controlled fields
     *
     */
    private void setAuthorizedMetadataFields()
    {
        authorityControlled = new HashSet<String>();
        Enumeration propertyNames = ConfigurationManager.getProperties().propertyNames();
        while(propertyNames.hasMoreElements())
        {
            String key = ((String) propertyNames.nextElement()).trim();
            if (key.startsWith(AC_PREFIX)
                    && ConfigurationManager.getBooleanProperty(key, false))
            {
                authorityControlled.add(key.substring(AC_PREFIX.length()));
            }
        }
    }


}
