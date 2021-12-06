import Entities.Client;
import Entities.Currency;
import Entities.Manager;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


@Stateless
@Path("/RestOperations")
@Produces(MediaType.APPLICATION_JSON)
public class RestOperations {
    @PersistenceContext(name = "school")
    EntityManager em;

    @GET
    @Path("/addClients")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response AddClient(String name) {
        Client c = null;
        try{
            c = new Client(name);
            em.persist(c);
        }catch (Exception e){
            e.printStackTrace();
            return Response.status(500).build();
        }
        return Response.status(Status.OK).entity(c).build();
    }


    @GET
    @Path("/addManager")
    public boolean AddManager(String name) {
        try{
            Manager mn = new Manager(name);
            em.persist(mn);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @GET
    @Path("/addCurrency")
    public boolean AddCurrency(String name, Float exchangeRate) {
        System.out.println("Name: " + name + "\tExchangeRate: " + exchangeRate);
        try{
            Currency c = new Currency(name, exchangeRate);
            em.persist(c);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<Client> ListClients(){
        try
        {
            TypedQuery<Client> clients = em.createQuery("FROM Client c", Client.class);
            return clients.getResultList();
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public List<Manager> ListManagers(){
        try
        {
            TypedQuery<Manager> managers = em.createQuery("FROM Manager mn", Manager.class);
            return managers.getResultList();
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    public List<Currency> ListCurrencies(){
        try
        {
            TypedQuery<Currency> currencies = em.createQuery("FROM Currency crr", Currency.class);
            return currencies.getResultList();
        }
        catch(Exception e){
            e.printStackTrace();
            throw e;
        }
    }
}
