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
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.core.Context;
import org.dspace.discovery.IndexableObject;
import org.dspace.discovery.indexobject.service.IndexableClaimedTaskService;
import org.dspace.discovery.indexobject.service.IndexableWorkflowItemService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.xmlworkflow.storedcomponents.ClaimedTask;
import org.dspace.xmlworkflow.storedcomponents.service.ClaimedTaskService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public class IndexableClaimedTaskServiceImpl extends IndexableObjectServiceImpl<IndexableClaimedTask>
        implements IndexableClaimedTaskService {

    @Autowired
    protected ClaimedTaskService claimedTaskService;
    @Autowired
    IndexableWorkflowItemService indexableWorkflowItemService;

    @Override
    public Iterator<IndexableClaimedTask> findAll(Context context) throws SQLException {
        final Iterator<ClaimedTask> claimedTasks = claimedTaskService.findAll(context).iterator();
        return new Iterator<IndexableClaimedTask>() {
            @Override
            public boolean hasNext() {
                return claimedTasks.hasNext();
            }

            @Override
            public IndexableClaimedTask next() {
                return new IndexableClaimedTask(claimedTasks.next());
            }
        };
    }

    @Override
    public String getType() {
        return new IndexableClaimedTask(null).getType();
    }

    @Override
    public SolrInputDocument buildDocument(Context context, IndexableClaimedTask indexableObject)
            throws SQLException, IOException {
        final SolrInputDocument doc = super.buildDocument(context, indexableObject);
        final ClaimedTask claimedTask = indexableObject.getIndexedObject();
        indexableWorkflowItemService.storeInprogressItemFields(context, doc, claimedTask.getWorkflowItem());

        addFacetIndex(doc, "action", claimedTask.getActionID(), claimedTask.getActionID());
        addFacetIndex(doc, "step", claimedTask.getStepID(), claimedTask.getStepID());

        doc.addField("taskfor", "e" + claimedTask.getOwner().getID().toString());

        String acvalue = DSpaceServicesFactory.getInstance().getConfigurationService()
                .getProperty("discovery.facet.namedtype.workflow.claimed");
        if (StringUtils.isBlank(acvalue)) {
            acvalue = indexableObject.getTypeText();
        }
        addNamedResourceTypeIndex(doc, acvalue);

        return doc;
    }

    @Override
    public IndexableObject findIndexableObject(Context context, String id) throws SQLException {
        return new IndexableClaimedTask(claimedTaskService.find(context, Integer.parseInt(id)));
    }
}
