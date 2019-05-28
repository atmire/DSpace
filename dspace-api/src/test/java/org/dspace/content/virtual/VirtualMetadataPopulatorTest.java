package org.dspace.content.virtual;

import org.dspace.content.RelationshipType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by: Andrew Wood
 * Date: 28 May 2019
 */
@RunWith(MockitoJUnitRunner.class)
public class VirtualMetadataPopulatorTest {

    @InjectMocks
    private VirtualMetadataPopulator virtualMetadataPopulator;

    @Test
    public void testSetMap() {
        Map<String, HashMap<String, VirtualMetadataConfiguration>> map = new HashMap<String,  HashMap<String, VirtualMetadataConfiguration>>();
        HashMap<String, VirtualMetadataConfiguration> mapExt = new HashMap<>();
        VirtualMetadataConfiguration virtualMetadataConfiguration = mock(VirtualMetadataConfiguration.class);
        mapExt.put("hashKey", virtualMetadataConfiguration);
        map.put("key", mapExt);
        virtualMetadataPopulator.setMap(map);
        assertEquals("TestSetMap 0", map, virtualMetadataPopulator.getMap());
    }

    @Test
    public void testGetMap() {
        Map<String, HashMap<String, VirtualMetadataConfiguration>> map = new HashMap<String,  HashMap<String, VirtualMetadataConfiguration>>();
        HashMap<String, VirtualMetadataConfiguration> mapExt = new HashMap<>();
        VirtualMetadataConfiguration virtualMetadataConfiguration = mock(VirtualMetadataConfiguration.class);
        mapExt.put("hashKey", virtualMetadataConfiguration);
        map.put("key", mapExt);
        virtualMetadataPopulator.setMap(map);
        assertEquals("TestGetMap 0", map, virtualMetadataPopulator.getMap());
    }

    @Test
    public void testIsUseForPlaceTrueForRelationshipType() {
        RelationshipType relationshipType = mock(RelationshipType.class);
        Map<String, HashMap<String, VirtualMetadataConfiguration>> map = new HashMap<String,  HashMap<String, VirtualMetadataConfiguration>>();
        HashMap<String, VirtualMetadataConfiguration> mapExt = new HashMap<>();
        VirtualMetadataConfiguration virtualMetadataConfiguration = mock(VirtualMetadataConfiguration.class);
        mapExt.put("hashKey", virtualMetadataConfiguration);
        map.put("LeftLabel", mapExt);
        map.put("NotRightLabel", mapExt);
        virtualMetadataPopulator.setMap(map);
        when(virtualMetadataConfiguration.getUseForPlace()).thenReturn(true);
        when(relationshipType.getLeftLabel()).thenReturn("LeftLabel");
        when(relationshipType.getRightLabel()).thenReturn("RightLabel");
        assertEquals("TestGetFields 0", false, virtualMetadataPopulator.isUseForPlaceTrueForRelationshipType(relationshipType, false));
        assertEquals("TestGetFields 1", true, virtualMetadataPopulator.isUseForPlaceTrueForRelationshipType(relationshipType, true));
    }
}
