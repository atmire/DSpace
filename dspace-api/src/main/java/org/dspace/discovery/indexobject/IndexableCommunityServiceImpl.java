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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.solr.common.SolrInputDocument;
import org.dspace.content.Community;
import org.dspace.content.service.CommunityService;
import org.dspace.core.Context;
import org.dspace.discovery.IndexableObject;
import org.dspace.discovery.SearchUtils;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoveryHitHighlightFieldConfiguration;
import org.dspace.discovery.configuration.DiscoveryHitHighlightingConfiguration;
import org.dspace.discovery.indexobject.service.IndexableCommunityService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public class IndexableCommunityServiceImpl extends IndexableDSpaceObjectServiceImpl<IndexableCommunity>
        implements IndexableCommunityService {

    @Autowired(required = true)
    protected CommunityService communityService;

    @Override
    public Iterator<IndexableCommunity> findAll(Context context) throws SQLException {
        Iterator<Community> communities = communityService.findAll(context).iterator();

        return new Iterator<IndexableCommunity>() {
            @Override
            public boolean hasNext() {
                return communities.hasNext();
            }

            @Override
            public IndexableCommunity next() {
                return new IndexableCommunity(communities.next());
            }
        };
    }

    @Override
    public String getType() {
        return new IndexableCommunity(null).getType();
    }


    @Override
    public SolrInputDocument buildDocument(Context context, IndexableCommunity indexableObject)
            throws SQLException, IOException {
        SolrInputDocument doc = super.buildDocument(context, indexableObject);
        final Community community = indexableObject.getIndexedObject();

        DiscoveryConfiguration discoveryConfiguration = SearchUtils.getDiscoveryConfiguration(community);
        DiscoveryHitHighlightingConfiguration highlightingConfiguration = discoveryConfiguration
            .getHitHighlightingConfiguration();
        List<String> highlightedMetadataFields = new ArrayList<String>();
        if (highlightingConfiguration != null) {
            for (DiscoveryHitHighlightFieldConfiguration configuration : highlightingConfiguration
                .getMetadataFields()) {
                highlightedMetadataFields.add(configuration.getField());
            }
        }

        // and populate it
        String description = communityService.getMetadata(community, "introductory_text");
        String description_abstract = communityService.getMetadata(community, "short_description");
        String description_table = communityService.getMetadata(community, "side_bar_text");
        String rights = communityService.getMetadata(community, "copyright_text");
        String title = communityService.getMetadata(community, "name");

        List<String> toIgnoreMetadataFields = SearchUtils.getIgnoredMetadataFields(community.getType());
        addContainerMetadataField(doc, highlightedMetadataFields, toIgnoreMetadataFields, "dc.description",
                                  description);
        addContainerMetadataField(doc, highlightedMetadataFields, toIgnoreMetadataFields, "dc.description.abstract",
                                  description_abstract);
        addContainerMetadataField(doc, highlightedMetadataFields, toIgnoreMetadataFields,
                                  "dc.description.tableofcontents", description_table);
        addContainerMetadataField(doc, highlightedMetadataFields, toIgnoreMetadataFields, "dc.rights", rights);
        addContainerMetadataField(doc, highlightedMetadataFields, toIgnoreMetadataFields, "dc.title", title);
        return doc;
    }

    @Override
    public IndexableObject findIndexableObject(Context context, String id) throws SQLException {
        return new IndexableCommunity(communityService.find(context, UUID.fromString(id)));
    }

    @Override
    public List<String> getLocations(Context context, IndexableCommunity indexableDSpaceObject) throws SQLException {
        final Community target = indexableDSpaceObject.getIndexedObject();
        List<String> locations = new ArrayList<>();
        // build list of community ids
        List<Community> communities = target.getParentCommunities();

        // now put those into strings
        for (Community community : communities) {
            locations.add("m" + community.getID());
        }

        return locations;
    }
}
