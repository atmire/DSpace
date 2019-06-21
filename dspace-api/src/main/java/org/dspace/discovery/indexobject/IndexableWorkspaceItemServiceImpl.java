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
import org.dspace.content.WorkspaceItem;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.Context;
import org.dspace.discovery.IndexableObject;
import org.dspace.discovery.SearchUtils;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.indexobject.service.IndexableWorkspaceItemService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public class IndexableWorkspaceItemServiceImpl extends IndexableInprogressSubmissionServiceImpl<IndexableWorkspaceItem>
        implements IndexableWorkspaceItemService {

    @Autowired
    protected WorkspaceItemService workspaceItemService;

    @Override
    public Iterator<IndexableWorkspaceItem> findAll(Context context) throws SQLException {
        final Iterator<WorkspaceItem> workspaceItems = workspaceItemService.findAll(context).iterator();

        return new Iterator<IndexableWorkspaceItem>() {
            @Override
            public boolean hasNext() {
                return workspaceItems.hasNext();
            }

            @Override
            public IndexableWorkspaceItem next() {
                return new IndexableWorkspaceItem(workspaceItems.next());
            }
        };
    }

    @Override
    public String getType() {
        return new IndexableWorkspaceItem(null).getType();
    }

    @Override
    public SolrInputDocument buildDocument(Context context, IndexableWorkspaceItem indexableObject)
            throws SQLException, IOException {
        final SolrInputDocument doc = super.buildDocument(context, indexableObject);

        String acvalue = DSpaceServicesFactory.getInstance().getConfigurationService()
                .getProperty("discovery.facet.namedtype.workspace");
        if (StringUtils.isBlank(acvalue)) {
            acvalue = indexableObject.getTypeText();
        }
        addNamedResourceTypeIndex(doc, acvalue);
        final WorkspaceItem inProgressSubmission = indexableObject.getIndexedObject();

        List<DiscoveryConfiguration> discoveryConfigurations = SearchUtils
                .getAllDiscoveryConfigurations(inProgressSubmission);
        indexableItemService.addDiscoveryFields(doc, context, inProgressSubmission.getItem(), discoveryConfigurations);

        return doc;
    }

    @Override
    public IndexableObject findIndexableObject(Context context, String id) throws SQLException {
        return new IndexableWorkspaceItem(workspaceItemService.find(context, Integer.parseInt(id)));
    }
}
