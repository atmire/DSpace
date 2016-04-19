/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.authority.service;

import org.dspace.authority.AuthorityValue;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.core.Context;

import java.sql.SQLException;
import java.util.Map;

/**
 * Service interface class for the Metadata Authority
 * The implementation of this class is responsible for all business logic calls for the Metadata Authority and is autowired by spring
 *
 * @author kevinvandevelde at atmire.com
 */
public interface AuthorityService {

    public void processAuthorities(Context context, Item item) throws SQLException, AuthorizeException;

    public boolean isConfigurationValid();

    /**
     * Retrieve all authority values found in the authority cache. Returns a map with as key the identifier of the authority value
     * and as value the authority value.
     *
     * @param context the dspace context
     * @return A map containing all authority values
     * @throws SQLException
     * @throws AuthorizeException
     */
    public Map<String, AuthorityValue> getAllAuthorityValues(Context context) throws SQLException, AuthorizeException;

    public void cleanAuthorityIndex() throws Exception;

    public void indexAuthorityValue(AuthorityValue authorityValue);

    public void commitAuthorityIndex();

    public boolean isAuthorityControlledField(String field);

}
