/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.bulkedit.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.dspace.app.bulkedit.BulkEditChange;
import org.dspace.app.bulkedit.MetadataImportException;
import org.dspace.authorize.AuthorizeException;
import org.dspace.core.Context;
import org.dspace.scripts.handler.DSpaceRunnableHandler;

public interface BulkEditParsingService<T> {
    List<BulkEditChange> parse(Context context, T source)
        throws MetadataImportException, SQLException, AuthorizeException, IOException;

    void setHandler(DSpaceRunnableHandler handler);
}
