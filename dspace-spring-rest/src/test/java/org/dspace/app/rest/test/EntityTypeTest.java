package org.dspace.app.rest.test;

import org.apache.logging.log4j.Logger;
import org.dspace.app.rest.builder.EntityTypeBuilder;
import org.dspace.content.*;
import org.dspace.content.service.EntityTypeService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by: Andrew Wood
 * Date: 17 May 2019
 */
public class EntityTypeTest extends AbstractControllerIntegrationTest {


    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(EntityTypeTest.class);

    /**
     * Item instance for the tests
     */
    private EntityType entityType;

    @Autowired
    private EntityTypeService entityTypeService;

    /**
     * This method will be run before every test as per @Before. It will
     * initialize resources required for the tests.
     *
     * Other methods can be annotated with @Before here or in subclasses
     * but no execution order is guaranteed
     */
    @Before
    @Override
    public void setUp() {
        try {
            super.setUp();

            if (entityTypeService.findAll(context).size() > 0) {
                //Don't initialize the setup more than once
                return;
            }

            context.turnOffAuthorisationSystem();
            //we have to create a new community in the database
            context.turnOffAuthorisationSystem();
            this.entityType = EntityTypeBuilder.createEntityTypeBuilder(context, "Test").build();

            //we need to commit the changes so we don't block the table for testing
            context.restoreAuthSystemState();
        }
        catch (Exception e){
            log.error("Error", e);
        }

    }

    /**
     * The standard setter for the ID of this EntityType
     */
    @Test
    public void testSetId() {
       int oldID = entityType.getID();
       int newID = oldID + 99999999;
       entityType.setId(newID);
       assertTrue("testSetId 0", entityType.getID() != oldID);
    }

    /**
     * The standard getter for the label of this EntityType
     */
    @Test
    public void testGetLabel() {
        assertTrue("testGetLabel 0", entityType.getLabel() != null);
    }

    /**
     * The standard setter for the label of this EntityType
     */
    @Test
    public void testSetLabel() {
        String oldLabel = entityType.getLabel();
        String newLabel = entityType.getLabel() + "TEST";
        entityType.setLabel(newLabel);
        assertFalse("testSetId 0", entityType.getLabel().equalsIgnoreCase(oldLabel));
    }

    /**
     * The standard getter for the ID of this EntityType
     */
    @Test
    public void testGetID() {
        assertTrue("testGetID 0", entityType.getID() != null);
    }

}
