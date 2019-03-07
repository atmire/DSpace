/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.pages.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.dspace.core.Context;
import org.dspace.core.GenericDAO;
import org.dspace.pages.Page;

/**
 * Database Access Object interface class for the Page object.
 * The implementation of this class is responsible for all database calls for the Community object and is autowired
 * by spring
 * This class should only be accessed from a single service and should never be exposed outside of the API
 *
 */
public interface PageDao extends GenericDAO<Page> {

    /**
     * This is the DAO implementation for {@link org.dspace.pages.service.PageService#findByUuid(Context, UUID)}
     */
    public Page findByUuid(Context context, UUID uuid) throws SQLException;

    /**
     * This is the DAO implementation for {@link org.dspace.pages.service.PageService#findByName(Context, String)}
     */
    public List<Page> findByName(Context context, String name) throws SQLException;

    /**
     * This is the DAO implementation for
     * {@link org.dspace.pages.service.PageService#findByNameAndLanguage(Context, String, String)}
     */
    public Page findByNameAndLanguage(Context context, String name, String language) throws SQLException;

}
