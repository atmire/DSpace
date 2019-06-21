/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery.indexobject;

import java.util.Date;

import org.dspace.content.Item;
import org.dspace.core.Constants;

/**
 * Item implementation for the IndexableObject
 *
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public class IndexableItem extends IndexableDSpaceObject<Item> {

    public IndexableItem(Item dso) {
        super(dso);
    }

    @Override
    public Date getLastModified() {
        return getIndexedObject().getLastModified();
    }

    @Override
    public String getType() {
        return String.valueOf(Constants.ITEM);
    }
}
