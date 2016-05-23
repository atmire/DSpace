/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.authority.indexer;

import org.dspace.authority.AuthorityValue;

/**
 * AuthorityIndexingService is responsible for index requests to the authority solr core.
 *
 * @author Antoine Snyers (antoine at atmire.com)
 * @author Kevin Van de Velde (kevin at atmire dot com)
 * @author Ben Bosman (ben at atmire dot com)
 * @author Mark Diggory (markd at atmire dot com)
 */
public interface AuthorityIndexingService {

    /**
     * Index an authority value.
     * @param value
     */
    public void indexContent(AuthorityValue value);

    /**
     * Empty the authority index.
     * @throws Exception
     */
    public void cleanIndex() throws Exception;

    /**
     * Save all changes since the last commit in the authority index.
     */
    public void commit();

    public boolean isConfiguredProperly();

    /**
     * Delete an authority index by it's authority ID
     * @param id
     * the authority ID
     * @throws Exception
     */
    void deleteAuthorityValueById(String id) throws Exception;
}
