/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.pages;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.DSpaceObject;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.core.Context;
import org.dspace.pages.factory.PagesServiceFactory;
import org.dspace.pages.service.PageService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This is the test class that will take care of all functionality regarding the Page object and its service
 */
public class PageTest extends AbstractUnitTest {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(PageTest.class);

    /**
     * Services to be used in this test file
     */
    protected PageService pageService = PagesServiceFactory.getInstance().getPageService();
    protected BitstreamService bitstreamService = ContentServiceFactory.getInstance().getBitstreamService();
    protected CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
    protected CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
    /**
     * Page instance used in the tests
     */
    private Page page;
    private Community community;
    /**
     * This method will be run before every test as per @Before. It will
     * initialize resources required for the tests.
     *
     * Other methods can be annotated with @Before here or in subclasses
     * but no execution order is guaranteed
     */
    @Before
    @Override
    public void init() {
        super.init();
        try {
            context.turnOffAuthorisationSystem();
            community = communityService.create(null, context);
            this.page = pageService.create(context, "testName", "testLanguage", community);

            context.restoreAuthSystemState();
        } catch (SQLException ex) {
            log.error("SQL Error in init", ex);
            fail("SQL Error in init: " + ex.getMessage());
        } catch (AuthorizeException e) {
            log.error("AuthorizeException Error in init", e);
            fail("AuthorizeException Error in init: " + e.getMessage());
        }
    }

    /**
     * This method will be run after every test as per @After. It will
     * clean resources initialized by the @Before methods.
     *
     * Other methods can be annotated with @After here or in subclasses
     * but no execution order is guaranteed
     */
    @After
    @Override
    public void destroy() {
        context.turnOffAuthorisationSystem();
        try {
            pageService.delete(context, page);
        } catch (SQLException e) {
            log.error("SQL Error in destroy", e);
            fail("SQL Error in destroy: " + e.getMessage());
        } catch (AuthorizeException e) {
            log.error("AuthorizeException Error in destroy", e);
            fail("AuthorizeException Error in destroy: " + e.getMessage());
        }
        try {
            communityService.delete(context, community);
        } catch (SQLException e) {
            log.error("SQL Error in destroy", e);
            fail("SQL Error in destroy: " + e.getMessage());
        } catch (AuthorizeException e) {
            log.error("AuthorizeException Error in destroy", e);
            fail("AuthorizeException Error in destroy: " + e.getMessage());
        } catch (IOException e) {
            log.error("IOException Error in destroy", e);
            fail("IOException Error in destroy: " + e.getMessage());
        }
        page = null;
        community = null;
        super.destroy();
        context.restoreAuthSystemState();
    }

    /**
     * Tests the {@link PageService#findByUuid(Context, UUID)} method
     * @throws Exception
     */
    @Test
    public void testPagesFindByUuid() throws Exception {
        UUID uuid = page.getID();
        Page foundPage = pageService.findByUuid(context, uuid);
        assertThat("testPagesFindByUuid 0", foundPage, notNullValue());
        assertThat("testPagesFindByUuid 1", foundPage.getID(), equalTo(uuid));
        assertThat("testPagesFindByUuid 2", foundPage.getName(), equalTo(page.getName()));
        assertThat("testPagesFindByUuid 3", foundPage.getLanguage(), equalTo(page.getLanguage()));
        assertThat("testPagesFindByUuid 4", foundPage.getBitstream(), equalTo(page.getBitstream()));
    }

    /**
     * Tests the {@link PageService#create(Context, String, String, DSpaceObject)} method
     * @throws Exception
     */
    @Test
    public void testPagesCreate() throws Exception {
        context.turnOffAuthorisationSystem();

        page = pageService.create(context, "anotherTestPage", "anotherTestLanguage", community);


        UUID uuid = page.getID();
        Page foundPage = pageService.findByUuid(context, uuid);
        assertThat("testPagesCreate 0", foundPage, notNullValue());
        assertThat("testPagesCreate 1", foundPage.getID(), equalTo(uuid));
        assertThat("testPagesCreate 2", foundPage.getName(), equalTo(page.getName()));
        assertThat("testPagesCreate 3", foundPage.getLanguage(), equalTo(page.getLanguage()));
        assertThat("testPagesCreate 4", foundPage.getdSpaceObject(), equalTo(community));

        context.restoreAuthSystemState();
    }

    /**
     * Tests the {@link PageService#update(Context, Page)} method
     * @throws Exception
     */
    @Test
    public void testPageUpdate() throws Exception {
        context.turnOffAuthorisationSystem();

        UUID uuid = page.getID();
        Page foundPage = pageService.findByUuid(context, uuid);
        foundPage.setLanguage("ThisIsAtest");
        pageService.update(context, foundPage);
        foundPage = pageService.findByUuid(context, uuid);
        assertThat("testPageUpdate 0", foundPage, notNullValue());
        assertThat("testPageUpdate 1", foundPage.getID(), equalTo(uuid));
        assertThat("testPageUpdate 2", foundPage.getName(), equalTo(page.getName()));
        assertThat("testPageUpdate 3", foundPage.getLanguage(), equalTo("ThisIsAtest"));
        assertThat("testPageUpdate 4", foundPage.getdSpaceObject(), equalTo(page.getdSpaceObject()));

        context.restoreAuthSystemState();
    }

