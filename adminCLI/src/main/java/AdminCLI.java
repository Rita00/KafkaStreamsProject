//import Entities.Person;
//import Entities.Currency;
//import Entities.Manager;

import data.Manager;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

public class AdminCLI {

    public static void printMenu(String menuHeader, String menuOptions[], boolean isMessage) {
        //Clear screen
//        System.out.print("\033[H\033[2J");
//        System.out.flush();

        System.out.println("\t\t" + menuHeader);

        for (String opt : menuOptions) {
            System.out.println(opt);
        }

        if (!isMessage) {
            System.out.print("Choose an option: ");
        }
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        Client client = ClientBuilder.newClient();

//        RestOperations restOp = new RestOperations();
        Scanner input = new Scanner(System.in);

        String mainOptions[] = {
                "1 - Add Manager",
                "2 - Add Person",
                "3 - Add Currency",
                "4 - List Managers",
                "5 - List Clients",
                "6 - List Currencies",
                "7 - Show credits per client",
                "8 - Show payments per client",
                "9 - Show balances per client",

                "7 - Exit"
        };

        String header = "Administration Menu";

        int opt;
        while (true) {
            printMenu(header, mainOptions, false);

            opt = input.nextInt();
            input.nextLine();

            switch (opt) {
                case 1:
                    WebTarget target = client.target("http://host.docker.internal:8080/restws/rest/RestOperations/addManager");

                    String addManagerOptions[] = {
                            "Insert manager name: "
                    };

                    printMenu(header, addManagerOptions, true);
                    String managerName = input.nextLine();
                    Entity<String> inputManager = Entity.entity(managerName, MediaType.APPLICATION_JSON);
                    Response response = target.request().post(inputManager);
                    String value = response.readEntity(String.class);
                    System.out.println("RESPONSE: " + value);
                    response.close();
//                    if (restOp.AddManager(managerName)) {
//                        String addManagerMessages[] = {
//                                "Manager " + managerName + " added successfully!",
//                                "Press enter to proceed..."
//                        };
//
//                        printMenu(header, addManagerMessages, true);
//                        System.in.read();
//                    } else {
//                        String addManagerMessages[] = {
//                                "Something went wrong while adding manager " + managerName,
//                                "Press enter to proceed..."
//                        };
//
//                        printMenu(header, addManagerMessages, true);
//                        System.in.read();
//                    }
                    break;
                case 2:
                    HashMap<String, Object> clientsProp = new HashMap<>();
                    target = client.target("http://host.docker.internal:8080/restws/rest/RestOperations/addClients");
                    WebTarget get = client.target("http://host.docker.internal:8080/restws/rest/RestOperations/listManagers");

                    String addClientOptions[] = {
                            "Insert client name: "
                    };

                    printMenu(header, addClientOptions, true);
                    String clientName = input.nextLine();

                    clientsProp.put("name", clientName);
                    Entity<HashMap<String, Object>> inputClient = Entity.entity(clientsProp, MediaType.APPLICATION_JSON);

                    String addClientOptions2[] = {
                            "Please choose a manager: "
                    };

                    Response managers = get.request().get();
                    Map<Integer, String> managerNames = managers.readEntity(new GenericType<Map<Integer, String>>() {
                    });
                    System.out.println(managerNames);
//                    String managerList[] = managerNames.split(",");
//                    int i = 1;
                    //for (Map.Entry<Integer, String> mngrNm : managerNames.entrySet()) {
                    List keys = new ArrayList(managerNames.keySet());
                    for (int i = 1; i <= managerNames.keySet().size(); i++) {
                        System.out.println(i + " - " + managerNames.get(keys.get(i - 1)));
                        //i++;
                    }

                    //TODO verify if choose a correct option
                    printMenu(header, addClientOptions2, false);
                    int index = input.nextInt();
                    input.nextLine();
//
////                    String clientManager = managerList[index];
//
                    clientsProp.put("managerID", keys.get(index - 1));
                    System.out.println(keys.get(index - 1));

                    response = target.request().post(inputClient);
                    value = response.readEntity(String.class);
                    System.out.println("RESPONSE: " + value);
                    response.close();

//                    if (restOp.AddClient(clientName)) {
//                        String addClientMessages[] = {
//                                "Person " + clientName + " added successfully!",
//                                "Press enter to proceed..."
//                        };
//
//                        printMenu(header, addClientMessages, true);
//                        System.in.read();
//                    } else {
//                        String addClientMessages[] = {
//                                "Something went wrong while adding client " + clientName,
//                                "Press enter to proceed..."
//                        };
//
//                        printMenu(header, addClientMessages, true);
//                        System.in.read();
//                    }
                    break;

                case 3:
                    target = client.target("http://host.docker.internal:8080/restws/rest/RestOperations/addCurrency");

                    HashMap<String, Object> currencyProp = new HashMap<>();

                    String addCurrencyOptions[] = {
                            "Insert currency name",
                            "and exchange rate (should be float)",
                            "separated by a single space"
                    };
//
                    // TODO if currencies with 2 names
                    printMenu(header, addCurrencyOptions, true);
                    String info = input.nextLine();
//
                    String currencyName = info.split(" ")[0];
                    currencyProp.put("name", currencyName);

                    Float exchangeRate = Float.parseFloat(info.split(" ")[1]);
                    currencyProp.put("exchangeRate", exchangeRate);

                    Entity<HashMap<String, Object>> inputCurrency = Entity.entity(currencyProp, MediaType.APPLICATION_JSON);
                    response = target.request().post(inputCurrency);
                    value = response.readEntity(String.class);
                    System.out.println("RESPONSE: " + value);
                    response.close();

//                    if (restOp.AddCurrency(currencyName, exchangeRate)) {
//                        String addCurrencyMessages[] = {
//                                "Currency " + currencyName + " with exchange rate " + exchangeRate + " added successfully!",
//                                "Press enter to proceed..."
//                        };
//
//                        printMenu(header, addCurrencyMessages, true);
//                        System.in.read();
//                    } else {
//                        String addCurrencyMessages[] = {
//                                "Something went wrong while adding currency " + currencyName + " with exchange rate " + exchangeRate,
//                                "Press enter to proceed..."
//                        };
//
//                        printMenu(header, addCurrencyMessages, true);
//                        System.in.read();
//                    }
                    break;
                case 4:
                    target = client.target("http://host.docker.internal:8080/restws/rest/RestOperations/listManagers");
                    managers = target.request().get();
                    Map<Integer, String> allManagers = managers.readEntity(new GenericType<Map<Integer, String>>() {
                    });

                    String listManagerOptions[] = {"List of Managers: "};

                    printMenu(header, listManagerOptions, true);

                    if (allManagers.isEmpty())
                        System.out.println("Sem Managers!");
                    else {
                        keys = new ArrayList(allManagers.keySet());
                        for (int i = 1; i <= allManagers.keySet().size(); i++) {
                            System.out.println(i + " - " + allManagers.get(keys.get(i - 1)));
                            //i++;
                        }
                    }
                    break;
                case 5:
                    target = client.target("http://host.docker.internal:8080/restws/rest/RestOperations/listClients");
                    Response clients = target.request().get();

                    Map<Integer, String> allClients = clients.readEntity(new GenericType<Map<Integer, String>>() {
                    });
                    String listClientOptions[] = {"List of clients: "};

                    printMenu(header, listClientOptions, true);

                    if (allClients.isEmpty())
                        System.out.println("Sem Clientes!");
                    else {
                        keys = new ArrayList(allClients.keySet());
                        for (int i = 1; i <= allClients.keySet().size(); i++) {
                            System.out.println(i + " - " + allClients.get(keys.get(i - 1)));
                            //i++;
                        }
                    }

                    break;
                case 6:
                    target = client.target("http://host.docker.internal:8080/restws/rest/RestOperations/listCurrencies");
                    Response currencies = target.request().get();

                    Map<String, Double> allCurrencies = currencies.readEntity(new GenericType<Map<String, Double>>() {
                    });
                    String listCurrenciesOptions[] = {"List of currencies: "};

                    printMenu(header, listCurrenciesOptions, true);
                    response = target.request().get();

                    if (allCurrencies.isEmpty())
                        System.out.println("Sem moedas!");
                    else {
                        for (Map.Entry<String, Double> mngrNm : allCurrencies.entrySet()) {
                            System.out.println(mngrNm.getKey() + " - " + mngrNm.getValue());
                        }
                    }
                    break;
                case 7:
                    return;
                case 8:
                    return;
            }
            Thread.sleep(250);
        }
    }
}