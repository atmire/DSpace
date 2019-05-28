package org.dspace.content.virtual;

import org.dspace.content.*;
import org.dspace.content.service.EntityService;
import org.dspace.content.service.RelationshipService;
import org.dspace.core.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by: Andrew Wood
 * Date: 28 May 2019
 */
@RunWith(MockitoJUnitRunner.class)
public class RelatedTest {

    @InjectMocks
    private Related related;

    @Mock
    private RelationshipService relationshipService;

    @Mock
    private EntityService entityService;

    @Mock
    private VirtualMetadataConfiguration virtualMetadataConfiguration;

    @Mock
    private Context context;


    @Test
    public void testGetRelationshipTypeString() {
        related.setRelationshipTypeString("TestType");
        assertEquals("TestGetRelationshipTypeString 0", "TestType", related.getRelationshipTypeString());
    }

    @Test
    public void testSetRelationshipTypeString() {
        related.setRelationshipTypeString("TestType");
        assertEquals("TestSetRelationshipTypeString 0", "TestType", related.getRelationshipTypeString());
    }

    @Test
    public void testSetPlace() {
        related.setPlace(0);
        assertTrue("TestSetPlace 0", 0 == related.getPlace());
    }

    @Test
    public void testGetPlace() {
        related.setPlace(0);
        assertTrue("TestGetPlace 0", 0 == related.getPlace());
    }

    @Test
    public void testGetVirtualMetadataConfiguration() {
        assertEquals("TestGetVirtualMetadataConfiguration 0", virtualMetadataConfiguration.getClass(), related.getVirtualMetadataConfiguration().getClass());
    }

    @Test
    public void testSetVirtualMetadataConfiguration() {
        related.setVirtualMetadataConfiguration(virtualMetadataConfiguration);
        assertEquals("TestGetVirtualMetadataConfiguration 0", virtualMetadataConfiguration, related.getVirtualMetadataConfiguration());
    }

    @Test
    public void testSetUseForPlace() {
        related.setUseForPlace(true);
        assertEquals("TestSetVirtualMetadataConfiguration 0", true, related.getUseForPlace());
    }

    @Test
    public void testGetUseForPlace() {
        related.setUseForPlace(true);
        assertEquals("TestSetVirtualMetadataConfiguration 0", true, related.getUseForPlace());
    }

    @Test
    public void testGetValues() throws Exception {
        List<RelationshipType> relationshipTypeList = new ArrayList<>();
        List<Relationship> relationshipList = new ArrayList<>();
        Relationship relationship = mock(Relationship.class);
        Item item = mock(Item.class);
        Entity entity = mock(Entity.class);
        EntityType entityType = mock(EntityType.class);
        RelationshipType relationshipType = mock(RelationshipType.class);
        related.setRelationshipTypeString("LeftLabel");
        relationshipTypeList.add(relationshipType);
        relationshipList.add(relationship);
        related.setPlace(0);
        when(item.getID()).thenReturn(UUID.randomUUID());
        when(relationshipType.getLeftLabel()).thenReturn("LeftLabel");
        when(relationshipType.getRightLabel()).thenReturn("RightLabel");
        when(relationshipType.getLeftType()).thenReturn(entityType);
        when(relationshipType.getRightType()).thenReturn(entityType);
        when(entityService.getAllRelationshipTypes(context, entity)).thenReturn(relationshipTypeList);
        when(entityService.findByItemId(context, item.getID())).thenReturn(entity);
        when(entityService.getType(context, entity)).thenReturn(entityType);
        when(relationshipService.findByItemAndRelationshipType(context, item, relationshipType)).thenReturn(relationshipList);
        when(relationship.getRelationshipType()).thenReturn(relationshipType);
        when(relationship.getLeftPlace()).thenReturn(0);
        when(relationship.getRightPlace()).thenReturn(1);
        when(relationship.getRightItem()).thenReturn(item);
        when(relationship.getLeftItem()).thenReturn(item);

        assertEquals("TestGetValues 0", virtualMetadataConfiguration.getValues(context, item), related.getValues(context, item));
        related.setPlace(1);
        assertEquals("TestGetValues 1", virtualMetadataConfiguration.getValues(context, item), related.getValues(context, item));
        related.setPlace(2);
        assertEquals("TestGetValues 2", new LinkedList<>(), related.getValues(context, item));
    }


}
