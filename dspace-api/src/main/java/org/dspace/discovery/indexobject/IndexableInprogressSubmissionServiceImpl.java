/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery.indexobject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.apache.solr.common.SolrInputDocument;
import org.dspace.content.InProgressSubmission;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.discovery.indexobject.service.IndexableCollectionService;
import org.dspace.discovery.indexobject.service.IndexableInprogressSubmissionService;
import org.dspace.discovery.indexobject.service.IndexableItemService;
import org.dspace.eperson.EPerson;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public abstract class IndexableInprogressSubmissionServiceImpl<T extends IndexableInProgressSubmission>
        extends IndexableObjectServiceImpl<T> implements IndexableInprogressSubmissionService<T> {

    @Autowired
    protected IndexableCollectionService indexableCollectionService;
    @Autowired
    protected IndexableItemService indexableItemService;


    @Override
    public SolrInputDocument buildDocument(Context context, T indexableObject) throws SQLException, IOException {
        SolrInputDocument doc = super.buildDocument(context, indexableObject);
        storeInprogressItemFields(context, doc, indexableObject.getIndexedObject());
        return doc;
    }

    @Override
    public void storeInprogressItemFields(Context context, SolrInputDocument doc,
                                          InProgressSubmission inProgressSubmission) throws SQLException {
        final Item item = inProgressSubmission.getItem();
        doc.addField("lastModified", item.getLastModified());
        EPerson submitter = inProgressSubmission.getSubmitter();
        if (submitter != null) {
            addFacetIndex(doc, "submitter", submitter.getID().toString(),
                    submitter.getFullName());
        }

        doc.addField("inprogress.item", new IndexableItem(inProgressSubmission.getItem()).getUniqueIndexID());

        // get the location string (for searching by collection & community)
        List<String> locations = indexableCollectionService.
                getCollectionLocations(context, inProgressSubmission.getCollection());
        indexableCollectionService.storeCommunityCollectionLocations(doc, locations);
    }

}
