/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery;

import static org.dspace.discovery.IndexingUtils.findDirectlyAuthorizedGroupAndEPersonPrefixedIds;
import static org.dspace.discovery.IndexingUtils.findTransitiveAdminGroupIds;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.logging.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.Collection;
import org.dspace.content.Item;
import org.dspace.content.dao.LocationDAO;
import org.dspace.content.dao.impl.DSOWithPoliciesDAOImpl;
import org.dspace.content.dao.impl.LocationDAOImpl;
import org.dspace.content.dao.pojo.DsoWithPolicies;
import org.dspace.content.dao.pojo.Location;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.LogHelper;
import org.dspace.discovery.indexobject.IndexableItem;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Indexes policies that yield write access to items.
 *
 * @author Koen Pauwels at atmire.com
 */
public class SolrServiceIndexItemEditorsPlugin implements SolrServiceIndexPlugin {
    private static final Logger log = org.apache.logging.log4j.LogManager
        .getLogger(SolrServiceIndexItemEditorsPlugin.class);

    @Autowired(required = true)
    protected AuthorizeService authorizeService;
    @Autowired
    private LocationDAOImpl locationDAO;
    @Autowired
    private DSOWithPoliciesDAOImpl dsoWithPoliciesDAO;

    @Override
    public void additionalIndex(Context context, IndexableObject idxObj, SolrInputDocument document) {
        if (idxObj instanceof IndexableItem) {
            Item item = ((IndexableItem) idxObj).getIndexedObject();
            if (item != null) {
                try {
                    // Index groups with ADMIN rights on Collections containing the Item, on
                    // Communities containing those Collections, and recursively on any Community containing ssuch a
                    // Community.
                    // TODO: Strictly speaking we should also check for epersons who received admin rights directly,
                    //       without being part of the admin group. Finding them may be a lot slower though.
                    //
                    Location itemLocation = locationDAO.findLocationByDsoId(context, item.getID());
                    List<UUID> colUUIDS = itemLocation.getLocationColl();
                    List<UUID> comUUIDS = itemLocation.getLocationComm();

                    for (UUID id : colUUIDS) {
                        DsoWithPolicies dsoWithPoliciesForCollection =
                                dsoWithPoliciesDAO.findDsoWithPoliciesByDsoId(context, id);
                        dsoWithPoliciesForCollection.getAdminPolicyIds().forEach(prefixedId -> {
                            document.addField("edit", prefixedId);
                        });
                    }

                    for (UUID id : comUUIDS) {
                        DsoWithPolicies dsoWithPoliciesForCommunity =
                                dsoWithPoliciesDAO.findDsoWithPoliciesByDsoId(context, id);
                        dsoWithPoliciesForCommunity.getAdminPolicyIds().forEach(prefixedId -> {
                            document.addField("edit", prefixedId);
                        });
                    }

                    // Index groups and epersons with WRITE or direct ADMIN rights on the Item.

                    DsoWithPolicies dsoWithPoliciesForItem =
                            dsoWithPoliciesDAO.findDsoWithPoliciesByDsoId(context, item.getID());

                    Set<String> prefixedIds = SetUtils.union(
                            dsoWithPoliciesForItem.getAdminPolicyIds(),
                            dsoWithPoliciesForItem.getEditPolicyIds()
                    );

                    for (String prefixedId : prefixedIds) {
                        document.addField("edit", prefixedId);
                    }
                } catch (SQLException e) {
                    log.error(LogHelper.getHeader(context, "Error while indexing resource policies",
                        "Item: (id " + item.getID() + " name " + item.getName() + ")" ));
                }
            }
        }
    }
}
