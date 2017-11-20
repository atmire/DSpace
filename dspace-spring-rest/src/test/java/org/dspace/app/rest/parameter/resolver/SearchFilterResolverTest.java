/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.parameter.resolver;

import org.dspace.app.rest.parameter.SearchFilter;
import org.dspace.content.Bitstream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchFilterResolverTest {

    @InjectMocks
    SearchFilterResolver searchFilterResolver;

    @Mock
    MethodParameter methodParameter;

    @Mock
    ParameterizedType parameterizedType;

    @Mock
    ModelAndViewContainer modelAndViewContainer;

    @Mock
    NativeWebRequest nativeWebRequest;

    @Mock
    WebDataBinderFactory webDataBinderFactory;


    @Test
    public void supportsParameterTestWithSearchFilterClassParameterType() throws Exception{
        Class searchFilterClass = SearchFilter.class;
        when(methodParameter.getParameterType()).thenReturn(searchFilterClass);

        assertTrue(searchFilterResolver.supportsParameter(methodParameter));
    }

    @Test
    public void supportsParameterTestWithNullParameterTypeAndFalseIsSearchFilterList() throws Exception{
        Class notSearchFilterClass = this.getClass();
        when(methodParameter.getParameterType()).thenReturn(notSearchFilterClass);

        assertFalse(searchFilterResolver.supportsParameter(methodParameter));
    }

    @Test
    public void supportsParameterTestIsSearchListTrueAndNullParameterType() throws Exception{

        Class notSearchFilterClass = this.getClass();
        when(methodParameter.getParameterType()).thenReturn(notSearchFilterClass);

        assertFalse(searchFilterResolver.supportsParameter(methodParameter));


        Class listClass = List.class;
        Class searchFilterClass = SearchFilter.class;
        Type[] types = new Type[1];
        types[0] = searchFilterClass;
        when(methodParameter.getParameterType()).thenReturn(listClass);
        when(methodParameter.getGenericParameterType()).thenReturn(parameterizedType);
        when(parameterizedType.getActualTypeArguments()).thenReturn(types);

        assertTrue(searchFilterResolver.supportsParameter(methodParameter));


    }

    @Test
    public void supportsParameterTestIsSearchListTrueAndSearchFilterClassParameterType() throws Exception{
        Class searchFilterClass = SearchFilter.class;
        when(methodParameter.getParameterType()).thenReturn(searchFilterClass);

        assertTrue(searchFilterResolver.supportsParameter(methodParameter));

        Class listClass = List.class;
        Type[] types = new Type[1];
        types[0] = searchFilterClass;
        when(methodParameter.getParameterType()).thenReturn(listClass);
        when(methodParameter.getGenericParameterType()).thenReturn(parameterizedType);
        when(parameterizedType.getActualTypeArguments()).thenReturn(types);

        assertTrue(searchFilterResolver.supportsParameter(methodParameter));
    }

    @Test
    public void supportsParameterTestIsSearchListFalseWrongListClassAndNullParameterType() throws Exception{
        Class notSearchFilterClass = this.getClass();
        when(methodParameter.getParameterType()).thenReturn(notSearchFilterClass);

        assertFalse(searchFilterResolver.supportsParameter(methodParameter));



        Class wrongClass = HashMap.class;
        Class searchFilterClass = SearchFilter.class;
        Type[] types = new Type[1];
        types[0] = searchFilterClass;
        when(methodParameter.getParameterType()).thenReturn(wrongClass);
        when(methodParameter.getGenericParameterType()).thenReturn(parameterizedType);
        when(parameterizedType.getActualTypeArguments()).thenReturn(types);

        assertFalse(searchFilterResolver.supportsParameter(methodParameter));
    }

    @Test
    public void supportsParameterTestIsSearchListFalseWrongTypeInGenericTypesAndNullParameterType() throws Exception{
        Class notSearchFilterClass = this.getClass();
        when(methodParameter.getParameterType()).thenReturn(notSearchFilterClass);

        assertFalse(searchFilterResolver.supportsParameter(methodParameter));



        Class listClass = List.class;
        Class wrongClass = HashMap.class;
        Type[] types = new Type[1];
        types[0] = wrongClass;
        when(methodParameter.getParameterType()).thenReturn(listClass);
        when(methodParameter.getGenericParameterType()).thenReturn(parameterizedType);
        when(parameterizedType.getActualTypeArguments()).thenReturn(types);

        assertFalse(searchFilterResolver.supportsParameter(methodParameter));
    }

    @Test
    public void supportsParameterTestIsSearchListFalseGenericTypesNullAndNullParameterType() throws Exception{
        Class notSearchFilterClass = this.getClass();
        when(methodParameter.getParameterType()).thenReturn(notSearchFilterClass);

        assertFalse(searchFilterResolver.supportsParameter(methodParameter));



        Class listClass = List.class;
        Class searchFilterClass = SearchFilter.class;
        Type[] types = new Type[1];
        types[0] = searchFilterClass;
        when(methodParameter.getParameterType()).thenReturn(listClass);
        when(methodParameter.getGenericParameterType()).thenReturn(null);
        when(parameterizedType.getActualTypeArguments()).thenReturn(types);

        assertFalse(searchFilterResolver.supportsParameter(methodParameter));
    }

    @Test
    public void supportsParameterTestIsSearchListFalseGenericTypesNullAndWrongListClassAndNullParameterType() throws Exception{
        Class notSearchFilterClass = this.getClass();
        when(methodParameter.getParameterType()).thenReturn(notSearchFilterClass);

        assertFalse(searchFilterResolver.supportsParameter(methodParameter));



        Class wrongClass = HashMap.class;
        Class searchFilterClass = SearchFilter.class;
        Type[] types = new Type[1];
        types[0] = searchFilterClass;
        when(methodParameter.getParameterType()).thenReturn(wrongClass);
        when(methodParameter.getGenericParameterType()).thenReturn(null);
        when(parameterizedType.getActualTypeArguments()).thenReturn(types);

        assertFalse(searchFilterResolver.supportsParameter(methodParameter));
    }

    @Test
    public void supportsParameterTestIsSearchListFalseWrongListClassAndWrongGenericTypeAndNullParameterType() throws Exception{
        Class notSearchFilterClass = this.getClass();
        when(methodParameter.getParameterType()).thenReturn(notSearchFilterClass);

        assertFalse(searchFilterResolver.supportsParameter(methodParameter));



        Class wrongClass = HashMap.class;
        Class anotherWrongClass = Bitstream.class;
        Type[] types = new Type[1];
        types[0] = anotherWrongClass;
        when(methodParameter.getParameterType()).thenReturn(wrongClass);
        when(methodParameter.getGenericParameterType()).thenReturn(parameterizedType);
        when(parameterizedType.getActualTypeArguments()).thenReturn(types);

        assertFalse(searchFilterResolver.supportsParameter(methodParameter));
    }



    @Test
    public void resolveArgumentsTest() throws Exception{

        LinkedList<String> linkedList = new LinkedList<String>();
        linkedList.add("f.title");
        Iterator<String> parameterNames = linkedList.iterator();
        when(nativeWebRequest.getParameterNames()).thenReturn(parameterNames);
        when(nativeWebRequest.getParameterValues(anyString())).thenReturn(new String[]{"test,equals"});
        Class searchFilterClass = SearchFilter.class;
        when(methodParameter.getParameterType()).thenReturn(searchFilterClass);

        SearchFilter searchFilter = (SearchFilter) searchFilterResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);

        assertEquals(searchFilter.getName(), "title");
        assertEquals(searchFilter.getOperator(), "equals");
        assertEquals(searchFilter.getValue(), "test");
    }

    @Test
    public void resolveArgumentsTestNoSearchFilter() throws Exception{

        LinkedList<String> linkedList = new LinkedList<String>();
        Iterator<String> parameterNames = linkedList.iterator();
        when(nativeWebRequest.getParameterNames()).thenReturn(parameterNames);
        when(nativeWebRequest.getParameterValues(anyString())).thenReturn(new String[]{"test,equals"});
        Class searchFilterClass = SearchFilter.class;
        when(methodParameter.getParameterType()).thenReturn(searchFilterClass);

        SearchFilter searchFilter = (SearchFilter) searchFilterResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);

        assertNull(searchFilter);
    }

    @Test
    public void resolveArgumentsTestParameterTypeDifferentFromSearchFilter() throws Exception{

        LinkedList<String> linkedList = new LinkedList<String>();
        linkedList.add("f.title");
        Iterator<String> parameterNames = linkedList.iterator();
        when(nativeWebRequest.getParameterNames()).thenReturn(parameterNames);
        when(nativeWebRequest.getParameterValues(anyString())).thenReturn(new String[]{"test,equals"});
        Class differentClass = this.getClass();
        when(methodParameter.getParameterType()).thenReturn(differentClass);

        List<?> list = (List<?>) searchFilterResolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);

        assertFalse(list.isEmpty());
    }
}
