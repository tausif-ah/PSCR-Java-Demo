package nist.p_70nanb17h188.demo.pscr19.server;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import nist.p_70nanb17h188.demo.pscr19.Device;
import nist.p_70nanb17h188.demo.pscr19.logic.app.messaging.MessagingNamespace;
import nist.p_70nanb17h188.demo.pscr19.logic.link.LinkLayer;
import nist.p_70nanb17h188.demo.pscr19.logic.log.Log;
import nist.p_70nanb17h188.demo.pscr19.logic.net.NetLayer;

/**
 * REST Web Service
 *
 * @author jchen
 */
@Path("init")
public class InitResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of InitResource
     */
    public InitResource() {
    }

    /**
     * @param name Name of the current device.
     *
     * @return an instance of java.lang.String
     */
    @Path("init")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public synchronized String getText(@QueryParam("name") String name) {
        if (Device.getName() == null) {
            Log.init(1000);
            Device.setName(name);
            LinkLayer.init();
            NetLayer.init();
            MessagingNamespace.init();
            Log.d("InitResource", "initialized, name=%s", Device.getName());
        }
        return Device.getName();
    }

}
