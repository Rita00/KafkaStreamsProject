import Entities.*;

import javax.ejb.Stateless;
import javax.persistence.*;
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
        Person newPerson;
        try {
            //Get clients currently in the database
            TypedQuery<Person> clients = em.createQuery("FROM Person c", Person.class);
            List<Person> allClients = clients.getResultList();
            for (Person p : allClients) {
                //Check if the client's name is already in the database
                if (clientsProp.get("name").toString().equals(p.getName())) {
                    //If it is can't be added, notify admin
                    return Response.status(Status.OK).entity("Client name already exists.\nPlease try again...").build();
                }
            }
            //If it's not, create new person object with clients properties (name and manager)
            newPerson = new Person(clientsProp.get("name").toString());
            //Get the manager from database using its id
            int managerID = Integer.parseInt(clientsProp.get("managerID").toString());
            Manager m = em.find(Manager.class, managerID);

            //Add new person
            newPerson.setManager(m);
            em.persist(newPerson);

            //Notify client admin everything went well
            return Response.status(Status.OK).entity("Client added successfully!").build();
        } catch (Exception e) {
            e.printStackTrace();
            //Something went wrong, notify admin
            return Response.status(500).build();
        }
    }

    @POST
    @Path("/addManager")
    public Response AddManager(String managerName) {
        try {
            //Get managers currently in the database
            TypedQuery<Manager> managers = em.createQuery("FROM Manager m", Manager.class);
            List<Manager> allManagers = managers.getResultList();
            for (Manager m : allManagers) {
                //Check if the manager's name is already in the database
                if (managerName.equals(m.getName())) {
                    //If it is can't be added, notify admin
                    return Response.status(Status.OK).entity("Manager name already exists.\nPlease try again...").build();
                }
            }

            //Otherwise, create manager object with the name
            Manager m = new Manager(managerName);
            //Added it to the database
            em.persist(m);

            //Notify admin everything went well
            return Response.status(Status.OK).entity("Manager added successfully!").build();
        } catch (Exception e) {
            e.printStackTrace();
            //Something went wrong notify the admin
            return Response.status(500).build();
        }
    }

    @POST
    @Path("/addCurrency")
    public Response AddCurrency(HashMap<String, Object> currencyProp) {
        Currency newCurrency;
        try {
            //Get clients currently in the database
            TypedQuery<Currency> currencies = em.createQuery("FROM Currency c", Currency.class);
            List<Currency> allCurrencies = currencies.getResultList();
            for (Currency c : allCurrencies) {
                //Check if the client's name is already in the database
                if (currencyProp.get("name").toString().equals(c.getName())) {
                    //If it is can't be added, notify admin
                    return Response.status(Status.OK).entity("Currency name already exists.\nPlease try again...").build();
                }
            }

            //Otherwise, create the currency object with name and exchange rate
            newCurrency = new Currency(currencyProp.get("name").toString(), Float.parseFloat(currencyProp.get("exchangeRate").toString()));
            //And add it to the database
            em.persist(newCurrency);

            //Notify admin all went well
            return Response.status(Status.OK).entity("Currency added successfully!").build();

        } catch (Exception e) {
            e.printStackTrace();
            //Something went wrong, notify admin
            return Response.status(500).build();
        }
    }

    @GET
    @Path("/listClients")
    public Map<Long, String> ListClients() {
        //Create hashmap to send information to client
        Map<Long, String> allClientsInfo = new HashMap<>();
        try {
            //Get all clients from the database
            TypedQuery<Person> clients = em.createQuery("FROM Person c", Person.class);
            List<Person> allClients = clients.getResultList();

            //If there are no clients in the database
            if (allClients.isEmpty()) {
                //Return empty
                return allClientsInfo;
            }

            for (Person p : allClients) {
                //Added client id and name to the string
                allClientsInfo.put(p.getId(), p.getName());
            }

            //Return all relevant info
            return allClientsInfo;
        } catch (Exception e) {
            e.printStackTrace();
            //Something went wrong return empty
            return allClientsInfo;
        }
    }

    @GET
    @Path("/listManagers")
    public Map<Integer, String> ListManagers() {
        //Create map to hold all relevant information
        Map<Integer, String> allManagersInfo = new HashMap<>();
        try {
            //Get all managers from database
            TypedQuery<Manager> managers = em.createQuery("FROM Manager mn", Manager.class);
            List<Manager> allManagers = managers.getResultList();

            //If there are no managers in the database
            if (allManagers.isEmpty()) {
                //Return empty
                return allManagersInfo;
            }

            for (Manager m : allManagers) {
                //Add to the map
                allManagersInfo.put(m.getId(), m.getName());
            }

            //Return all relevant info
            return allManagersInfo;
        } catch (Exception e) {
            e.printStackTrace();
            //Something went wrong, return empty
            return allManagersInfo;
        }
    }

    @GET
    @Path("/listCurrencies")
    public Map<String, Double> ListCurrencies() {
        //Create map to hold all relevant information
        Map<String, Double> allCurrenciesInfo = new HashMap<>();
        try {
            //Get all currencies from database
            TypedQuery<Currency> currencies = em.createQuery("FROM Currency crr", Currency.class);
            List<Currency> allCurrencies = currencies.getResultList();

            //If there are no currencies in the database
            if (allCurrencies.isEmpty()) {
                //Return empty
                return allCurrenciesInfo;
            }

            for (Currency c : allCurrencies) {
                //Add info to the map
                allCurrenciesInfo.put(c.getName(), c.getExchangeRate());
            }

            //Return all relevant information
            return allCurrenciesInfo;
        } catch (Exception e) {
            //Something went wrong, return empty
            e.printStackTrace();
            return allCurrenciesInfo;
        }
    }

    @GET
    @Path("/listCreditPerClient")
    public Map<Integer, Double> ListCreditPerClient() {
        //Create map to hold relevant info
        Map<Integer, Double> creditsPerClient = new HashMap<>();
        try {
            //Get all credits per client
            TypedQuery<CreditsPerClient> clients = em.createQuery("FROM CreditsPerClient p", CreditsPerClient.class);
            List<CreditsPerClient> allClients = clients.getResultList();

            //If there are no credits per client in the database
            if (allClients.isEmpty()) {
                //Return empty
                return creditsPerClient;
            }

            for (CreditsPerClient p : allClients) {
                //Add to the map
                creditsPerClient.put(p.getClient_id(), p.getTotal_credits());
            }

            //Return all relevant information
            return creditsPerClient;
        } catch (Exception e) {
            e.printStackTrace();
            return creditsPerClient;
        }
    }

    @GET
    @Path("/listPaymentsPerClient")
    public Map<Integer, Double> ListPaymentsPerClient() {
        //Create map to hold relevant info
        Map<Integer, Double> paymentsPerClient = new HashMap<>();
        try {
            //Get all payments per client from database
            TypedQuery<PaymentsPerClient> clients = em.createQuery("FROM PaymentsPerClient p", PaymentsPerClient.class);
            List<PaymentsPerClient> allClients = clients.getResultList();

            //If there are no payments per client in the database
            if (allClients.isEmpty()) {
                //Return empty
                return paymentsPerClient;
            }

            for (PaymentsPerClient p : allClients) {
                //Add to the map
                paymentsPerClient.put(p.getClient_id(), p.getTotal_payments());
            }

            //Return all relevant info
            return paymentsPerClient;
        } catch (Exception e) {
            e.printStackTrace();
            //Something went wrong, return empty
            return paymentsPerClient;
        }
    }

    @GET
    @Path("/listBalancesPerClient")
    public Map<Integer, Double> ListBalancesPerClient() {
        //Create map to hold all relevant information
        Map<Integer, Double> balancesPerClient = new HashMap<>();
        try {
            //Get all balances per client from database
            TypedQuery<BalancePerClient> clients = em.createQuery("FROM BalancePerClient p", BalancePerClient.class);
            List<BalancePerClient> allClients = clients.getResultList();

            //If there are no balances per client in the database
            if (allClients.isEmpty()) {
                //Return empty
                return balancesPerClient;
            }

            for (BalancePerClient p : allClients) {
                //Add to the map
                balancesPerClient.put(p.getClient_id(), p.getCurrent_balance());
            }

            //Return all relevant information
            return balancesPerClient;
        } catch (Exception e) {
            e.printStackTrace();
            //Something went wrong, return empty
            return balancesPerClient;
        }
    }

    @GET
    @Path("listTotalCredits")
    public Double ListTotalCredits() {
        //Get total credits from the database
        Query q = em.createQuery("FROM TotalResults r where r.aggregate =: aggregate");
        q.setParameter("aggregate", "allCredits");

        try {
            TotalResults credits = (TotalResults) q.getSingleResult();

            //Return sum of all credits
            return credits.getValue();
        } catch (NoResultException e) {
            //No results, return 0 to notify admin
            return 0D;
        }
    }

    @GET
    @Path("listTotalPayments")
    public Double ListTotalPayments() {
        //Get sum of all payments from the database
        Query q = em.createQuery("FROM TotalResults r where r.aggregate =: aggregate");
        q.setParameter("aggregate", "allPayments");

        try {
            TotalResults payments = (TotalResults) q.getSingleResult();

            //Return sum of all payments
            return payments.getValue();
        } catch (NoResultException e) {
            //No results, return 0 to notify admin
            return 0D;
        }
    }

    @GET
    @Path("listTotalBalances")
    public Double ListTotalBalances() {
        //Get sum of balances from database
        Query q = em.createQuery("FROM TotalResults r where r.aggregate =: aggregate");
        q.setParameter("aggregate", "allBalances");
        try {
            TotalResults balances = (TotalResults) q.getSingleResult();

            //Return sum of all balances
            return balances.getValue();
        } catch (NoResultException e) {
            //No results, return 0 to notify admin
            return 0D;
        }
    }

    @GET
    @Path("listClientHighestDebt")
    public Map<String, Object> ListClientHighestDebt() {
        //Create map to hold info
        Map<String, Object> clientHighestDebt = new HashMap<>();

        //Get client with the highest debt
        Query q = em.createQuery("FROM MostNegBalance mnb");
        try {
            MostNegBalance clientId = (MostNegBalance) q.getSingleResult();

            //Get person with correspondent client id
            Query p = em.createQuery("FROM Person p WHERE p.id = :id");
            p.setParameter("id", clientId.getClient_id());
            Person person = (Person) p.getSingleResult();

            //Put relevant info in the map
            clientHighestDebt.put("name", person.getName());
            clientHighestDebt.put("current_balance", clientId.getCurrent_balance());

            //Return it
            return clientHighestDebt;
        } catch (NoResultException e) {
            //Return empty to notify admin
            return clientHighestDebt;
        }
    }

    @GET
    @Path("/billPerClient")
    public Map<Long, Double> BillPerClient() {
        //Create map to hold all relevant information
        Map<Long, Double> billPerClient = new HashMap<>();
        try {
            //Get all windowed credits per client from database
            TypedQuery<WindowedCreditPerClient> billPerClients = em.createQuery("FROM WindowedCreditPerClient wcpc", WindowedCreditPerClient.class);
            List<WindowedCreditPerClient> billPerClientsList = billPerClients.getResultList();

            //If there are no credits per client in the database
            if (billPerClientsList.isEmpty()) {
                //Return empty
                return billPerClient;
            }

            for (WindowedCreditPerClient wcpc : billPerClientsList) {
                //Add to the map
                billPerClient.put(wcpc.getClient_id(), wcpc.getTotal_credits_lastmonth());
            }

            //Return all relevant information
            return billPerClient;
        } catch (Exception e) {
            e.printStackTrace();
            //Something went wrong, return empty
            return billPerClient;
        }
    }
}