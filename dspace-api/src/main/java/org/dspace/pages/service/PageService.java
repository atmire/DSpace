/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.pages.service;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.DSpaceObject;
import org.dspace.core.Context;
import org.dspace.pages.Page;

/**
 * This is the interface class that will deal with all functionality with regards to Page objects
 */
public interface PageService {
    /**
     * This method will create a Page object in the database according to the given parameters.
     * @param context       The relevant DSpace context
     * @param name          The name that the newly created page object will have in the database
     * @param language      The language that the newly created page object will have in the database
     * @return              The created Page object
     * @throws SQLException If something goes wrong
     * @throws AuthorizeException If something goes wrong
     */
    public Page create(Context context, String name, String language, DSpaceObject dSpaceObject)
        throws SQLException, AuthorizeException;

    /**
     * This method will find a Page object by UUID and return it
     * @param context   The relevant DSpace context
     * @param uuid      The UUID on which the Page object should be matched
     * @return          The Page object that has a UUID that is identical to the given UUID
     * @throws SQLException If something goes wrong
     */
    public Page findByUuid(Context context, UUID uuid) throws SQLException;

    /**
     * This method will find a list of Page objects that have the same name as the given name in the parameters
     * and this list will be returned
     * @param context   The relevant DSpace context
     * @param name      The name on which the Page objects will be matched
     * @return          The list of Page objects that have the same name as the name passed along in the parameters
     * @throws SQLException If something goes wrong
     */
    public List<Page> findByName(Context context, String name) throws SQLException;

    /**
     * This method will find a Page object that has the same name and language as given in the parameters and this
     * object will be returned
     * @param context   The relevant DSpace context
     * @param name      The name on which the Page object will be matched
     * @param language  The language on which the Page object will be matched
     * @return          The Page object that has the same name and the same language as given in the parameters
     * @throws SQLException If something goes wrong
     */
    public Page findByNameAndLanguage(Context context, String name, String language) throws SQLException;

    public List<Page> findByDSpaceObject(Context context, DSpaceObject dSpaceObject) throws SQLException;
    /**
     * This method will delete the current Bitstream from the given Page object and it'll create a new Bitstream
     * with the Inputstream given in the parameters to then attach this new Bitstream to the Page object that is passed
     * along in the parameters.
     * The file will get anonymous read rights through this method.
     * @param context       The relevant DSpace context
     * @param inputStream   The Inputstream that is to be used for the new Bitstream
     * @param name          The name for the bitstream to be created for the page
     * @param contentType   The contentType to be used for the bitstream that'll be created for the page
     * @param page          The Page object that will have it's Bitstream altered
     * @throws IOException  If something goes wrong
     * @throws SQLException If something goes wrong
     * @throws AuthorizeException   If something goes wrong
     */
    public void attachFile(Context context, InputStream inputStream, String name, String contentType,
                           Page page)
        throws IOException, SQLException, AuthorizeException;

    /**
     * This method will retrieve all the Page objects currently in the database. This will be redirected to the DAO
     * classes for the Page object
     * @param context   The relevant DSpace context
     * @return          The list containing all Page objects currently in the database
     * @throws SQLException If something goes wrong
     */
    public List<Page> findAll(Context context) throws SQLException;

    /**
     * Updates the Page object in the DB.
     * @param context               The relevant DSpace context
     * @param page                  The Page object to be updated
     * @throws SQLException         If something goes wrong
     * @throws AuthorizeException   If something goes wrong
     */
    public void update(Context context, Page page) throws SQLException, AuthorizeException;

    /**
     * Updates the list of Page objects in the DB.
     * @param context               The relevant DSpace context
     * @param pages                 The Page objects to be updated
     * @throws SQLException         If something goes wrong
     * @throws AuthorizeException   If something goes wrong
     */
    public void update(Context context, List<Page> pages) throws SQLException, AuthorizeException;

    /**
     * Deletes the Page object from the Database
     * @param context               The relevant DSpace context
     * @param page                  The Page object to be deleted
     * @throws SQLException         If something goes wrong
     * @throws AuthorizeException   If something goes wrong
     */
    public void delete(Context context, Page page) throws SQLException, AuthorizeException;

}
