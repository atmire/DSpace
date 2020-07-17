/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.bulkedit;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dspace.AbstractIntegrationTest;
import org.dspace.app.launcher.ScriptLauncher;
import org.dspace.app.scripts.handler.impl.TestDSpaceRunnableHandler;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.EntityType;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.Relationship;
import org.dspace.content.RelationshipType;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.EntityTypeService;
import org.dspace.content.service.InstallItemService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.RelationshipService;
import org.dspace.content.service.RelationshipTypeService;
import org.dspace.content.service.WorkspaceItemService;
import org.junit.Before;
import org.junit.Test;

public class MetadataImportTest extends AbstractIntegrationTest {

    private ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    private CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
    private CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
    private WorkspaceItemService workspaceItemService = ContentServiceFactory.getInstance().getWorkspaceItemService();
    private InstallItemService installItemService = ContentServiceFactory.getInstance().getInstallItemService();
    private EntityTypeService entityTypeService = ContentServiceFactory.getInstance().getEntityTypeService();
    private RelationshipTypeService relationshipTypeService = ContentServiceFactory.getInstance()
                                                                                   .getRelationshipTypeService();
    private RelationshipService relationshipService = ContentServiceFactory.getInstance().getRelationshipService();

    Collection collection;


    @Before
    public void setup() throws SQLException, AuthorizeException {
        context.turnOffAuthorisationSystem();
        Community community = communityService.create(null, context);
        collection = collectionService.create(context, community);
        context.restoreAuthSystemState();
    }
    @Test
    public void metadataImportTest() throws Exception {
        String fileLocation = new File(testProps.get("test.importcsv").toString()).getAbsolutePath();
        String[] args = new String[] {"metadata-import", "-f", fileLocation, "-e", eperson.getEmail(), "-s"};
        TestDSpaceRunnableHandler testDSpaceRunnableHandler = new TestDSpaceRunnableHandler();

        ScriptLauncher.handleScript(args, ScriptLauncher.getConfig(kernelImpl), testDSpaceRunnableHandler, kernelImpl);
        Item importedItem = itemService.findAll(context).next();
        List<MetadataValue> metadata = itemService.getMetadata(importedItem, "dc", "contributor", "author", Item.ANY);
        assertTrue(StringUtils.equals(metadata.get(0).getValue(), "Donald, SmithImported"));
        context.turnOffAuthorisationSystem();
        itemService.delete(context, itemService.find(context, importedItem.getID()));
        context.commit();
        context.restoreAuthSystemState();

    }

    @Test
    public void personMetadataImportTest() throws Exception {

        String fileLocation = new File(testProps.get("test.importpersoncsv").toString()).getAbsolutePath();
        String[] args = new String[] {"metadata-import", "-f", fileLocation, "-e", eperson.getEmail(), "-s"};
        TestDSpaceRunnableHandler testDSpaceRunnableHandler = new TestDSpaceRunnableHandler();

        ScriptLauncher.handleScript(args, ScriptLauncher.getConfig(kernelImpl), testDSpaceRunnableHandler, kernelImpl);
        Item importedItem = itemService.findAll(context).next();
        assertTrue(
            StringUtils.equals(
                itemService.getMetadata(importedItem, "person", "birthDate", null, Item.ANY)
                           .get(0).getValue(), "2000"));
        context.turnOffAuthorisationSystem();
        itemService.delete(context, itemService.find(context, importedItem.getID()));
        context.commit();
        context.restoreAuthSystemState();
    }

