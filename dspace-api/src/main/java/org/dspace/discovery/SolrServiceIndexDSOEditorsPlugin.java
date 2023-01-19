/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery;

import static org.dspace.discovery.IndexingUtils.findDirectlyAuthorizedGroupAndEPersonPrefixedIds;
import static org.dspace.discovery.IndexingUtils.findInheritedAdminGroupIds;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogHelper;
import org.dspace.core.ReloadableEntity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Indexes policies that yield write access to items, collections and communities.
 *
 * @author Koen Pauwels at atmire.com
 */
public class SolrServiceIndexDSOEditorsPlugin implements SolrServiceIndexPlugin {
    private static final Logger log = org.apache.logging.log4j.LogManager
        .getLogger(SolrServiceIndexDSOEditorsPlugin.class);

    @Autowired(required = true)
    protected AuthorizeService authorizeService;

    @Override
    public void additionalIndex(Context context, IndexableObject idxObj, SolrInputDocument document) {
        ReloadableEntity entity = idxObj.getIndexedObject();
        if (!(entity instanceof Community || entity instanceof Collection || entity instanceof Item)) {
            return;
        }
        DSpaceObject dso = (DSpaceObject) entity;
        try {
            indexInheritedAdminRights(context, dso, document);
            indexResourcePolicies(context, dso, document);
        } catch (SQLException e) {
            log.error(LogHelper.getHeader(context, "Error while indexing resource policies",
                "For DSpaceObject with UUID = " + dso.getID()));
        }
    }

    /**
     * Index groups with ADMIN rights on Collections containing the Item, on
     * Communities containing those Collections, and recursively on any Community containing such a
     * Community.
     */
    private void indexInheritedAdminRights(Context context, DSpaceObject dso, SolrInputDocument document)
        // TODO: Strictly speaking we should also check for epersons who received admin rights directly,
        //       without being part of the admin group. Finding them may be a lot slower though.
        throws SQLException {
        if (dso instanceof Item) {
            for (Collection coll : ((Item) dso).getCollections()) {
                indexInheritedAdminRights(context, coll, document);
            }
        } else {
            for (UUID unprefixedID : findComColInheritedAdminGroupIds(context, dso)) {
                document.addField(Constants.INDEX_EDIT, "g" + unprefixedID);
            }
        }
    }

    /**
     * Index groups and epersons with WRITE or direct ADMIN rights on the Item.
     */
    private void indexResourcePolicies(Context context, DSpaceObject dso, SolrInputDocument document)
        throws SQLException {
        List<String> prefixedIds = findDirectlyAuthorizedGroupAndEPersonPrefixedIds(
            authorizeService, context, dso, new int[] {Constants.WRITE, Constants.ADMIN}
        );
        for (String prefixedId : prefixedIds) {
            document.addField(Constants.INDEX_EDIT, prefixedId);
        }
    }

    private List<UUID> findComColInheritedAdminGroupIds(Context context, DSpaceObject dso) throws SQLException {
        if (dso instanceof Collection) {
            return findInheritedAdminGroupIds(context, (Collection) dso);
        } else if (dso instanceof Community) {
            return findInheritedAdminGroupIds(context, (Community) dso);
        } else {
            return List.of();
        }
    }
}
