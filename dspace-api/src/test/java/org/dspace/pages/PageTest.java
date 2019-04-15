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
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Bitstream;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.BitstreamService;
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
    /**
     * Page instance used in the tests
     */
    private Page page;

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
//            File f = new File(testProps.get("test.bitstream").toString());
            //TODO Change to test file
            this.page = pageService.create(context, "testName", "testLanguage");

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
            log.error("SQL Error in init", e);
            fail("SQL Error in init: " + e.getMessage());
        } catch (AuthorizeException e) {
            log.error("AuthorizeException Error in init", e);
            fail("AuthorizeException Error in init: " + e.getMessage());
        }
        page = null;
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
     * Tests the {@link PageService#create(Context, String, String)} method
     * @throws Exception
     */
    @Test
    public void testPagesCreate() throws Exception {
        context.turnOffAuthorisationSystem();

        Page page = new Page();
        page = pageService.create(context, "anotherTestPage", "anotherTestLanguage");


        UUID uuid = page.getID();
        Page foundPage = pageService.findByUuid(context, uuid);
        assertThat("testPagesCreate 0", foundPage, notNullValue());
        assertThat("testPagesCreate 1", foundPage.getID(), equalTo(uuid));
        assertThat("testPagesCreate 2", foundPage.getName(), equalTo(page.getName()));
        assertThat("testPagesCreate 3", foundPage.getLanguage(), equalTo(page.getLanguage()));

        context.restoreAuthSystemState();
    }

    /**
     * Tests the {@link PageService#update(Context, Object)} method
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

        context.restoreAuthSystemState();
    }

    /**
     * Tests the {@link PageService#delete(Context, Object)} method
     * @throws Exception
     */
    @Test
    public void testPagesDelete() throws Exception {
        context.turnOffAuthorisationSystem();

        Page page = pageService.create(context, "anotherPageTest", "anotherLanguageTest");


        UUID uuid = page.getID();
        Page foundPage = pageService.findByUuid(context, uuid);
        assertThat("testPagesDelete 0", foundPage, notNullValue());
        assertThat("testPagesDelete 1", foundPage.getID(), equalTo(uuid));
        assertThat("testPagesDelete 2", foundPage.getName(), equalTo(page.getName()));
        assertThat("testPagesDelete 3", foundPage.getLanguage(), equalTo(page.getLanguage()));

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
        Page page = pageService.create(context, name, "tla");

        Page secondPage = pageService.create(context, name, "atl");


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
        Page page = pageService.create(context, name, "FirstLanguage");

        Page secondPage = pageService.create(context, name, "SecondLanguage");


        Page foundPage = pageService.findByNameAndLanguage(context, name, "SecondLanguage");
        assertThat("testPagesFindByNameAndLanguage 0", foundPage, notNullValue());
        assertThat("testPagesFindByNameAndLanguage 1", foundPage.getID(), equalTo(secondPage.getID()));
        assertThat("testPagesFindByNameAndLanguage 2", foundPage.getName(), equalTo(secondPage.getName()));
        assertThat("testPagesFindByNameAndLanguage 3", foundPage.getLanguage(), equalTo(secondPage.getLanguage()));
        context.restoreAuthSystemState();
    }

    /**
     * Tests the {@link PageService#attachFile(Context, InputStream, String, String, Page)} method
     * @throws Exception
     */
    @Test
    public void testPagesAttachFile() throws Exception {
        context.turnOffAuthorisationSystem();

        Page page = pageService.create(context, "anotherPageTestName", "anotherPageTestLanguage");


        UUID uuid = page.getID();
        Page foundPage = pageService.findByUuid(context, uuid);
        assertThat("testPagesAttachFile 0", foundPage, notNullValue());
        assertThat("testPagesAttachFile 1", foundPage.getID(), equalTo(uuid));
        assertThat("testPagesAttachFile 2", foundPage.getName(), equalTo(page.getName()));
        assertThat("testPagesAttachFile 3", foundPage.getLanguage(), equalTo(page.getLanguage()));
        assertThat("testPagesAttachFile 4", foundPage.getBitstream(), equalTo(page.getBitstream()));

        Bitstream bitstream = foundPage.getBitstream();
        InputStream newInputStream = IOUtils.toInputStream("secondbitstream");

        pageService.attachFile(context, newInputStream, file.getName(), uploadfile.getContentType(), page);

        foundPage = pageService.findByUuid(context, uuid);
        assertNotEquals(bitstream, foundPage.getBitstream());

        context.restoreAuthSystemState();
    }
}
