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

    //Todo nao permitir nomes iguais
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
            System.out.println("Manager name: " + managerName);
            Manager m = new Manager(managerName);
            System.out.println("Manager: " + m);
            em.persist(m);
            System.out.println("Manager ok");
            return Response.status(Status.OK).entity("Manager inserido com sucesso!").build();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROOO!!!");
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
    public Map<Long, String> ListClients() {
        Map<Long, String> allClientsInfo = new HashMap<>();
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

    @GET
    @Path("/listCreditPerClient")
    public Map<Integer, Double> ListCreditPerClient() {
        Map<Integer, Double> creditsPerClient = new HashMap<>();
        try {
            TypedQuery<CreditsPerClient> clients = em.createQuery("FROM CreditsPerClient p", CreditsPerClient.class);
            List<CreditsPerClient> allClients = clients.getResultList();
            for (CreditsPerClient p : allClients) {
                creditsPerClient.put(p.getClient_id(), p.getTotal_credits());
            }
            System.out.println(creditsPerClient);
            return creditsPerClient;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GET
    @Path("/listPaymentsPerClient")
    public Map<Integer, Double> ListPaymentsPerClient() {
        Map<Integer, Double> paymentsPerClient = new HashMap<>();
        try {
            TypedQuery<PaymentsPerClient> clients = em.createQuery("FROM PaymentsPerClient p", PaymentsPerClient.class);
            List<PaymentsPerClient> allClients = clients.getResultList();
            for (PaymentsPerClient p : allClients) {
                paymentsPerClient.put(p.getClient_id(), p.getTotal_payments());
            }
            System.out.println(paymentsPerClient);
            return paymentsPerClient;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GET
    @Path("/listBalancesPerClient")
    public Map<Integer, Double> ListBalancesPerClient() {
        Map<Integer, Double> balancesPerClient = new HashMap<>();
        try {
            TypedQuery<BalancePerClient> clients = em.createQuery("FROM BalancePerClient p", BalancePerClient.class);
            List<BalancePerClient> allClients = clients.getResultList();
            for (BalancePerClient p : allClients) {
                balancesPerClient.put(p.getClient_id(), p.getCurrent_balance());
            }
            System.out.println(balancesPerClient);
            return balancesPerClient;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GET
    @Path("listTotalCredits")
    public Double ListTotalCredits() {
        Query q = em.createQuery("FROM TotalResults r where r.aggregate =: aggregate");
        q.setParameter("aggregate", "allCredits");

        try {
            TotalResults credits = (TotalResults) q.getSingleResult();
            return credits.getValue();
        } catch (NoResultException e) {
            return 0d;
        }
    }

    @GET
    @Path("listTotalPayments")
    public Double ListTotalPayments() {
        Query q = em.createQuery("FROM TotalResults r where r.aggregate =: aggregate");
        q.setParameter("aggregate", "allPayments");

        try {
            TotalResults payments = (TotalResults) q.getSingleResult();
            return payments.getValue();
        } catch (NoResultException e) {
            return 0d;
        }
    }

    @GET
    @Path("listTotalBalances")
    public Double ListTotalBalances() {
        Query q = em.createQuery("FROM TotalResults r where r.aggregate =: aggregate");
        q.setParameter("aggregate", "allBalances");

        try {
            TotalResults balances = (TotalResults) q.getSingleResult();
            return balances.getValue();
        } catch (NoResultException e) {
            return 0d;
        }
    }


    @GET
    @Path("listClientHighestDebt")
    public Map<String, Object> ListClientHighestDebt() {
        Map<String, Object> clientHighestDebt = new HashMap<>();
        Query q = em.createQuery("FROM MostNegBalance mnb");
        System.out.println("Aqui0");
        try {
            MostNegBalance clientId = (MostNegBalance) q.getSingleResult();
            System.out.println("Aqui1");
            Query p = em.createQuery("FROM Person p WHERE p.id = :id");
            System.out.println("Aqui2");
            p.setParameter("id", clientId.getClient_id());
            System.out.println("Highest DebtID: " + clientId);
            Person person = (Person) p.getSingleResult();
            System.out.println("Highest Debt Person name: " + person.getName());
            System.out.println("Highest Debt current balance: " + clientId.getCurrent_balance());
            clientHighestDebt.put("name", person.getName());
            clientHighestDebt.put("current_balance", clientId.getCurrent_balance());
            System.out.println("Highest Debt Map: " + clientHighestDebt.get("name") + " - " + clientHighestDebt.get("current_balance"));
            return clientHighestDebt;
        } catch (NoResultException e) {
            return clientHighestDebt;
        }
    }
}