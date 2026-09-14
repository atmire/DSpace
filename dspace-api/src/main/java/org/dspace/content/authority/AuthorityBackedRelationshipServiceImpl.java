/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority;

import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.authority.service.AuthorityBackedRelationshipService;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Default implementation of {@link AuthorityBackedRelationshipService}. It stamps
 * the relationship sides on the metadata value itself; Hibernate then writes the
 * {@code relationship} secondary-table row as part of the value's own insert.
 *
 * @author Adamo Fapohunda (adamo.fapohunda at 4science.com)
 * @author Vincenzo Mecca (vins01-4science - vincenzo.mecca at 4science.com)
 */
public class AuthorityBackedRelationshipServiceImpl implements AuthorityBackedRelationshipService {

    private static final Logger log = LogManager.getLogger(AuthorityBackedRelationshipServiceImpl.class);

    @Autowired(required = true)
    protected AuthorizeService authorizeService;

    @Override
    public boolean markRelationshipForResolvedAuthority(Context context, Item ownerItem,
        MetadataValue ownerMetadataValue, Item relatedItem) throws SQLException, AuthorizeException {

        if (ownerMetadataValue == null || relatedItem == null) {
            return false;
        }

        if (ownerMetadataValue.isRelationshipBacked()) {
            // Idempotent: the value already names both sides.
            return false;
        }

        if (!authorizeService.authorizeActionBoolean(context, ownerItem, Constants.WRITE) &&
            !authorizeService.authorizeActionBoolean(context, relatedItem, Constants.WRITE)) {
            throw new AuthorizeException("You do not have write rights on this relationship's items");
        }

        ownerMetadataValue.setLeftItem(ownerItem.getID());
        ownerMetadataValue.setRightItem(relatedItem.getID());

        log.debug("Stamped metadata value {} (item {}) as authority-backed towards item {}",
            ownerMetadataValue.getID(), ownerItem.getID(), relatedItem.getID());

        return true;
    }

}
