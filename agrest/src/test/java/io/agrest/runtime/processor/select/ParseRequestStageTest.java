package io.agrest.runtime.processor.select;

import io.agrest.AgException;
import io.agrest.it.fixture.cayenne.E1;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.protocol.Dir;
import io.agrest.protocol.Sort;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JacksonService;
import io.agrest.runtime.protocol.CayenneExpParser;
import io.agrest.runtime.protocol.ExcludeParser;
import io.agrest.runtime.protocol.ICayenneExpParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ISizeParser;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.protocol.IncludeParser;
import io.agrest.runtime.protocol.SizeParser;
import io.agrest.runtime.protocol.SortParser;
import io.agrest.runtime.request.DefaultRequestBuilderFactory;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParseRequestStageTest extends TestWithCayenneMapping {

    private ParseRequestStage parseStage;

    @Before
    public void setUp() {

        IJacksonService jacksonService = new JacksonService();

        // prepare parse request stage
        ICayenneExpParser expParser = new CayenneExpParser(jacksonService);
        ISortParser sortParser = new SortParser(jacksonService);
        ISizeParser sizeParser = new SizeParser();
        IIncludeParser includeParser = new IncludeParser(jacksonService, expParser, sortParser, sizeParser);
        IExcludeParser excludeParser = new ExcludeParser(jacksonService);

        IAgRequestBuilderFactory requestBuilderFactory
                = new DefaultRequestBuilderFactory(expParser, sortParser, includeParser, excludeParser);
        this.parseStage = new ParseRequestStage(requestBuilderFactory);
    }

    @Test
    public void testSelectRequest_Default() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNull(context.getMergedRequest().getCayenneExp());
        assertTrue(context.getMergedRequest().getOrderings().isEmpty());
        assertNull(context.getMergedRequest().getMapBy());
        assertNull(context.getMergedRequest().getLimit());
        assertNull(context.getMergedRequest().getStart());
        assertTrue(context.getMergedRequest().getIncludes().isEmpty());
        assertTrue(context.getMergedRequest().getExcludes().isEmpty());
    }

    @Test
    public void testSelectRequest_IncludeAttrs() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("include")).thenReturn(Arrays.asList("description", "age"));

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);

        assertNotNull(context.getMergedRequest());

        assertEquals(2, context.getMergedRequest().getIncludes().size());
        assertEquals("description", context.getMergedRequest().getIncludes().get(0).getPath());
        assertEquals("age", context.getMergedRequest().getIncludes().get(1).getPath());
    }

    @Test
    public void testSelectRequest_IncludeAttrs_AsArray() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("include")).thenReturn(Arrays.asList("[\"description\", \"age\"]"));

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);

        assertNotNull(context.getMergedRequest());

        assertEquals(2, context.getMergedRequest().getIncludes().size());
        assertEquals("description", context.getMergedRequest().getIncludes().get(0).getPath());
        assertEquals("age", context.getMergedRequest().getIncludes().get(1).getPath());
    }

    @Test
    public void testSelectRequest_ExcludeAttrs() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("exclude")).thenReturn(Arrays.asList("description", "age"));

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);

        assertNotNull(context.getMergedRequest());

        assertEquals(2, context.getMergedRequest().getExcludes().size());
        assertEquals("description", context.getMergedRequest().getExcludes().get(0).getPath());
        assertEquals("age", context.getMergedRequest().getExcludes().get(1).getPath());
    }

    @Test
    public void testSelectRequest_ExcludeAttrs_AsArray() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("exclude")).thenReturn(Arrays.asList("[\"description\", \"age\"]"));

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);

        assertNotNull(context.getMergedRequest());

        assertEquals(2, context.getMergedRequest().getExcludes().size());
        assertEquals("description", context.getMergedRequest().getExcludes().get(0).getPath());
        assertEquals("age", context.getMergedRequest().getExcludes().get(1).getPath());
    }

    @Test
    public void testSelectRequest_IncludeExcludeAttrs() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("include")).thenReturn(Arrays.asList("description", "age", "id"));
        when(params.get("exclude")).thenReturn(Arrays.asList("description", "name"));

        SelectContext<E1> context = prepareContext(params, E1.class);

        parseStage.execute(context);

        assertNotNull(context.getMergedRequest());

        assertEquals(3, context.getMergedRequest().getIncludes().size());
        assertEquals("description", context.getMergedRequest().getIncludes().get(0).getPath());
        assertEquals("age", context.getMergedRequest().getIncludes().get(1).getPath());
        assertEquals("id", context.getMergedRequest().getIncludes().get(2).getPath());

        assertEquals(2, context.getMergedRequest().getExcludes().size());
        assertEquals("description", context.getMergedRequest().getExcludes().get(0).getPath());
        assertEquals("name", context.getMergedRequest().getExcludes().get(1).getPath());
    }

    @Test
    public void testSelectRequest_IncludeRels() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("include")).thenReturn(Arrays.asList("e3s"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.execute(context);

        assertNotNull(context.getMergedRequest());

        assertEquals(1, context.getMergedRequest().getIncludes().size());
        assertEquals("e3s", context.getMergedRequest().getIncludes().get(0).getPath());
    }


    @Test
    public void testSelectRequest_SortSimple_NoDir() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("sort")).thenReturn(Collections.singletonList("e2"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNotNull(context.getMergedRequest().getOrderings());

        Sort ordering = context.getMergedRequest().getOrderings().get(0);
        assertEquals("e2", ordering.getProperty());
    }

    @Test
    public void testSelectRequest_SortSimple_ASC() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("sort")).thenReturn(Collections.singletonList("e2"));
        when(params.get("dir")).thenReturn(Collections.singletonList("ASC"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNotNull(context.getMergedRequest().getOrderings());

        assertEquals(1, context.getMergedRequest().getOrderings().size());
        Sort ordering = context.getMergedRequest().getOrderings().get(0);
        assertEquals("e2", ordering.getProperty());
        assertEquals(Dir.ASC, ordering.getDirection());
    }

    @Test
    public void testSelectRequest_SortSimple_DESC() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("sort")).thenReturn(Collections.singletonList("e2"));
        when(params.get("dir")).thenReturn(Collections.singletonList("DESC"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNotNull(context.getMergedRequest().getOrderings());

        Sort ordering = context.getMergedRequest().getOrderings().get(0);
        assertEquals("e2", ordering.getProperty());
        assertEquals(Dir.DESC, ordering.getDirection());
    }

    @Test(expected = AgException.class)
    public void testSelectRequest_SortSimple_Garbage() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("sort")).thenReturn(Collections.singletonList("s1"));
        when(params.get("dir")).thenReturn(Collections.singletonList("XYZ"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.execute(context);
    }

    @Test
    public void testSelectRequest_Sort() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("sort")).thenReturn(
                Collections.singletonList("[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"address\",\"direction\":\"ASC\"}]"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNotNull(context.getMergedRequest().getOrderings());
        assertEquals(2, context.getMergedRequest().getOrderings().size());

        Sort o1 = context.getMergedRequest().getOrderings().get(0);
        Sort o2 = context.getMergedRequest().getOrderings().get(1);

        assertEquals("name", o1.getProperty());
        assertEquals(Dir.DESC, o1.getDirection());
        assertEquals("address", o2.getProperty());
        assertEquals(Dir.ASC, o2.getDirection());
    }

    @Test
    public void testSelectRequest_Sort_Dupes() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("sort")).thenReturn(
                Collections.singletonList("[{\"property\":\"name\",\"direction\":\"DESC\"},{\"property\":\"name\",\"direction\":\"ASC\"}]"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNotNull(context.getMergedRequest().getOrderings());
        assertEquals(2, context.getMergedRequest().getOrderings().size());

        Sort o1 = context.getMergedRequest().getOrderings().get(0);
        Sort o2 = context.getMergedRequest().getOrderings().get(1);

        assertEquals("name", o1.getProperty());
        assertEquals(Dir.DESC, o1.getDirection());
        assertEquals("name", o2.getProperty());
        assertEquals(Dir.ASC, o2.getDirection());
    }

    @Test(expected = AgException.class)
    public void testSelectRequest_Sort_BadSpec() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("sort")).thenReturn(
                Collections.singletonList("[{\"property\":\"p1\",\"direction\":\"DESC\"},{\"property\":\"p2\",\"direction\":\"XXX\"}]"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.execute(context);
    }

    @Test(expected = AgException.class)
    public void testSelectRequest_CayenneExp_BadSpec() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("cayenneExp"))
                .thenReturn(Collections.singletonList("{exp : \"numericProp = 12345 and stringProp = 'John Smith' and booleanProp = true\"}"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.execute(context);
    }

    @Test
    public void testSelectRequest_CayenneExp() {

        @SuppressWarnings("unchecked")
        MultivaluedMap<String, String> params = mock(MultivaluedMap.class);
        when(params.get("cayenneExp")).thenReturn(Collections.singletonList("{\"exp\" : \"name = 'John Smith'\"}"));

        SelectContext<E2> context = prepareContext(params, E2.class);

        parseStage.execute(context);

        assertNotNull(context.getMergedRequest());
        assertNotNull(context.getMergedRequest().getCayenneExp());

        assertEquals("name = 'John Smith'", context.getMergedRequest().getCayenneExp().getExp());
    }
}
