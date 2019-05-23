package org.dspace.content;

import org.apache.logging.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.*;
import org.dspace.core.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Created by: Andrew Wood
 * Date: 13 May 2019
 */
public class EntityTest extends AbstractUnitTest {

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(EntityTest.class);

    /**
     * Item instance for the tests
     */
    private Entity entity;
    private Item it;

    private CommunityService communityService = ContentServiceFactory.getInstance().getCommunityService();
    private CollectionService collectionService = ContentServiceFactory.getInstance().getCollectionService();
    private WorkspaceItemService workspaceItemService = ContentServiceFactory.getInstance().getWorkspaceItemService();
    private InstallItemService installItemService = ContentServiceFactory.getInstance().getInstallItemService();
    private ItemService itemService = ContentServiceFactory.getInstance().getItemService();


    private Collection collection;
    private Community owningCommunity;

    @Autowired(required = true)
    protected RelationshipService relationshipService;


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
            //we have to create a new community in the database
            context.turnOffAuthorisationSystem();
            relationshipService = ContentServiceFactory.getInstance().getRelationshipService();
            this.owningCommunity = communityService.create(null, context);
            this.collection = collectionService.create(context, owningCommunity);
            WorkspaceItem workspaceItem = workspaceItemService.create(context, collection, false);
            this.it = installItemService.installItem(context, workspaceItem);

            it.setSubmitter(context.getCurrentUser());
            itemService.update(context, it);
            List<Relationship> relationshipList = relationshipService.findByItem(context, it);
            this.entity = new Entity(it, relationshipList);

            //we need to commit the changes so we don't block the table for testing
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
     * Standard getter for the Item for this Entity object
     * @return  The Item that is described in this Entity object
     */
    @Test
    public void testGetItem() {
        assertThat("testGetItem 0", entity.getItem(), notNullValue());
        assertTrue("testGetItem 1", entity.getItem().getID() != null);

    }

    /**
     * Standard setter for the Item for this Entity object
     */
    @Test
    public void testSetItem() {
        try{
            String oldId = entity.getItem().id.toString();
            WorkspaceItem workspaceItem = workspaceItemService.create(context, collection, false);
            Item newItem = installItemService.installItem(context, workspaceItem);

            newItem.setSubmitter(context.getCurrentUser());
            itemService.update(context, newItem);
            entity.setItem(newItem);
            assertFalse("testGetItem 1", entity.getItem().getID().toString().equalsIgnoreCase(oldId));
        }
        catch (SQLException sqle){
            log.error("SQL error from testSetItem", sqle);
        }
        catch (AuthorizeException ae){
            log.error("Auth error from testSetItem", ae);
        }
    }

    /**
     * Standard getter for the list of relationships for the Item in this Entity object
     * @return  the list of relationships
     */
    @Test
    public void testGetRelationships() {
        assertThat("testGetRelationships 0", entity.getRelationships(), notNullValue());
    }

    /**
     * Standard setter for the list of relationships for the Item in this Entity object
     */
    public void testSetRelationships() {
        List<Relationship> oldRelationships = entity.getRelationships();
        int oldRelationshipsSize = entity.getRelationships().size();
        Relationship relationship = new Relationship();
        oldRelationships.add(relationship);
        entity.setRelationships(oldRelationships);
        assertTrue("testSetRelationships 1", entity.getRelationships().size() != oldRelationshipsSize);
    }

    @After
    @Override
    public void destroy() {
        super.destroy();
    }
}
