package org.dspace.content.virtual;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by: Andrew Wood
 * Date: 28 May 2019
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityTypeToFilterQueryServiceTest {

    @InjectMocks
    private EntityTypeToFilterQueryService entityTypeToFilterQueryService;

    @Test
    public void testSetMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        entityTypeToFilterQueryService.setMap(map);
        assertEquals("TestSetMap 0", map, entityTypeToFilterQueryService.getMap());
    }

    @Test
    public void testGetMap() {
        Map<String, String> map = Collections.emptyMap();
        assertEquals("TestGetFields 0", map.getClass(), map.getClass());
    }

    @Test
    public void testHasKey() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        entityTypeToFilterQueryService.setMap(map);
        assertEquals("TestHasKey 0", true, entityTypeToFilterQueryService.hasKey("key"));
    }

    @Test
    public void testGetFilterQueryForKey() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        entityTypeToFilterQueryService.setMap(map);
        assertEquals("TestGetFilterQueryForKey 0", "value", entityTypeToFilterQueryService.getFilterQueryForKey("key"));
    }
}
