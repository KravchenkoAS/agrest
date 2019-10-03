package io.agrest.it;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.SelectStage;
import io.agrest.encoder.DataResponseEncoder;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.GenericEncoder;
import io.agrest.encoder.ListEncoder;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E27Nopk;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.query.ObjectSelect;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;

public class GET_StagesIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E27Nopk.class};
    }

    @Test
    public void testNoId() {
        e27NoPk().insertColumns("name")
                .values("z")
                .values("a").exec();

        Response response = target("/e27").request().get();
        onSuccess(response).bodyEquals(2,
                "{\"name\":\"a\"},{\"name\":\"z\"}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e27")
        public DataResponse<?> get(@Context UriInfo uriInfo) {

            // since Cayenne won't be able to fetch objects with no id, our only option is ColumnSelect and a custom encoder
            return Ag.select(E27Nopk.class, config)
                    .uri(uriInfo)
                    .terminalStage(SelectStage.APPLY_SERVER_PARAMS, this::fetchAll)
                    .get();
        }

        private void fetchAll(SelectContext<E27Nopk> context) {

            List names = ObjectSelect.columnQuery(E27Nopk.class, E27Nopk.NAME)
                    .orderBy(E27Nopk.NAME.asc())
                    .select(AgRuntime.service(ICayennePersister.class, config).sharedContext());

            context.getEntity().setResult(names);

            Encoder rowEncoder = new NoIdEncoder();
            ListEncoder listEncoder = new ListEncoder(rowEncoder);
            Encoder encoder = new DataResponseEncoder("data", listEncoder, "total", GenericEncoder.encoder());
            context.setEncoder(encoder);
        }
    }

    static class NoIdEncoder implements Encoder {

        @Override
        public boolean willEncode(String propertyName, Object object) {
            return true;
        }

        @Override
        public boolean encode(String propertyName, Object object, JsonGenerator out) throws IOException {

            out.writeStartObject();
            out.writeStringField("name", object == null ? null : object.toString());
            out.writeEndObject();

            return true;
        }

    }
}

