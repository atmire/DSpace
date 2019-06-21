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
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.core.Context;
import org.dspace.discovery.IndexableObject;
import org.dspace.discovery.SearchUtils;
import org.dspace.discovery.configuration.DiscoveryConfiguration;
import org.dspace.discovery.configuration.DiscoveryHitHighlightFieldConfiguration;
import org.dspace.discovery.configuration.DiscoveryHitHighlightingConfiguration;
import org.dspace.discovery.indexobject.service.IndexableCollectionService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author Kevin Van de Velde (kevin at atmire dot com)
 */
public class IndexableCollectionServiceImpl extends IndexableDSpaceObjectServiceImpl<IndexableCollection>
        implements IndexableCollectionService {

    @Autowired
    protected CollectionService collectionService;

    @Autowired
    protected CommunityService communityService;

    @Override
    public Iterator<IndexableCollection> findAll(Context context) throws SQLException {
        Iterator<Collection> collections = collectionService.findAll(context).iterator();

        return new Iterator<IndexableCollection>() {
            @Override
            public boolean hasNext() {
                return collections.hasNext();
            }

            @Override
            public IndexableCollection next() {
                return new IndexableCollection(collections.next());
            }
        };
    }

    @Override
    public String getType() {
        return new IndexableCollection(null).getType();
    }

    /**
     * Build a solr document for a DSpace Collection.
     *
     * @param context    The relevant DSpace Context.
     * @param indexableCollection indexableCollection to be indexed
     * @throws SQLException sql exception
     * @throws IOException  IO exception
     */
    @Override
    public SolrInputDocument buildDocument(Context context, IndexableCollection indexableCollection)
            throws IOException, SQLException {
        // Create Lucene Document
        SolrInputDocument doc = super.buildDocument(context, indexableCollection);

        final Collection collection = indexableCollection.getIndexedObject();

        DiscoveryConfiguration discoveryConfiguration = SearchUtils.getDiscoveryConfiguration(collection);
        DiscoveryHitHighlightingConfiguration highlightingConfiguration = discoveryConfiguration
            .getHitHighlightingConfiguration();
        List<String> highlightedMetadataFields = new ArrayList<>();
        if (highlightingConfiguration != null) {
            for (DiscoveryHitHighlightFieldConfiguration configuration : highlightingConfiguration
                .getMetadataFields()) {
                highlightedMetadataFields.add(configuration.getField());
            }
        }


        // and populate it
        String description = collectionService.getMetadata(collection, "introductory_text");
        String description_abstract = collectionService.getMetadata(collection, "short_description");
        String description_table = collectionService.getMetadata(collection, "side_bar_text");
        String provenance = collectionService.getMetadata(collection, "provenance_description");
        String rights = collectionService.getMetadata(collection, "copyright_text");
        String rights_license = collectionService.getMetadata(collection, "license");
        String title = collectionService.getMetadata(collection, "name");

        List<String> toIgnoreMetadataFields = SearchUtils.getIgnoredMetadataFields(collection.getType());
        addContainerMetadataField(doc, highlightedMetadataFields, toIgnoreMetadataFields, "dc.description",
                                  description);
        addContainerMetadataField(doc, highlightedMetadataFields, toIgnoreMetadataFields, "dc.description.abstract",
                                  description_abstract);
        addContainerMetadataField(doc, highlightedMetadataFields, toIgnoreMetadataFields,
                                  "dc.description.tableofcontents", description_table);
        addContainerMetadataField(doc, highlightedMetadataFields, toIgnoreMetadataFields, "dc.provenance", provenance);
        addContainerMetadataField(doc, highlightedMetadataFields, toIgnoreMetadataFields, "dc.rights", rights);
        addContainerMetadataField(doc, highlightedMetadataFields, toIgnoreMetadataFields, "dc.rights.license",
                                  rights_license);
        addContainerMetadataField(doc, highlightedMetadataFields, toIgnoreMetadataFields, "dc.title", title);

        return doc;
    }

    @Override
    public IndexableObject findIndexableObject(Context context, String id) throws SQLException {
        return new IndexableCollection(collectionService.find(context, UUID.fromString(id)));
    }

    @Override
    public List<String> getLocations(Context context, IndexableCollection indexableCollection) throws SQLException {
        return getCollectionLocations(context, indexableCollection.getIndexedObject());
    }

    public List<String> getCollectionLocations(Context context, Collection collection) throws SQLException {
        List<String> locations = new ArrayList<>();
        // build list of community ids
        List<Community> communities = ContentServiceFactory.getInstance().getCommunityService().
                getAllParents(context, collection);

        // now put those into strings
        for (Community community : communities) {
            locations.add("m" + community.getID());
        }

        return locations;
    }
}
