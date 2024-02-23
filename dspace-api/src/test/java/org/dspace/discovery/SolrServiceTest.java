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

    /**
     * This test confirms that changes made to items directly, without DSpace's service layer, has no effect on
     * their solr index (because the change is never indexed)
     * This test merely exists to back up the idea that our item changes are NOT indexed until we want it to.
     */
    @Test
    public void directDatabaseNoCommitTest() throws Exception {
        setTitleDatabase("Test Item MODIFIED");
        indexItemNoCommit();
        assertSolrTitle("Test Item");
    }

    /**
     * This test performs a "soft" commit after making changes to an item, confirming the changes are immediately
     * present in solr
     */
    @Test
    public void directDatabaseSoftCommitTest() throws Exception {
        setTitleDatabase("Test Item MODIFIED");
        indexItemNoCommit();
        solrSearchCore.getSolr().commit(false, false);
        assertSolrTitle("Test Item MODIFIED");
    }

    /**
     * This test performs a "hard" commit after making changes to an item, confirming the changes are immediately
     * present in solr
     */
    @Test
    public void directDatabaseHardCommitTest() throws Exception {
        setTitleDatabase("Test Item MODIFIED");
        indexItemNoCommit();
        solrSearchCore.getSolr().commit(true, true);
        assertSolrTitle("Test Item MODIFIED");
    }

    /**
     * This test confirms that deleting an item through DSpace's service layer has an immediate effect on the indexed
     * solr content of that item (removed from index).
     * This essentially tests whether a "hard" commit happens somewhere within the service layer upon item deletion,
     * which is important, because "soft" commits won't have an immediate impact on solr content when an index is
     * removed.
     */
    @Test
    public void deleteItemSolrTest() throws Exception {
        // Confirm the item is present in solr
        SolrDocumentList results = getSolrDocumentList();
        assertEquals(1, results.getNumFound());

        // Delete the item
        ItemBuilder.deleteItem(item.getID());

        // Confirm the item isn't present in solr
        results = getSolrDocumentList();
        assertEquals(0, results.getNumFound());
    }

    private SolrDocumentList getSolrDocumentList() throws Exception {
        SolrQuery query = new SolrQuery();
        query.setQuery("search.resourceid:\"" + item.getID() + "\"");
        return solrSearchCore.getSolr().query(query, solrSearchCore.REQUEST_METHOD).getResults();
    }

    private void assertSolrTitle(String expectedTitle) throws Exception {
        // Retrieve item document from solr
        SolrDocumentList results = getSolrDocumentList();
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
