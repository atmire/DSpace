/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery.indexobject.service;

import java.sql.SQLException;

import org.apache.solr.common.SolrInputDocument;
import org.dspace.content.InProgressSubmission;
import org.dspace.core.Context;
import org.dspace.discovery.indexobject.IndexableInProgressSubmission;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public interface IndexableInprogressSubmissionService<T extends IndexableInProgressSubmission>
        extends IndexableObjectService<T> {

    void storeInprogressItemFields(Context context, SolrInputDocument doc, InProgressSubmission inProgressSubmission)
            throws SQLException;
}
