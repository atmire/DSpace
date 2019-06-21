/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery.indexobject;

import org.dspace.xmlworkflow.storedcomponents.XmlWorkflowItem;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public class IndexableWorkflowItem extends IndexableInProgressSubmission<XmlWorkflowItem> {


    public IndexableWorkflowItem(XmlWorkflowItem inProgressSubmission) {
        super(inProgressSubmission);
    }

    @Override
    public String getType() {
        return IndexableWorkflowItem.class.getSimpleName();
    }

    @Override
    public String getTypeText() {
        return "WORKFLOWITEM";
    }

    @Override
    public Integer getID() {
        return getIndexedObject().getID();
    }
}
