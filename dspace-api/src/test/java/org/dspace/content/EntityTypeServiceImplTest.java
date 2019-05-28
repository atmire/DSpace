package org.dspace.content;

import org.apache.logging.log4j.Logger;

import org.dspace.authorize.service.AuthorizeService;
import org.dspace.content.dao.EntityTypeDAO;
import org.dspace.core.Context;
import org.dspace.core.GenericDAO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by: Andrew Wood
 * Date: 17 May 2019
 */
@RunWith(MockitoJUnitRunner.class)
public class EntityTypeServiceImplTest   {

    @InjectMocks
    private EntityTypeServiceImpl entityTypeService;


    @Mock
    private EntityType entityType;

    @Mock
    private EntityTypeDAO entityTypeDAO;

    @Mock
    private Context context;

    @Mock
    private GenericDAO genericDAO;

    @Mock
    private AuthorizeService authorizeService;

    /**
     * log4j category
     */
    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(EntityTypeServiceImplTest.class);

    @Test
    public void testFindByEntityType() throws Exception {
        when(entityTypeDAO.findByEntityType(context, "TestType")).thenReturn(entityType);
        assertEquals("TestFindByEntityType 0", entityType, entityTypeService.findByEntityType(context, "TestType"));
    }


    @Test
    public void testFindAll() throws Exception {
        List<EntityType> entityTypeList = new ArrayList<>();
        when(entityTypeDAO.findAll(context, EntityType.class)).thenReturn(entityTypeList);
        assertEquals("TestFindAll 0", entityTypeList, entityTypeService.findAll(context));
    }


    @Test
    public void testCreate() throws Exception {
        //TODO Fix null return on assert
        when(authorizeService.isAdmin(context)).thenReturn(true);
        EntityType entityType = new EntityType();
        entityType.setLabel("Test");
        when(entityTypeDAO.create(context, entityType)).thenReturn(entityType);
        assertEquals("TestCreate 0", entityType, entityTypeService.create(context, "Test"));
    }

    @Test
    public void testFind() throws Exception {
        when(entityTypeDAO.findByID(context, EntityType.class, 0)).thenReturn(entityType);
        assertEquals("TestFind 0", entityType, entityTypeService.find(context, 0));
    }

    @Test
    public void testUpdate() throws Exception {
        List<EntityType> entityTypeList = new ArrayList<>();
        entityTypeList.add(entityType);
        when(authorizeService.isAdmin(context)).thenReturn(true);
        entityTypeService.update(context, entityTypeList);
        Mockito.verify(entityTypeDAO, times(1)).save(context, entityType);
    }

    @Test
    public void testDelete() throws Exception {
        when(authorizeService.isAdmin(context)).thenReturn(true);
        entityTypeService.delete(context, entityType);
        Mockito.verify(entityTypeDAO, times(1)).delete(context, entityType);
    }


}
