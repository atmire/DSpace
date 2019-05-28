package org.dspace.content.virtual;

import org.dspace.content.Item;
import org.dspace.core.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by: Andrew Wood
 * Date: 28 May 2019
 */
@RunWith(MockitoJUnitRunner.class)
public class UUIDValueTest {

    @InjectMocks
    private UUIDValue UUIDValue;

    @Mock
    private Context context;

    @Test
    public void testGetValues() throws Exception {
        List<String> list = new LinkedList<>();
        Item item = mock(Item.class);
        UUID uuid = UUID.randomUUID();
        when(item.getID()).thenReturn(uuid);
        list.add(String.valueOf(uuid));
        assertEquals("TestGetValues 0", list, UUIDValue.getValues(context, item));
    }

    @Test
    public void testSetUseForPlace() {
        UUIDValue.setUseForPlace(true);
        assertEquals("TestSetUseForPlace 0", true, UUIDValue.getUseForPlace());

    }

    @Test
    public void testGetUseForPlace() {
        UUIDValue.setUseForPlace(true);
        assertEquals("TestGetUseForPlace 0", true, UUIDValue.getUseForPlace());
    }
}
