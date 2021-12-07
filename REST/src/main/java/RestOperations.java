import Entities.Person;
import Entities.Currency;
import Entities.Manager;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.ws.rs.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


@Stateless
@Path("/RestOperations")
@Produces(MediaType.APPLICATION_JSON)
public class RestOperations {
    @PersistenceContext(name = "school")
    EntityManager em;

    @POST
    @Path("/addClients")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response AddClient(HashMap<String, Object> clientsProp) {
        Person p = null;
        try {
            System.out.println("Add Person: " + clientsProp.get("name").toString());
            p = new Person(clientsProp.get("name").toString());
            em.persist(p);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).build();
//            return false;
        }
        return Response.status(Status.OK).entity(p).build();
//        return true;
    }

    @POST
    @Path("/addManager")
    public Response AddManager(Manager m) {
        try {
            em.persist(m);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).build();
        }
        return Response.status(Status.OK).entity(m).build();
    }

    @POST
    @Path("/addCurrency")
    public Response AddCurrency(HashMap<String, Object> currencyProp) {
        System.out.println("Name: " + currencyProp.get("name").toString() + "\tExchangeRate: " + currencyProp.get("exchangeRate").toString());
        Currency c = null;
        try {
            c = new Currency(currencyProp.get("name").toString(), Float.parseFloat(currencyProp.get("exchangeRate").toString()));
            em.persist(c);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).build();
        }
        return Response.status(Status.OK).entity(c).build();
    }

    @GET
    @Path("/listClients")
    public List<Person> ListClients(){
        try
        {
            TypedQuery<Person> clients = em.createQuery("FROM Person c", Person.class);
            return clients.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GET
    @Path("/listManagers")
    public Map<Integer, String> ListManagers(){
        Map<Integer, String> allManagersInfo = new HashMap<>();
        try
        {
            TypedQuery<Manager> managers = em.createQuery("FROM Manager mn", Manager.class);
            List<Manager> allManagers = managers.getResultList();
            for (Manager m : allManagers) {
                System.out.println("\n\n\n\nID: " + m.getId() + "\n\n\n\n" + m.getName());
                allManagersInfo.put(m.getId(), m.getName());
            }
            System.out.println(allManagersInfo);
            return allManagersInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }

    @GET
    @Path("/listCurrencies")
    public List<Currency> ListCurrencies(){
        try
        {
            TypedQuery<Currency> currencies = em.createQuery("FROM Currency crr", Currency.class);
            return currencies.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
