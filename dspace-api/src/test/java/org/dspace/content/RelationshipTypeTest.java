/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.EntityTypeService;
import org.dspace.content.service.RelationshipTypeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RelationshipTypeTest extends AbstractUnitTest {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(RelationshipTypeTest.class);

    private RelationshipTypeService relationshipTypeService = ContentServiceFactory.getInstance()
                                                                                   .getRelationshipTypeService();
    private EntityTypeService entityTypeService = ContentServiceFactory.getInstance().getEntityTypeService();

    private RelationshipType firstRelationshipType;
    private RelationshipType secondRelationshipType;

    private EntityType authorEntityType;
    private EntityType publicationEntityType;
    private EntityType projectEntityType;

    @Before
    @Override
    public void init() {
        super.init();
        try {
            //we have to create a new community in the database
            context.turnOffAuthorisationSystem();

            authorEntityType = entityTypeService.create(context, "person");
            publicationEntityType = entityTypeService.create(context, "publication");
            projectEntityType = entityTypeService.create(context, "project");

            firstRelationshipType = relationshipTypeService.create(context, publicationEntityType, authorEntityType,
                                                                   "isAuthorOfPublication",
                                                                   "isPublicationOfAuthor",
                                                                   0, null,
                                                                   0, null);
            secondRelationshipType = relationshipTypeService.create(context, authorEntityType, projectEntityType,
                                                                    "isProjectOfPerson",
                                                                    "isPersonOfProject",
                                                                    0, null,
                                                                    0, null);
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

    @After
    @Override
    public void destroy() {
        try {
            context.turnOffAuthorisationSystem();
            if (firstRelationshipType != null) {
                relationshipTypeService.delete(context, relationshipTypeService.find(context,
                                                                                     firstRelationshipType.getID()));
            }
            if (secondRelationshipType != null) {
                relationshipTypeService.delete(context, relationshipTypeService.find(context,
                                                                                     secondRelationshipType.getID()));
            }
            if (authorEntityType != null) {
                entityTypeService.delete(context, authorEntityType);
            }
            if (publicationEntityType != null) {
                entityTypeService.delete(context, publicationEntityType);
            }
            if (projectEntityType != null) {
                entityTypeService.delete(context, projectEntityType);
            }
            context.restoreAuthSystemState();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (AuthorizeException e) {
            e.printStackTrace();
        }
        super.destroy();
    }

    @Test
    public void testRelationshipTypeFind() throws Exception {
        Integer firstRelationshipTypeId = firstRelationshipType.getID();
        RelationshipType found = relationshipTypeService.find(context, firstRelationshipTypeId);
        checkRelationshipTypeValues(found, firstRelationshipType);
    }

    @Test
    public void testRelationshipTypeFindByTypesAndLabels() throws Exception {
        RelationshipType found = relationshipTypeService.findbyTypesAndLabels(context, publicationEntityType,
                                                                              authorEntityType,
                                                                              "isAuthorOfPublication",
                                                                              "isPublicationOfAuthor");
        checkRelationshipTypeValues(found, firstRelationshipType);
    }

    @Test
    public void testRelationshipTypeFindAll() throws Exception {
        List<RelationshipType> foundRelationshipTypes = relationshipTypeService.findAll(context);
        assertThat(foundRelationshipTypes, notNullValue());
        assertThat(foundRelationshipTypes.size(), equalTo(2));
    }

    @Test
    public void testRelationshipTypeFindByLeftOrRightLabel() throws Exception {
        List<RelationshipType> found = relationshipTypeService.findByLeftOrRightLabel(context,
                                                                                      "isAuthorOfPublication");
        assertThat(found, notNullValue());
        assertThat(found.size(), equalTo(1));
        checkRelationshipTypeValues(found.get(0), firstRelationshipType);
    }

    @Test
    public void testRelationshipTypefindByEntityType() throws Exception {
        List<RelationshipType> found = relationshipTypeService.findByEntityType(context, projectEntityType);
        assertThat(found, notNullValue());
        assertThat(found.size(), equalTo(1));
        checkRelationshipTypeValues(found.get(0), secondRelationshipType);
    }

    private void checkRelationshipTypeValues(RelationshipType found, RelationshipType original) {
        assertThat(found, notNullValue());
        assertThat(found.getLeftLabel(), equalTo(original.getLeftLabel()));
        assertThat(found.getRightLabel(), equalTo(original.getRightLabel()));
        assertThat(found.getLeftType(), equalTo(original.getLeftType()));
        assertThat(found.getRightType(), equalTo(original.getRightType()));
        assertThat(found.getLeftMinCardinality(), equalTo(original.getLeftMinCardinality()));
        assertThat(found.getLeftMaxCardinality(), equalTo(original.getLeftMaxCardinality()));
        assertThat(found.getRightMinCardinality(), equalTo(original.getRightMinCardinality()));
        assertThat(found.getRightMaxCardinality(), equalTo(original.getRightMaxCardinality()));
    }
}
