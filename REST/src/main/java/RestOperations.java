import Entities.Client;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/RestOperations")
@Produces(MediaType.APPLICATION_JSON)
public class RestOperations {
    EntityManager em;

    @GET
    @Path("/addClients")
    public String AddClient(String name) {
        Client c = new Client(name);
        em.persist(c);
        return "Success";
    }
}
