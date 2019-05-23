package org.dspace.content;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.RelationshipDAO;
import org.dspace.content.dao.RelationshipTypeDAO;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.*;
import org.dspace.content.virtual.VirtualMetadataPopulator;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.eperson.EPerson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import javax.management.relation.RelationService;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by: Andrew Wood
 * Date: 20 May 2019
 */
@RunWith(MockitoJUnitRunner.class)
public class RelationshipServiceImplTest {

    @InjectMocks
    private RelationshipServiceImpl relationshipService;

    @Mock
    private RelationshipDAO relationshipDAO;

    @Mock
    private Context context;

    @Mock
    private Relationship relationship;

    @Mock
    private List<Relationship> relationshipsList;

    @Mock
    private AuthorizeService authorizeService;

    @Mock
    private ItemService itemService;

    @Mock
    private VirtualMetadataPopulator virtualMetadataPopulator;

    @Before
    public void init() {
        relationshipsList = new ArrayList<>();
        relationshipsList.add(relationship);
    }

    @Test
    public void testFindAll() throws Exception {
        when(relationshipDAO.findAll(context, Relationship.class)).thenReturn(relationshipsList);
        List<Relationship> result = relationshipService.findAll(context);
        assertEquals("TestFindAll 0", relationshipsList, result);
    }

    @Test
    public void testFindByItem() throws Exception {
        List<Relationship> relationshipTest = new ArrayList<>();
        Item cindy = mock(Item.class);
        Item fred = mock(Item.class);
        Item bob = mock(Item.class);
        Item hank = mock(Item.class);
        Item jasper = mock(Item.class);
        Item spot = mock(Item.class);
        RelationshipType hasDog = new RelationshipType();
        RelationshipType hasFather = new RelationshipType();
        RelationshipType hasMother = new RelationshipType();
        hasDog.setLeftLabel("hasDog");
        hasDog.setRightLabel("isDogOf");
        hasFather.setLeftLabel("hasFather");
        hasFather.setRightLabel("isFatherOf");
        hasMother.setLeftLabel("hasMother");
        hasMother.setRightLabel("isMotherOf");

        relationshipTest.add(getRelationship(cindy, spot, hasDog,0,0));
        relationshipTest.add(getRelationship(cindy, jasper, hasDog,0,1));
        relationshipTest.add(getRelationship(cindy, hank, hasFather,0,0));
        relationshipTest.add(getRelationship(fred, cindy, hasMother,0,0));
        relationshipTest.add(getRelationship(bob, cindy, hasMother,1,0));
        when(relationshipService.findByItem(context, cindy)).thenReturn(relationshipTest);
        when(relationshipDAO.findByItem(context, cindy)).thenReturn(relationshipTest);


        List<Relationship> results = relationshipService.findByItem(context, cindy);
        assertEquals("TestFindByItem 0", relationshipTest.size(), results.size());
    }

    @Test
    public void testCreate() throws Exception {
        Relationship relationship = relationshipDAO.create(context,new Relationship());
        relationshipService = mock(relationshipService.getClass());
        context.turnOffAuthorisationSystem();

        when(authorizeService.isAdmin(context)).thenReturn(true);
        assertEquals("TestCreate 0", relationship, relationshipService.create(context));

        MetadataValue metVal = mock(MetadataValue.class);
        List<MetadataValue> metsList = new ArrayList<>();
        List<Relationship> leftTypelist = new ArrayList<>();
        List<Relationship> rightTypelist = new ArrayList<>();
        Item leftItem = mock(Item.class);
        Item rightItem = mock(Item.class);
        RelationshipType testRel = new RelationshipType();
        EntityType leftEntityType = mock(EntityType.class);
        EntityType rightEntityType = mock(EntityType.class);
        testRel.setLeftType(leftEntityType);
        testRel.setRightType(rightEntityType);
        testRel.setLeftLabel("Entitylabel");
        testRel.setRightLabel("Entitylabel");
        metsList.add(metVal);
        relationship = getRelationship(leftItem, rightItem, testRel, 0,0);
        leftTypelist.add(relationship);
        rightTypelist.add(relationship);
        when(virtualMetadataPopulator.isUseForPlaceTrueForRelationshipType(relationship.getRelationshipType(), true)).thenReturn(true);
        when(authorizeService.authorizeActionBoolean(context, relationship.getLeftItem(), Constants.WRITE)).thenReturn(true);
        when(authorizeService.authorizeActionBoolean(context, relationship.getRightItem(), Constants.WRITE)).thenReturn(true);
        when(relationshipService.findByItem(context,leftItem)).thenReturn(leftTypelist);
        when(relationshipService.findByItem(context,rightItem)).thenReturn(rightTypelist);
        when(leftEntityType.getLabel()).thenReturn("Entitylabel");
        when(rightEntityType.getLabel()).thenReturn("Entitylabel");
        when(metVal.getValue()).thenReturn("Entitylabel");
        when(metVal.getValue()).thenReturn("Entitylabel");
        when(metsList.get(0).getValue()).thenReturn("Entitylabel");
        when(relationshipService.findByItemAndRelationshipType(context, leftItem, testRel, true)).thenReturn(leftTypelist);
        when(itemService.getMetadata(leftItem, "relationship", "type", null, Item.ANY)).thenReturn(metsList);
        when(itemService.getMetadata(rightItem, "relationship", "type", null, Item.ANY)).thenReturn(metsList);
        when(relationshipService.create(context, relationship)).thenReturn(relationship);
        when(relationshipService.create(context, leftItem, rightItem, testRel,0,0)).thenReturn(relationship);
        when(relationshipDAO.create(context, relationship)).thenReturn(relationship);


        assertEquals("TestCreate 1", relationship, relationshipService.create(context, leftItem, rightItem, testRel,0,0));

        context.restoreAuthSystemState();
    }

    @Test
    public void findLeftPlaceByLeftItem(){

    }

    private Relationship getRelationship(Item leftItem, Item rightItem, RelationshipType relationshipType, int leftPlace, int rightPlace){
        Relationship relationship = new Relationship();
        relationship.setLeftItem(leftItem);
        relationship.setRightItem(rightItem);
        relationship.setRelationshipType(relationshipType);
        relationship.setLeftPlace(leftPlace);
        relationship.setRightPlace(rightPlace);

        return relationship;
    }


}
