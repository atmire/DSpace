/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery;

import java.io.Serializable;
import java.util.Date;

import org.dspace.core.Constants;
import org.dspace.core.ReloadableEntity;

/**
 * This is the basic interface that a data model entity need to implement to be indexable in Discover
 * 
 * @author Andrea Bollini (andrea.bollini at 4science.it)
 *
 * @param <PK>
 *            the Class of the primary key
 */
public interface IndexableObject<T extends ReloadableEntity, PK extends Serializable> {

    /**
     * 
     * @return the string constant representing the Entity Type, @see {@link Constants}
     */
    String getType();

    PK getID();

    T getIndexedObject();

    void setIndexedObject(T object);

    /**
     * 
     * @return an unique id to index
     */
    default String getUniqueIndexID() {
        return getType() + "-" + getID().toString();
    }

    /**
     *
     * @return a textual alias of the Entity Type @see {@link #getType()}
     */
    String getTypeText();

    /**
     * Return the last modified date of an of an object, or if no modification dates are stored, return NUll
     * @return the last modified date
     */
    default Date getLastModified() {
        return null;
    }
}
