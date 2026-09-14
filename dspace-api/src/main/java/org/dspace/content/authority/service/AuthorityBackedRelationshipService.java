/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority.service;

import java.sql.SQLException;

import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.core.Context;

/**
 * Service that marks a metadata value as authority-backed once its authority has
 * been resolved to a related item. This is the single place where such values are
 * stamped, so that the system path (reference-token resolution) and the user path
 * (directly-picked UUID) cannot drift apart.
 * <p>
 * The stamped relationship is not a separate entity: the two sides are held on the
 * metadata value itself (see {@link MetadataValue#setLeftItem} and
 * {@link MetadataValue#setRightItem}) and are written by Hibernate into the
 * {@code relationship} secondary table together with the value's own {@code INSERT}.
 * The row therefore lives and dies with the metadata value it belongs to.
 *
 * @author Adamo Fapohunda (adamo.fapohunda at 4science.com)
 * @author Vincenzo Mecca (vins01-4science - vincenzo.mecca at 4science.com)
 */
public interface AuthorityBackedRelationshipService {

    /**
     * Stamp the given owning metadata value as authority-backed towards the resolved
     * related item, by setting its relationship sides (owner item &rarr; left,
     * related item &rarr; right).
     * <p>
     * The method is idempotent: if the value already carries both sides (see
     * {@link MetadataValue#isRelationshipBacked()}), it is left untouched and
     * {@code false} is returned. This method never modifies the owning metadata
     * value's {@code value} or {@code authority}; stamping the authority is the
     * caller's concern.
     * </p>
     *
     * @param context            the DSpace context
     * @param ownerItem          the item that owns the metadata value; becomes the left side
     * @param ownerMetadataValue the owning metadata value, stamped in place
     * @param relatedItem        the resolved target item; becomes the right side
     * @return {@code true} if the value was stamped, {@code false} if it already was
     *         (or if there is nothing to stamp)
     * @throws SQLException       if a database error occurs
     * @throws AuthorizeException if the current user may not write on either item
     */
    boolean markRelationshipForResolvedAuthority(Context context, Item ownerItem,
        MetadataValue ownerMetadataValue, Item relatedItem) throws SQLException, AuthorizeException;

}
