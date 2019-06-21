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
import org.dspace.discovery.indexobject.service.IndexablePoolTaskService;
import org.dspace.discovery.indexobject.service.IndexableWorkflowItemService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.xmlworkflow.storedcomponents.PoolTask;
import org.dspace.xmlworkflow.storedcomponents.service.PoolTaskService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public class IndexablePoolTaskServiceImpl extends IndexableObjectServiceImpl<IndexablePoolTask>
        implements IndexablePoolTaskService {

    @Autowired
    protected PoolTaskService poolTaskService;

    @Autowired
    IndexableWorkflowItemService indexableWorkflowItemService;

    @Override
    public Iterator<IndexablePoolTask> findAll(Context context) throws SQLException {
        final Iterator<PoolTask> pooledTasks = poolTaskService.findAll(context).iterator();
        return new Iterator<IndexablePoolTask>() {
            @Override
            public boolean hasNext() {
                return pooledTasks.hasNext();
            }

            @Override
            public IndexablePoolTask next() {
                return new IndexablePoolTask(pooledTasks.next());
            }
        };
    }

    @Override
    public String getType() {
        return new IndexablePoolTask(null).getType();
    }

    @Override
    public SolrInputDocument buildDocument(Context context, IndexablePoolTask indexableObject)
            throws SQLException, IOException {
        final SolrInputDocument doc = super.buildDocument(context, indexableObject);
        final PoolTask poolTask = indexableObject.getIndexedObject();
        indexableWorkflowItemService.storeInprogressItemFields(context, doc, poolTask.getWorkflowItem());

        addFacetIndex(doc, "action", poolTask.getActionID(), poolTask.getActionID());
        addFacetIndex(doc, "step", poolTask.getStepID(), poolTask.getStepID());
        if (poolTask.getEperson() != null) {
            doc.addField("taskfor", "e" + poolTask.getEperson().getID().toString());
        }
        if (poolTask.getGroup() != null) {
            doc.addField("taskfor", "g" + poolTask.getGroup().getID().toString());
        }

        String acvalue = DSpaceServicesFactory.getInstance().getConfigurationService()
                .getProperty("discovery.facet.namedtype.workflow.pooled");
        if (StringUtils.isBlank(acvalue)) {
            acvalue = indexableObject.getTypeText();
        }
        addNamedResourceTypeIndex(doc, acvalue);

        return doc;
    }

    @Override
    public IndexableObject findIndexableObject(Context context, String id) throws SQLException {
        return new IndexablePoolTask(poolTaskService.find(context, Integer.parseInt(id)));
    }


}
