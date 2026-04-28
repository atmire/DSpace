/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.api.token.dao;

import java.sql.SQLException;
import java.util.List;

import org.dspace.api.token.ApiToken;
import org.dspace.core.Context;
import org.dspace.core.GenericDAO;
import org.dspace.eperson.EPerson;

/**
 * This is the Data Access Object for the {@link ApiToken} object
 */
public interface ApiTokenDAO extends GenericDAO<ApiToken> {

    /**
     * Returns a list of all ApiToken objects in the database linked to a given EPerson
     *
     * @param context The current context
     * @param ePerson The EPerson for which to retrieve the tokens
     * @return The list of all ApiToken objects linked a given EPerson
     */
    List<ApiToken> findAllByEPerson(Context context, EPerson ePerson, int limit, int offset) throws SQLException;
}
