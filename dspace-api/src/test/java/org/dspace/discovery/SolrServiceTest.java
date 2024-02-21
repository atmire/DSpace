/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.discovery;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.AbstractIntegrationTestWithDatabase;
import org.dspace.builder.CollectionBuilder;
import org.dspace.builder.CommunityBuilder;
import org.dspace.builder.ItemBuilder;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.MetadataValueService;
import org.dspace.discovery.indexobject.IndexableItem;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.junit.Test;

public class SolrServiceTest extends AbstractIntegrationTestWithDatabase {
    protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    protected MetadataValueService metadataValueService = ContentServiceFactory.getInstance().getMetadataValueService();
    protected SearchService searchService = SearchUtils.getSearchService();
    protected SolrSearchCore solrSearchCore = DSpaceServicesFactory.getInstance().getServiceManager()
            .getServicesByType(SolrSearchCore.class).get(0);

    private Community community;
    private Collection collection;
    private Item item;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        context.turnOffAuthorisationSystem();

        community = CommunityBuilder.createCommunity(context).withName("Top Community").build();
        collection = CollectionBuilder.createCollection(context, community).withName("Test Collection").build();
        item = ItemBuilder.createItem(context, collection).withTitle("Test Item").build();

        context.restoreAuthSystemState();
    }

    @Test
    public void directDatabaseNoCommitTest() throws Exception {
        setTitleDatabase("Test Item MODIFIED");
        indexItemNoCommit();
        assertSolrTitle("Test Item");
    }

    @Test
    public void directDatabaseSoftCommitTest() throws Exception {
        setTitleDatabase("Test Item MODIFIED");
        indexItemNoCommit();
        solrSearchCore.getSolr().commit(false, false);
        assertSolrTitle("Test Item MODIFIED");
    }

    @Test
    public void directDatabaseHardCommitTest() throws Exception {
        setTitleDatabase("Test Item MODIFIED");
        indexItemNoCommit();
        solrSearchCore.getSolr().commit(true, true);
        assertSolrTitle("Test Item MODIFIED");
    }

    private void assertSolrTitle(String expectedTitle) throws Exception {
        // Retrieve item document from solr
        SolrQuery query = new SolrQuery();
        query.setQuery("search.resourceid:\"" + item.getID() + "\"");
        SolrDocumentList results = solrSearchCore.getSolr().query(query, solrSearchCore.REQUEST_METHOD).getResults();
        assertEquals(1, results.getNumFound());
        SolrDocument result = results.get(0);

        // Compare the expected value of the title with the actual one
        assertEquals("[" + expectedTitle + "]", result.get("title").toString());
    }

    private void setTitleDatabase(String newTitle) throws Exception {
        // Retrieve the current title
        List<MetadataValue> titles = itemService.getMetadata(item, "dc", "title", null, Item.ANY);
        assertEquals(1, titles.size());
        MetadataValue title = titles.get(0);

        // Modify the title directly in the database, this shouldn't trigger a solr update
        title.setValue(newTitle);
        metadataValueService.update(context, title);
    }

    private void indexItemNoCommit() throws Exception {
        solrSearchCore.indexingService.indexContent(context, new IndexableItem(item), true, false);
    }
}
