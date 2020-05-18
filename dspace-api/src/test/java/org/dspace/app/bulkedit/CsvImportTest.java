package org.dspace.app.bulkedit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.app.launcher.ScriptLauncher;
import org.dspace.app.scripts.handler.impl.TestDSpaceRunnableHandler;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.Collection;
import org.dspace.content.Community;
import org.dspace.content.EntityType;
import org.dspace.content.Item;
import org.dspace.content.MetadataValue;
import org.dspace.content.Relationship;
import org.dspace.content.RelationshipMetadataService;
import org.dspace.content.RelationshipMetadataServiceTest;
import org.dspace.content.RelationshipType;
import org.dspace.content.WorkspaceItem;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.CollectionService;
import org.dspace.content.service.CommunityService;
import org.dspace.content.service.EntityService;
import org.dspace.content.service.EntityTypeService;
import org.dspace.content.service.InstallItemService;
import org.dspace.content.service.ItemService;
import org.dspace.content.service.RelationshipService;
import org.dspace.content.service.RelationshipTypeService;
import org.dspace.content.service.WorkspaceItemService;
import org.dspace.core.I18nUtil;
import org.dspace.eperson.Group;
import org.dspace.eperson.factory.EPersonServiceFactory;
import org.dspace.eperson.service.GroupService;
import org.jdom.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CsvImportTest extends AbstractUnitTest {

    private static final Logger log = org.apache.logging.log4j.LogManager
        .getLogger(CsvImportTest.class);

    protected RelationshipMetadataService relationshipMetadataService = ContentServiceFactory
        .getInstance().getRelationshipMetadataService();
    protected RelationshipService relationshipService = ContentServiceFactory.getInstance().getRelationshipService();
    protected RelationshipTypeService relationshipTypeService = ContentServiceFactory.getInstance()
                                                                                     .getRelationshipTypeService();
    protected EntityService entityService = ContentServiceFactory.getInstance().getEntityService();
    protected EntityTypeService entityTypeService = ContentServiceFactory.getInstance().getEntityTypeService();
    protected CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
    protected CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
    protected ItemService itemService = ContentServiceFactory.getInstance().getItemService();
    protected InstallItemService installItemService = ContentServiceFactory.getInstance().getInstallItemService();
    protected WorkspaceItemService workspaceItemService = ContentServiceFactory.getInstance().getWorkspaceItemService();

    Collection col;
    Item leftItem;
    Item rightItem;
    Relationship relationship;
    RelationshipType isAuthorOfPublicationRelationshipType;

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

            GroupService groupService = EPersonServiceFactory.getInstance().getGroupService();
            Group adminGroup = groupService.findByName(context, Group.ADMIN);
            groupService.addMember(context, adminGroup, eperson);

            Community community = communityService.create(null, context);

            col = collectionService.create(context, community);
            WorkspaceItem leftIs = workspaceItemService.create(context, col, false);
            WorkspaceItem rightIs = workspaceItemService.create(context, col, false);

            leftItem = installItemService.installItem(context, leftIs);
            rightItem = installItemService.installItem(context, rightIs);
            context.restoreAuthSystemState();
        } catch (AuthorizeException ex) {
            log.error("Authorization Error in init", ex);
            fail("Authorization Error in init: " + ex.getMessage());
        } catch (SQLException ex) {
            log.error("SQL Error in init", ex);
            fail("SQL Error in init: " + ex.getMessage());
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
        context.abort();
        super.destroy();
    }

    @Test
    public void verifySubjectDeletionOnCsvImportWithEmptyColumn() throws Exception {
        itemService.addMetadata(context, leftItem, "dc", "subject", null, null, "testSubject");
        itemService.addMetadata(context, leftItem, "dc", "title", null, null, "new title");
        assertEquals("testSubject", itemService.getMetadata(leftItem, "dc", "subject", null, Item.ANY).get(0).getValue());

        String csvLineString = leftItem.getID().toString() + "," + col
            .getHandle() + "," + "new title" + ",";

        String[] csv = {"id,collection,dc.title,dc.subject", csvLineString};
        performImportScript(csv);

        List<MetadataValue> subjects = itemService.getMetadata(leftItem, "dc", "subject", null, Item.ANY);
        assertEquals(0, subjects.size());
    }

    @Test
    public void verifyRelationshipDeletionOnCsvImportWithEmptyColumn() throws Exception {
        context.turnOffAuthorisationSystem();
        itemService.addMetadata(context, leftItem, "dc", "title", null, null, "new title");
        itemService.addMetadata(context, leftItem, "relationship", "type", null, null, "Publication");
        itemService.addMetadata(context, rightItem, "relationship", "type", null, null, "Author");
        itemService.addMetadata(context, rightItem, "person", "familyName", null, null, "familyName");
        itemService.addMetadata(context, rightItem, "person", "givenName", null, null, "firstName");
        EntityType publicationEntityType = entityTypeService.create(context, "Publication");
        EntityType authorEntityType = entityTypeService.create(context, "Author");
        isAuthorOfPublicationRelationshipType = relationshipTypeService
            .create(context, publicationEntityType, authorEntityType,
                    "isAuthorOfPublication", "isPublicationOfAuthor",
                    null, null, null, null);

        relationship = relationshipService.create(context, leftItem, rightItem,
                                                  isAuthorOfPublicationRelationshipType, 0, 0);
        context.restoreAuthSystemState();


        List<Relationship> relationships = relationshipService.findByItem(context, leftItem);
        assertEquals(1, relationships.size());
        String csvLineString = leftItem.getID().toString() + "," + col
            .getHandle() + "," + "new title" + ",";

        String[] csv = {"id,collection,dc.title,relation.isAuthorOfPublication", csvLineString};
        performImportScript(csv);

        relationships = relationshipService.findByItem(context, leftItem);
        assertEquals(0, relationships.size());
    }


    private void performImportScript(String[] csv) throws Exception {
        String filename = "test.csv";
        BufferedWriter out = new BufferedWriter(
            new OutputStreamWriter(
                new FileOutputStream(filename), "UTF-8"));
        for (String csvLine : csv) {
            out.write(csvLine + "\n");
        }
        out.flush();
        out.close();
        out = null;



        MetadataImport.main(new String[] {"metadata-import", "-f", filename, "-e", eperson.getEmail(), "-s"});

        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }
}
