/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.bulkedit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.dspace.AbstractIntegrationTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.ItemService;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.EPerson;
import org.dspace.eperson.Group;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.EPersonService;
import org.dspace.eperson.service.GroupService;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MetadataImportTest extends AbstractIntegrationTest {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(MetadataImportTest.class);

    private ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    private CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
    private CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
    private EPersonService ePersonService = EPersonServiceFactory.getInstance().getEPersonService();

    @Before
    public void init() {
        ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();

        configurationService.setProperty("authority.controlled.dc.contributor.author", true);
        configurationService.setProperty("authority.category.dc.contributor.author", "person");
        configurationService.setProperty("choices.plugin.dc.contributor.author", "CachedAuthority");
        configurationService.setProperty("choices.presentation.dc.contributor.author", "authorLookup");

        super.init();

    }

    @After
    public void destroy() {
        context.turnOffAuthorisationSystem();
        try {
            Iterator<Item> itemIterator = itemService.findAll(context);
            while (itemIterator.hasNext()) {
                Item item = itemIterator.next();
                itemService.delete(context, item);
            }
        } catch (SQLException | IOException | AuthorizeException e) {
            log.error(e.getMessage(), e);
        }
        context.restoreAuthSystemState();
        super.destroy();
    }

    @Test
    public void createItemMetadataImportTest() throws Exception {

        context.turnOffAuthorisationSystem();

        createAdminEPerson();


        Community community = communityService.create(null, context);
        Collection collection = collectionService.create(context, community);

        InputStream testcsv = MetadataImportTest.class.getClassLoader()
                                                      .getResourceAsStream("createitemmetadataimport.csv");

        String testCsvString = IOUtils.toString(testcsv, StandardCharsets.UTF_8);
        testCsvString = StringUtils.replace(testCsvString, "{collectionhandleplaceholder}", collection.getHandle());


        File file = new File("testing");

        FileUtils.copyInputStreamToFile(IOUtils.toInputStream(testCsvString, StandardCharsets.UTF_8), file);

        MetadataImport.main(new String[] {"-f", file.getAbsolutePath(), "-e", "admin@email.com", "-s"});

        List<Item> items = (List<Item>) IteratorUtils.toList(itemService.findAll(context));
        Assert.assertTrue(items.size() == 1);
        Item importedItem = items.get(0);
        List<MetadataValue> nullAuthorityList = itemService
            .getMetadata(importedItem, "dc", "contributor", "author", Item.ANY, null);
        List<MetadataValue> firstOrcidList = itemService
            .getMetadata(importedItem, "dc", "contributor", "author", Item.ANY, "0000-0000-0000-0000");
        List<MetadataValue> secondOrcidList = itemService
            .getMetadata(importedItem, "dc", "contributor", "author", Item.ANY, "1111-1111-1111-1111");

        Assert.assertTrue(nullAuthorityList.size() == 1);
        Assert.assertTrue(StringUtils.equals(nullAuthorityList.get(0).getValue(), "SmithieBenji"));
        Assert.assertTrue(StringUtils.equals(nullAuthorityList.get(0).getAuthority(), null));
        Assert.assertTrue(firstOrcidList.size() == 1);
        Assert.assertTrue(StringUtils.equals(firstOrcidList.get(0).getValue(), null));
        Assert.assertTrue(StringUtils.equals(firstOrcidList.get(0).getAuthority(), "0000-0000-0000-0000"));
        Assert.assertTrue(secondOrcidList.size() == 1);
        Assert.assertTrue(StringUtils.equals(secondOrcidList.get(0).getValue(), null));
        Assert.assertTrue(StringUtils.equals(secondOrcidList.get(0).getAuthority(), "1111-1111-1111-1111"));

        Assert.assertTrue(itemService.getMetadata(importedItem, "dc", "contributor", "author", Item.ANY).size() == 3);


    }

    private void createAdminEPerson() throws SQLException, AuthorizeException {
        EPerson admin = ePersonService.findByEmail(context, "admin@email.com");
        if (admin == null) {
            admin = ePersonService.create(context);
            admin.setFirstName(context, "first (admin)");
            admin.setLastName(context, "last (admin)");
            admin.setEmail("admin@email.com");
            admin.setCanLogIn(true);
            admin.setLanguage(context, I18nUtil.getDefaultLocale().getLanguage());
            ePersonService.setPassword(admin, "test");
            // actually save the eperson to unit testing DB
            ePersonService.update(context, admin);
            GroupService groupService = EPersonServiceFactory.getInstance().getGroupService();
            Group adminGroup = groupService.findByName(context, Group.ADMIN);
            groupService.addMember(context, adminGroup, admin);
        }
    }

    @Test
    public void updateItemMetadataImportTest() throws SQLException, AuthorizeException, IOException {

        context.turnOffAuthorisationSystem();

        createAdminEPerson();


        Community community = communityService.create(null, context);
        Collection collection = collectionService.create(context, community);

        InputStream testcsv = MetadataImportTest.class.getClassLoader()
                                                      .getResourceAsStream("createitemmetadataimport.csv");

        String testCsvString = IOUtils.toString(testcsv, StandardCharsets.UTF_8);
        testCsvString = StringUtils.replace(testCsvString, "{collectionhandleplaceholder}", collection.getHandle());


        File file = new File("testing");

        FileUtils.copyInputStreamToFile(IOUtils.toInputStream(testCsvString, StandardCharsets.UTF_8), file);

        MetadataImport.main(new String[] {"-f", file.getAbsolutePath(), "-e", "admin@email.com", "-s"});

        List<Item> items = (List<Item>) IteratorUtils.toList(itemService.findAll(context));
        Assert.assertTrue(items.size() == 1);
        Item importedItem = items.get(0);
        List<MetadataValue> nullAuthorityList = itemService
            .getMetadata(importedItem, "dc", "contributor", "author", Item.ANY, null);
        List<MetadataValue> firstOrcidList = itemService
            .getMetadata(importedItem, "dc", "contributor", "author", Item.ANY, "0000-0000-0000-0000");
        List<MetadataValue> secondOrcidList = itemService
            .getMetadata(importedItem, "dc", "contributor", "author", Item.ANY, "1111-1111-1111-1111");

        Assert.assertTrue(nullAuthorityList.size() == 1);
        Assert.assertTrue(StringUtils.equals(nullAuthorityList.get(0).getValue(), "SmithieBenji"));
        Assert.assertTrue(StringUtils.equals(nullAuthorityList.get(0).getAuthority(), null));
        Assert.assertTrue(firstOrcidList.size() == 1);
        Assert.assertTrue(StringUtils.equals(firstOrcidList.get(0).getValue(), null));
        Assert.assertTrue(StringUtils.equals(firstOrcidList.get(0).getAuthority(), "0000-0000-0000-0000"));
        Assert.assertTrue(secondOrcidList.size() == 1);
        Assert.assertTrue(StringUtils.equals(secondOrcidList.get(0).getValue(), null));
        Assert.assertTrue(StringUtils.equals(secondOrcidList.get(0).getAuthority(), "1111-1111-1111-1111"));

        Assert.assertTrue(itemService.getMetadata(importedItem, "dc", "contributor", "author", Item.ANY).size() == 3);


        testcsv = MetadataImportTest.class.getClassLoader().getResourceAsStream("updateItemMetadataImport.csv");
        testCsvString = IOUtils.toString(testcsv, StandardCharsets.UTF_8);
        testCsvString = StringUtils.replace(testCsvString, "{collectionhandleplaceholder}", collection.getHandle());
        testCsvString = StringUtils.replace(testCsvString, "{itemidplaceholder}", String.valueOf(importedItem.getID()));

        file = new File("testing");

        FileUtils.copyInputStreamToFile(IOUtils.toInputStream(testCsvString, StandardCharsets.UTF_8), file);

        MetadataImport.main(new String[] {"-f", file.getAbsolutePath(), "-e", "admin@email.com", "-s"});

        items = (List<Item>) IteratorUtils.toList(itemService.findAll(context));
        Assert.assertTrue(items.size() == 1);
        importedItem = items.get(0);
        nullAuthorityList = itemService.getMetadata(importedItem, "dc", "contributor", "author", Item.ANY, null);
        firstOrcidList = itemService
            .getMetadata(importedItem, "dc", "contributor", "author", Item.ANY, "0000-0000-0000-0000");
        secondOrcidList = itemService
            .getMetadata(importedItem, "dc", "contributor", "author", Item.ANY, "1111-1111-1111-1111");
        List<MetadataValue> thirdOrcidList = itemService
            .getMetadata(importedItem, "dc", "contributor", "author", Item.ANY, "2222-2222-2222-2222");

        Assert.assertTrue(nullAuthorityList.size() == 0);
        Assert.assertTrue(firstOrcidList.size() == 0);
        Assert.assertTrue(secondOrcidList.size() == 0);
        Assert.assertTrue(thirdOrcidList.size() == 1);
        Assert.assertTrue(StringUtils.equals(thirdOrcidList.get(0).getValue(), null));
        Assert.assertTrue(StringUtils.equals(thirdOrcidList.get(0).getAuthority(), "2222-2222-2222-2222"));

    }
}
