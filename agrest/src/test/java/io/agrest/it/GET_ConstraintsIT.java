package io.agrest.it;

import io.agrest.Ag;
import io.agrest.DataResponse;
import io.agrest.constraints.Constraint;
import io.agrest.it.fixture.JerseyAndDerbyCase;
import io.agrest.it.fixture.cayenne.E10;
import io.agrest.it.fixture.cayenne.E11;
import io.agrest.it.fixture.cayenne.E4;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public class GET_ConstraintsIT extends JerseyAndDerbyCase {

    @BeforeClass
    public static void startTestRuntime() {
        startTestRuntime(Resource.class);
    }

    @Override
    protected Class<?>[] testEntities() {
        return new Class[]{E4.class, E10.class, E11.class};
    }

    @Test
    public void testImplicit() {

        e4().insertColumns("id", "c_varchar", "c_int").values(1, "xxx", 5).exec();

        Response r = target("/e4/limit_attributes").request().get();
        onSuccess(r).bodyEquals(1, "{\"id\":1,\"cInt\":5}");
    }

    @Test
    public void testExplicit() {

        e4().insertColumns("id", "c_varchar", "c_int").values(1, "xxx", 5).exec();

        Response r = target("/e4/limit_attributes")
                .queryParam("include", E4.C_BOOLEAN.getName())
                .queryParam("include", E4.C_INT.getName())
                .request().get();

        onSuccess(r).bodyEquals(1, "{\"cInt\":5}");
    }

    @Test
    public void testAnnotated() {

        e10().insertColumns("id", "c_varchar", "c_int", "c_boolean", "c_date")
                .values(1, "xxx", 5, true, "2014-01-02").exec();

        Response r = target("/e10").request().get();
        onSuccess(r).bodyEquals(1, "{\"id\":1,\"cBoolean\":true,\"cInt\":5}");
    }

    @Test
    public void testAnnotated_Relationship() {

        e10().insertColumns("id", "c_varchar", "c_int", "c_boolean", "c_date")
                .values(1, "xxx", 5, true, "2014-01-02").exec();

        e11().insertColumns("id", "e10_id", "address", "name")
                .values(15, 1, "aaa", "nnn").exec();

        Response r = target("/e10").queryParam("include", E10.E11S.getName()).request().get();
        onSuccess(r).bodyEquals(1, "{\"id\":1,\"cBoolean\":true,\"cInt\":5,\"e11s\":[{\"address\":\"aaa\"}]}");
    }

    @Path("")
    public static class Resource {

        @Context
        private Configuration config;

        @GET
        @Path("e4/limit_attributes")
        public DataResponse<E4> getObjects_LimitAttributes(@Context UriInfo uriInfo) {
            return Ag.select(E4.class, config).uri(uriInfo)
                    .constraint(Constraint.idOnly(E4.class).attributes(E4.C_INT))
                    .get();
        }

        @GET
        @Path("e10")
        public DataResponse<E10> get(@Context UriInfo uriInfo) {
            return Ag.select(E10.class, config).uri(uriInfo).get();
        }
    }
}
