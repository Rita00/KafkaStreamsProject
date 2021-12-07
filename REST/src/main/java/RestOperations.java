import Entities.Person;
import Entities.Currency;
import Entities.Manager;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
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
            System.out.println("Manager ID: " + clientsProp.get("managerID").toString());
            p = new Person(clientsProp.get("name").toString());

            int managerID = Integer.parseInt(clientsProp.get("managerID").toString());
            System.out.println("Int ManagerID: " + managerID);
            Manager m = em.find(Manager.class, managerID);
            p.setManager(m);

            em.persist(p);

            return Response.status(Status.OK).entity("Cliente inserido com sucesso!").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).build();
//            return false;
        }
//        return true;
    }

    @POST
    @Path("/addManager")
    public Response AddManager(String managerName) {
        try {
            Manager m = new Manager(managerName);
            em.persist(m);
            return Response.status(Status.OK).entity("Manager inserido com sucesso!").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).build();
        }
    }

    @POST
    @Path("/addCurrency")
    public Response AddCurrency(HashMap<String, Object> currencyProp) {
        System.out.println("Name: " + currencyProp.get("name").toString() + "\tExchangeRate: " + currencyProp.get("exchangeRate").toString());
        Currency c = null;
        try {
            c = new Currency(currencyProp.get("name").toString(), Float.parseFloat(currencyProp.get("exchangeRate").toString()));
            em.persist(c);
            return Response.status(Status.OK).entity("Moeda inserida com sucesso!").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/listClients")
    public Map<Integer, String> ListClients() {
        Map<Integer, String> allClientsInfo = new HashMap<>();
        try {
            TypedQuery<Person> clients = em.createQuery("FROM Person c", Person.class);

            List<Person> allClients = clients.getResultList();
            for (Person p : allClients) {
                allClientsInfo.put(p.getId(), p.getName());
            }
            return allClientsInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GET
    @Path("/listManagers")
    public Map<Integer, String> ListManagers() {
        Map<Integer, String> allManagersInfo = new HashMap<>();
        try {
            TypedQuery<Manager> managers = em.createQuery("FROM Manager mn", Manager.class);
            List<Manager> allManagers = managers.getResultList();
            for (Manager m : allManagers) {
                allManagersInfo.put(m.getId(), m.getName());
            }
            System.out.println(allManagersInfo);
            return allManagersInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GET
    @Path("/listCurrencies")
    public Map<String, Double> ListCurrencies() {
        Map<String, Double> allCurrenciesInfo = new HashMap<>();

        try {
            TypedQuery<Currency> currencies = em.createQuery("FROM Currency crr", Currency.class);

            List<Currency> allCurrencies = currencies.getResultList();
            for (Currency c : allCurrencies) {
                allCurrenciesInfo.put(c.getName(), c.getExchangeRate());
            }
            return allCurrenciesInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