    /**
     * Tests the {@link PageService#delete(Context, Page)} method
     * @throws Exception
     */
    @Test
    public void testPagesDelete() throws Exception {
        context.turnOffAuthorisationSystem();

        Page page = pageService.create(context, "anotherPageTest", "anotherLanguageTest", community);


        UUID uuid = page.getID();
        Page foundPage = pageService.findByUuid(context, uuid);
        assertThat("testPagesDelete 0", foundPage, notNullValue());
        assertThat("testPagesDelete 1", foundPage.getID(), equalTo(uuid));
        assertThat("testPagesDelete 2", foundPage.getName(), equalTo(page.getName()));
        assertThat("testPagesDelete 3", foundPage.getLanguage(), equalTo(page.getLanguage()));
        assertThat("testPagesDelete 4", foundPage.getdSpaceObject(), equalTo(community));

        pageService.delete(context, foundPage);
        foundPage = pageService.findByUuid(context, uuid);
        assertThat("testPagesDelete 5", foundPage, nullValue());
        context.restoreAuthSystemState();
    }

    /**
     * Tests the {@link PageService#findByName(Context, String)} method
     * @throws Exception
     */
    @Test
    public void testPagesFindByName() throws Exception {
        context.turnOffAuthorisationSystem();

        String name = "anotherTestPage";
        Page page = pageService.create(context, name, "tla", community);

        Page secondPage = pageService.create(context, name, "atl", community);


        List<Page> foundPages = pageService.findByName(context, name);
        assertThat("testPagesFindByName 0", foundPages.size(), equalTo(2));

        context.restoreAuthSystemState();
    }

    /**
     * Tests the {@link PageService#findByNameAndLanguage(Context, String, String)} method
     * @throws Exception
     */
    @Test
    public void testPagesFindByNameAndLanguage() throws Exception {
        context.turnOffAuthorisationSystem();

        String name = "anotherTestPage";
        Page page = pageService.create(context, name, "FirstLanguage", community);

        Page secondPage = pageService.create(context, name, "SecondLanguage", community);


        Page foundPage = pageService.findByNameAndLanguage(context, name, "SecondLanguage");
        assertThat("testPagesFindByNameAndLanguage 0", foundPage, notNullValue());
        assertThat("testPagesFindByNameAndLanguage 1", foundPage.getID(), equalTo(secondPage.getID()));
        assertThat("testPagesFindByNameAndLanguage 2", foundPage.getName(), equalTo(secondPage.getName()));
        assertThat("testPagesFindByNameAndLanguage 3", foundPage.getLanguage(), equalTo(secondPage.getLanguage()));
        assertThat("testPagesFindByNameAndLanguage 4", foundPage.getdSpaceObject(), equalTo(community));

        context.restoreAuthSystemState();
    }

    /**
     * Tests the {@link PageService#attachFile(Context, InputStream, String, String, Page)} method
     * @throws Exception
     */
    @Test
    public void testPagesAttachFile() throws Exception {
        context.turnOffAuthorisationSystem();

        Page page = pageService.create(context, "anotherPageTestName", "anotherPageTestLanguage", community);


        UUID uuid = page.getID();
        Page foundPage = pageService.findByUuid(context, uuid);
        assertThat("testPagesAttachFile 0", foundPage, notNullValue());
        assertThat("testPagesAttachFile 1", foundPage.getID(), equalTo(uuid));
        assertThat("testPagesAttachFile 2", foundPage.getName(), equalTo(page.getName()));
        assertThat("testPagesAttachFile 3", foundPage.getLanguage(), equalTo(page.getLanguage()));
        assertThat("testPagesAttachFile 4", foundPage.getBitstream(), equalTo(page.getBitstream()));
        assertThat("testPagesAttachFile 5", foundPage.getdSpaceObject(), equalTo(community));

        Bitstream bitstream = foundPage.getBitstream();
        InputStream newInputStream = IOUtils.toInputStream("secondbitstream");

        pageService.attachFile(context, newInputStream, "testName", "application/pdf", page);

        foundPage = pageService.findByUuid(context, uuid);
        assertNotEquals(bitstream, foundPage.getBitstream());

        context.restoreAuthSystemState();
    }

    @Test
    public void testDeleteCommunityCollectionWithPagesAttached() throws Exception {
        context.turnOffAuthorisationSystem();

        Community community1 = communityService.create(null, context);
        Collection collection = collectionService.create(context, community1);

        Page page1 = pageService.create(context, "testCommunity1Deletion", "en", community1);
        Page page2 = pageService.create(context, "testCollectionDeletion", "en", collection);

        Page foundPage1 = pageService.findByUuid(context, page1.getID());
        Page foundPage2 = pageService.findByUuid(context, page2.getID());

        assertThat("testDeleteCommunityCollectionWithPagesAttached 1", foundPage1.getdSpaceObject(),
                   equalTo(community1));
        assertThat("testDeleteCommunityCollectionWithPagesAttached 2", foundPage2.getdSpaceObject(),
                   equalTo(collection));

        collectionService.delete(context, collection);
        Page foundPage2AfterDeletion = pageService.findByUuid(context, page2.getID());
        assertNull(foundPage2AfterDeletion);
        Collection foundCollectionAfterDeletion = collectionService.find(context, collection.getID());
        assertNull(foundCollectionAfterDeletion);

        communityService.delete(context, community1);
        Page foundPage1AfterDeletion = pageService.findByUuid(context, page1.getID());
        assertNull(foundPage1AfterDeletion);
        Community foundCommunityAfterDeletion = communityService.find(context, community1.getID());
        assertNull(foundCommunityAfterDeletion);
    }
}
