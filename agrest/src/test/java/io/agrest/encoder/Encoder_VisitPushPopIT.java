package io.agrest.encoder;

import io.agrest.DataResponse;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E5;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.Persistent;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Encoder_VisitPushPopIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        JerseyAndDerbyCase.startTestRuntime();
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E2.class, E3.class, E5.class};
    }

    private String responseContents(DataResponse<?> response, PushPopVisitor visitor) {
        response.getEncoder().visitEntities(response.getObjects(), visitor);
        return visitor.ids.stream().collect(joining(";"));
    }

    @Test
    public void testVisit_Tree() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        e5().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(7, "zzz", 2, 1)
                .values(8, "yyy", 1, 1)
                .values(9, "zzz", 1, 2).exec();

        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("include", "{\"path\":\"e3s\",\"sort\":\"id\"}");
        params.putSingle("include", "e3s.e5");

        UriInfo mockUri = mock(UriInfo.class);
        when(mockUri.getQueryParameters()).thenReturn(params);

        DataResponse<E2> response = ag().select(E2.class).uri(mockUri).get();

        PushPopVisitor visitor = new PushPopVisitor();

        assertEquals("E3:8;E3:9;E3:7", responseContents(response, visitor));
    }

    @Test
    public void testVisit_Tree_MapBy() {

        e2().insertColumns("id_", "name")
                .values(1, "xxx")
                .values(2, "yyy")
                .values(3, "zzz").exec();

        e5().insertColumns("id", "name")
                .values(1, "xxx")
                .values(2, "yyy").exec();

        e3().insertColumns("id_", "name", "e2_id", "e5_id")
                .values(7, "zzz", 2, 1)
                .values(8, "yyy", 1, 1)
                .values(9, "zzz", 1, 2).exec();

        MultivaluedHashMap<String, String> params = new MultivaluedHashMap<>();
        params.putSingle("include", "{\"path\":\"e3s\",\"sort\":\"id\"}");
        params.putSingle("include", "e3s.e5");
        params.putSingle("mapBy", "name");

        UriInfo mockUri = mock(UriInfo.class);
        when(mockUri.getQueryParameters()).thenReturn(params);

        DataResponse<E2> response = ag().select(E2.class).uri(mockUri).get();

        PushPopVisitor visitor = new PushPopVisitor();

        assertEquals("E3:8;E3:9;E3:7", responseContents(response, visitor));
    }

    class PushPopVisitor implements EncoderVisitor {

        String processPath = "e3s";
        Deque<String> stack = new ArrayDeque<>();
        List<String> ids = new ArrayList<>();
        boolean terminal;

        @Override
        public int visit(Object object) {

            if (terminal) {
                Persistent p = (Persistent) object;
                ids.add(p.getObjectId().getEntityName() + ":" + Cayenne.intPKForObject(p));
                return Encoder.VISIT_SKIP_CHILDREN;
            }

            return Encoder.VISIT_CONTINUE;
        }

        @Override
        public void push(String relationship) {
            stack.push(relationship);
            terminal = String.join(".", stack).equals(processPath);
        }

        @Override
        public void pop() {
            stack.pop();
            terminal = String.join(".", stack).equals(processPath);
        }
    }
}
