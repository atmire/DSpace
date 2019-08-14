/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.projection;

import java.util.Date;
import java.util.List;

import org.dspace.content.Bitstream;
import org.dspace.content.Bundle;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.Site;
import org.dspace.eperson.EPerson;

import org.springframework.data.rest.core.config.Projection;


/**
 * Projection interface that only exposes metadata
 */
@Projection(name = "metadata",
        types = { Item.class, Bundle.class, Bitstream.class,
        Community.class, Collection.class, EPerson.class, Site.class})
public interface MetadataOnlyProjection {

    List<MetadataValue> getMetadata();

    String getHandle();

    String getName();

    boolean isArchived();

    boolean isWithdrawn();

    boolean isDiscoverable();

    Date getLastModified();

}
