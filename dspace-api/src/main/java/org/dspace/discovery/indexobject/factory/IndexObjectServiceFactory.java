/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery.indexobject.factory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.discovery.IndexableObject;
import org.dspace.discovery.indexobject.IndexableClaimedTask;
import org.dspace.discovery.indexobject.IndexableCollection;
import org.dspace.discovery.indexobject.IndexableCommunity;
import org.dspace.discovery.indexobject.IndexableItem;
import org.dspace.discovery.indexobject.IndexablePoolTask;
import org.dspace.discovery.indexobject.IndexableWorkflowItem;
import org.dspace.discovery.indexobject.IndexableWorkspaceItem;
import org.dspace.discovery.indexobject.service.IndexableObjectService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.xmlworkflow.storedcomponents.ClaimedTask;
import org.dspace.xmlworkflow.storedcomponents.PoolTask;
import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;
import org.dspace.xmlworkflow.storedcomponents.service.ClaimedTaskService;
import org.dspace.xmlworkflow.storedcomponents.service.PoolTaskService;
import org.dspace.xmlworkflow.storedcomponents.service.XmlWorkflowItemService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public abstract class IndexObjectServiceFactory {

    @Autowired
    protected WorkspaceItemService workspaceItemService;
    @Autowired
    protected XmlWorkflowItemService xmlWorkflowItemService;
    @Autowired
    protected ClaimedTaskService claimedTaskService;
    @Autowired
    protected PoolTaskService poolTaskService;

    /**
     * Return the list of all the available implementations of the IndexableObjectService interface
     *
     * @return the list of IndexableObjectService
     */
    public abstract List<IndexableObjectService> getIndexableObjectServices();

    public IndexableObjectService getIndexableObjectService(IndexableObject indexableObject) {
        return getIndexableObjectService(indexableObject.getType());
    }

    public IndexableObjectService getIndexableObjectService(String indexableObjectUniqueString) {
        // The unique identifier of an IndexableObject will always be {type}-{identifier}
        return getIndexableObjectServiceByType(StringUtils.substringBefore(indexableObjectUniqueString, "-"));
    }

    public IndexableObjectService getIndexableObjectServiceByType(String indexableServiceType) {
        for (IndexableObjectService indexableObjectService : getIndexableObjectServices()) {
            if (indexableObjectService.getType().equals(indexableServiceType)) {
                return indexableObjectService;
            }
        }
        return null;
    }

    /**
     * Retrieve all the indexable objects for the provided object
     * @param object
     * @return
     */
    public List<IndexableObject> getIndexableObjects(Context context, Object object) throws SQLException {
        List<IndexableObject> results = new ArrayList<>();
        if (object instanceof DSpaceObject) {
            switch ((((DSpaceObject) object).getType())) {
                case Constants.COMMUNITY:
                    results.add(new IndexableCommunity((Community) object));
                    break;
                case Constants.COLLECTION:
                    results.add(new IndexableCollection((Collection) object));
                    break;
                case Constants.ITEM:
                    final Item item = (Item) object;
                    results.add(new IndexableItem(item));

                    // Workflow / workspace items don't have a collection, so check before firing off additional queries
                    if (item.getOwningCollection() == null) {
                        // Check if we have a workflow / workspace item
                        final WorkspaceItem workspaceItem = workspaceItemService.findByItem(context, item);
                        if (workspaceItem != null) {
                            results.add(new IndexableWorkspaceItem(workspaceItem));
                        } else {
                            // Check if we a workflow item
                            final XmlWorkflowItem xmlWorkflowItem = xmlWorkflowItemService.findByItem(context, item);
                            if (xmlWorkflowItem != null) {
                                results.add(new IndexableWorkflowItem(xmlWorkflowItem));
                                List<ClaimedTask> claimedTasks = claimedTaskService.find(context, xmlWorkflowItem);
                                List<PoolTask> pools = poolTaskService.find(context, xmlWorkflowItem);
                                for (PoolTask poolTask : pools) {
                                    results.add(new IndexablePoolTask(poolTask));
                                }
                                for (ClaimedTask claimedTask : claimedTasks) {
                                    results.add(new IndexableClaimedTask(claimedTask));
                                }
                            }
                        }
                    }
                    break;
                default:
                    throw new IllegalArgumentException("The object: " + object.getClass().getName()
                            + " cannot be indexed");


            }
        } else {
            throw new IllegalArgumentException("The object: " + object.getClass().getName()
                    + " cannot be indexed");
        }
        return results;
    }

    public static IndexObjectServiceFactory getInstance() {
        return DSpaceServicesFactory.getInstance().getServiceManager()
                                    .getServiceByName("indexObjectFactory", IndexObjectServiceFactory.class);
    }
}
