package org.dspace.app.rest.model;

import org.dspace.app.rest.RootRestResourceController;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by raf on 26/09/2017.
 */
public class RootRestTest {
    RootRest rootRest;

    @Before
    public void setUp() throws Exception{
        rootRest = new RootRest();
    }

    @Test
    public void testConstructorDoesNotReturnNull() throws Exception{
        assertNotNull(rootRest);
    }

    @Test
    public void testGetDspaceNameBeforeSetterReturnsNull() throws Exception{
        assertNull(rootRest.getDspaceName());
    }

    @Test
    public void testGetDspaceUrlBeforeSetterReturnsNull() throws Exception{
        assertNull(rootRest.getDspaceURL());
    }
    @Test
    public void testGetTypeReturnsCorrectValue() throws Exception{
        assertEquals(RootRest.NAME, rootRest.getType());
    }

    @Test
    public void testGetCategoryReturnsCorrectValue() throws Exception{
        assertEquals(RootRest.CATEGORY, rootRest.getCategory());
    }

    @Test
    public void testGetControllerReturnsCorrectValue() throws Exception{
        assertEquals(RootRestResourceController.class, rootRest.getController());
    }

    @Test
    public void testSetDspaceNameSetsCorrectValue() throws Exception{
        String dspaceName = "dspacename";
        rootRest.setDspaceName(dspaceName);
        assertEquals(dspaceName, rootRest.getDspaceName());
    }

    @Test
    public void testSetDspaceNameNullSetsNull() throws Exception{
        rootRest.setDspaceName(null);
        assertNull(rootRest.getDspaceName());
    }

    @Test
    public void testSetDspaceUrlSetsCorrectValue() throws Exception{
        String dspaceUrl = "dspaceurl";
        rootRest.setDspaceURL(dspaceUrl);
        assertEquals(dspaceUrl, rootRest.getDspaceURL());
    }

    @Test
    public void testSetDspaceUrlNullSetsNull() throws Exception{
        rootRest.setDspaceURL(null);
        assertNull(rootRest.getDspaceURL());
    }
}
