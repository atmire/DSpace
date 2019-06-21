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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.content.Item;
import org.dspace.core.Context;
import org.dspace.discovery.IndexableObject;
import org.dspace.discovery.SearchUtils;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.indexobject.service.IndexableWorkflowItemService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.service.XmlWorkflowItemService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public class IndexableWorkflowItemServiceImpl extends IndexableInprogressSubmissionServiceImpl<IndexableWorkflowItem>
        implements IndexableWorkflowItemService {

    @Autowired
    protected XmlWorkflowItemService workflowItemService;

    @Override
    public Iterator<IndexableWorkflowItem> findAll(Context context) throws SQLException {
        final Iterator<XmlWorkflowItem> workflowItems = workflowItemService.findAll(context).iterator();

        return new Iterator<IndexableWorkflowItem>() {
            @Override
            public boolean hasNext() {
                return workflowItems.hasNext();
            }

            @Override
            public IndexableWorkflowItem next() {
                return new IndexableWorkflowItem(workflowItems.next());
            }
        };
    }

    @Override
    public String getType() {
        return new IndexableWorkflowItem(null).getType();
    }

    @Override
    public SolrInputDocument buildDocument(Context context, IndexableWorkflowItem indexableObject)
            throws SQLException, IOException {
        final SolrInputDocument doc = super.buildDocument(context, indexableObject);
        final XmlWorkflowItem workflowItem = indexableObject.getIndexedObject();
        final Item item = workflowItem.getItem();
        List<DiscoveryConfiguration> discoveryConfigurations = SearchUtils
                .getAllDiscoveryConfigurations(workflowItem);
        indexableItemService.addDiscoveryFields(doc, context, item, discoveryConfigurations);

        String acvalue = DSpaceServicesFactory.getInstance().getConfigurationService()
                .getProperty("discovery.facet.namedtype.workflow.item");
        if (StringUtils.isBlank(acvalue)) {
            acvalue = indexableObject.getTypeText();
        }
        addNamedResourceTypeIndex(doc, acvalue);

        return doc;
    }

    @Override
    public IndexableObject findIndexableObject(Context context, String id) throws SQLException {
        return new IndexableWorkflowItem(workflowItemService.find(context, Integer.parseInt(id)));
    }
}
