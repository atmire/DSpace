package org.dspace.content;

import org.apache.logging.log4j.Logger;
import org.dspace.AbstractUnitTest;
import org.dspace.authorize.AuthorizeException;
import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.EntityTypeDAO;
import org.dspace.content.dao.RelationshipDAO;
import org.dspace.content.dao.RelationshipTypeDAO;
import org.dspace.content.factory.ContentServiceFactory;
import org.dspace.content.service.*;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by: Andrew Wood
 * Date: 13 May 2019
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityServiceImplTest  {

    @InjectMocks
    private EntityServiceImpl entityService;

    @Mock
    private Context context;

    @Mock
    private ItemService itemService;

    @Mock
    private AuthorizeService authorizeService;

    @Mock
    private RelationshipService relationshipService;

    @Mock
    private RelationshipTypeService relationshipTypeService;

    @Mock
    private EntityTypeService entityTypeService;

    @Mock
    private EntityType leftType;

    @Mock
    private EntityType rightType;

    @Test
    public void testfindByItemId() throws Exception {
        //TODO Test works but does not pass
        Item item = mock(Item.class);
        List<Relationship> relationshipList = new ArrayList<>();
        Entity entity = new Entity(item, relationshipList);
        assertEquals("TestFindByItem 0", entity, entityService.findByItemId(context, item.getID()));
    }

    @Test
    public void testGetType() throws Exception {
        Entity entity = mock(Entity.class);
        EntityTypeService entityTypeService = mock(EntityTypeService.class);
        Item item = mock(Item.class);
        List<MetadataValue> list = new ArrayList<>();
        MetadataValue metadataValue = mock(MetadataValue.class);
        list.add(metadataValue);
        EntityType entityType = entityTypeService.findByEntityType(context, "testType");
        when(metadataValue.getValue()).thenReturn("testType");
        when(itemService.getMetadata(item, "relationship", "type", null, Item.ANY)).thenReturn(list);
        assertEquals("TestGetType 0", entityType, entityService.getType(context, entity));
    }

    @Test
    public void testGetLeftRelation() throws Exception {
        Item item = mock(Item.class);
        UUID uuid = UUID.randomUUID();
        Relationship relationship = mock(Relationship.class);
        List<Relationship> relationshipList = new ArrayList<>();
        relationshipList.add(relationship);
        Entity entity = mock(Entity.class);
        when(entity.getItem()).thenReturn(item);
        when(relationship.getLeftItem()).thenReturn(item);
        when(item.getID()).thenReturn(uuid);
        when(entity.getRelationships()).thenReturn(relationshipList);
        assertEquals("TestGetLeftRelations 0", relationshipList, entityService.getLeftRelations(context, entity));
    }

    @Test
    public void testGetRightRelation() throws Exception {
        Item item = mock(Item.class);
        UUID uuid = UUID.randomUUID();
        Relationship relationship = mock(Relationship.class);
        List<Relationship> relationshipList = new ArrayList<>();
        relationshipList.add(relationship);
        Entity entity = mock(Entity.class);
        when(entity.getItem()).thenReturn(item);
        when(relationship.getRightItem()).thenReturn(item);
        when(item.getID()).thenReturn(uuid);
        when(entity.getRelationships()).thenReturn(relationshipList);
        assertEquals("TestGetLeftRelations 0", relationshipList, entityService.getRightRelations(context, entity));
    }

    @Test
    public void testGetRelationsByLabel() throws Exception {
        Relationship relationship = mock(Relationship.class);
        RelationshipType relationshipType = mock(RelationshipType.class);
        relationship.setRelationshipType(relationshipType);
        List<Relationship> relationshipList = new ArrayList<>();
        relationshipList.add(relationship);
        when(relationshipService.findAll(context)).thenReturn(relationshipList);
        when(relationship.getRelationshipType()).thenReturn(relationshipType);
        when(relationshipType.getLeftLabel()).thenReturn("leftLabel");
        when(relationshipType.getRightLabel()).thenReturn("rightLabel");
        assertEquals("TestGetRelationsByLabel 0", relationshipList, entityService.getRelationsByLabel(context, "leftLabel"));
    }

    @Test
    public void testGetAllRelationshipTypes() throws Exception {
        List<MetadataValue> list = new ArrayList<>();
        MetadataValue metadataValue = mock(MetadataValue.class);
        list.add(metadataValue);
        Item item = mock(Item.class);
        RelationshipTypeDAO relationshipTypeDAO = mock(RelationshipTypeDAO.class);
        Entity entity = mock(Entity.class);
        Relationship relationship = mock(Relationship.class);
        RelationshipType relationshipType = mock(RelationshipType.class);
        relationship.setRelationshipType(relationshipType);
        relationshipType.setLeftType(leftType);
        relationshipType.setLeftType(rightType);
        List<RelationshipType> relationshipTypeList = new ArrayList<>();
        relationshipTypeList.add(relationshipType);
        when(metadataValue.getValue()).thenReturn("testType");
        when(entity.getItem()).thenReturn(item);
        when(itemService.getMetadata(item, "relationship", "type", null, Item.ANY)).thenReturn(list);
        when(relationshipTypeDAO.findAll(context, RelationshipType.class)).thenReturn(relationshipTypeList);
        when(relationshipTypeService.findAll(context)).thenReturn(relationshipTypeList);
        when(relationshipType.getLeftType()).thenReturn(leftType);
        when(relationshipType.getRightType()).thenReturn(rightType);
        when(entityTypeService.findByEntityType(context, "value")).thenReturn(leftType);
        when(leftType.getID()).thenReturn(0);
        when(rightType.getID()).thenReturn(1);
        when(entityService.getType(context, entity)).thenReturn(leftType);
        assertEquals("TestGetAllRelationshipTypes 0", relationshipTypeList, entityService.getAllRelationshipTypes(context, entity));
    }

    @Test
    public void testGetRelationshipTypesByLabel() throws Exception {
        List<RelationshipType> list = new LinkedList<>();
        RelationshipType relationshipType = mock(RelationshipType.class);
        list.add(relationshipType);
        when(relationshipTypeService.findAll(context)).thenReturn(list);
        when(relationshipType.getLeftLabel()).thenReturn("leftLabel");
        when(relationshipType.getRightLabel()).thenReturn("rightLabel");
        assertEquals("TestGetRelationshipTypesByLabel 0", list, entityService.getRelationshipTypesByLabel(context, "leftLabel"));
    }




}