    @Test
    public void relationshipMetadataImportTest() throws Exception {

        String fileLocation = new File(testProps.get("test.importrelationshipcsv").toString()).getAbsolutePath();

        context.turnOffAuthorisationSystem();
        WorkspaceItem workspaceItem = workspaceItemService.create(context, collection, false);
        Item item = installItemService.installItem(context, workspaceItem);
        item.setSubmitter(context.getCurrentUser());
        itemService.addMetadata(context, item, "relationship", "type", null, null, "Publication");
        itemService.update(context, item);

        workspaceItem = workspaceItemService.create(context, collection, false);
        Item item1 = installItemService.installItem(context, workspaceItem);
        item1.setSubmitter(context.getCurrentUser());
        itemService.addMetadata(context, item1, "relationship", "type", null, null, "Publication");
        itemService.update(context, item1);

        EntityType publication = entityTypeService.create(context, "Publication");
        EntityType person = entityTypeService.create(context, "Person");
        RelationshipType relationshipType = relationshipTypeService.create(context, publication, person,
    "isAuthorOfPublication", "isPublicationOfAuthor", 0, 10, 0, 10);
        context.restoreAuthSystemState();

        List<String> list = Files.readAllLines(Paths.get(fileLocation));
        String lastLine = list.get(list.size() - 1);
        list.remove(list.size() - 1);
        lastLine = lastLine + "\"" + item1.getID() + "\"";
//        lastLine = "\"" + item.getID() + "\"" + lastLine + "\"" + item1.getID() + "\"";
        list.add(lastLine);
        String testFileLocation = testProps.get("test.importrelationshipusedintestcsv").toString();
        Files.write(Paths.get(testFileLocation), list);
        String[] args = new String[] {"metadata-import", "-f", testFileLocation, "-e", eperson.getEmail(), "-v"};
        TestDSpaceRunnableHandler testDSpaceRunnableHandler = new TestDSpaceRunnableHandler();

        ScriptLauncher.handleScript(args, ScriptLauncher.getConfig(kernelImpl), testDSpaceRunnableHandler, kernelImpl);
        context.turnOffAuthorisationSystem();
        relationshipTypeService.delete(context, relationshipTypeService.find(context, relationshipType.getID()));
        entityTypeService.delete(context, entityTypeService.find(context, person.getID()));
        entityTypeService.delete(context, entityTypeService.find(context, publication.getID()));
        itemService.delete(context, itemService.find(context, item.getID()));
        itemService.delete(context, itemService.find(context, item1.getID()));
        Files.delete(Paths.get(testFileLocation));
        context.commit();
        context.restoreAuthSystemState();
    }

    @Test
    public void relationshipMetadataImporAlreadyExistingItemTest() throws Exception {

        String fileLocation = new File(testProps.get("test.importrelationshipexistingcsv").toString())
            .getAbsolutePath();

        context.turnOffAuthorisationSystem();


        WorkspaceItem workspaceItem = workspaceItemService.create(context, collection, false);
        Item item = installItemService.installItem(context, workspaceItem);
        item.setSubmitter(context.getCurrentUser());
        itemService.addMetadata(context, item, "relationship", "type", null, null, "Person");
        itemService.update(context, item);
        List<Relationship> relationshipList = relationshipService.findByItem(context, item);
        assertEquals(0, relationshipList.size());

        workspaceItem = workspaceItemService.create(context, collection, false);
        Item item1 = installItemService.installItem(context, workspaceItem);
        item1.setSubmitter(context.getCurrentUser());
        itemService.addMetadata(context, item1, "relationship", "type", null, null, "Publication");
        itemService.update(context, item1);

        EntityType publication = entityTypeService.create(context, "Publication");
        EntityType person = entityTypeService.create(context, "Person");
        RelationshipType relationshipType = relationshipTypeService.create(context, publication, person,
                                                                           "isAuthorOfPublication",
                                                                           "isPublicationOfAuthor",
                                                                           0, 10, 0, 10);
        context.restoreAuthSystemState();

        List<String> list = Files.readAllLines(Paths.get(fileLocation));
        String lastLine = list.get(list.size() - 1);
        list.remove(list.size() - 1);
//        lastLine = lastLine + "\"" + item1.getID() + "\"";
        lastLine = "\"" + item.getID() + "\"" + lastLine + "\"" + item1.getID() + "\"";
        list.add(lastLine);
        String testFileLocation = testProps.get("test.importrelationshipusedintestcsv").toString();
        Files.write(Paths.get(testFileLocation), list);
        String[] args = new String[] {"metadata-import", "-f", testFileLocation, "-e", eperson.getEmail(), "-s"};
        TestDSpaceRunnableHandler testDSpaceRunnableHandler = new TestDSpaceRunnableHandler();

        ScriptLauncher.handleScript(args, ScriptLauncher.getConfig(kernelImpl), testDSpaceRunnableHandler, kernelImpl);
        context.turnOffAuthorisationSystem();
        item = itemService.find(context, item.getID());
        relationshipList = relationshipService.findByItem(context, item);
        assertEquals(1, relationshipList.size());
        relationshipService.delete(context, relationshipList.get(0));
        relationshipTypeService.delete(context, relationshipTypeService.find(context, relationshipType.getID()));
        entityTypeService.delete(context, entityTypeService.find(context, person.getID()));
        entityTypeService.delete(context, entityTypeService.find(context, publication.getID()));
        itemService.delete(context, itemService.find(context, item.getID()));
        itemService.delete(context, itemService.find(context, item1.getID()));
        Files.delete(Paths.get(testFileLocation));
        context.commit();
        context.restoreAuthSystemState();
    }
}
