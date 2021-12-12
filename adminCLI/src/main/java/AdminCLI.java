import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

@SuppressWarnings({"DuplicatedCode", "ResultOfMethodCallIgnored", "IfStatementWithIdenticalBranches"})
public class AdminCLI {

    public static void printMenu(String menuHeader, String[] menuOptions, boolean isMessage, String input) {
        //Clear screen
        System.out.print("\033[H\033[2J");
        System.out.flush();

        //Print menu header
        System.out.println("\t\t ##-Administration Menu-##");
        System.out.println("\t\t\t" + menuHeader);

        //Print every item in menu
        for (String opt : menuOptions) {
            System.out.println(opt);
        }

        if(!isMessage){
            System.out.print(input);
        }
    }


    public static void main(String[] args) throws IOException {
        //Build client for exposed rest
        Client restClient = ClientBuilder.newClient();

        //Scanner to handle user input
        Scanner input = new Scanner(System.in);

        //Main menu options
        String[] mainMenuOptions = {
                "1 -> Add Manager",
                "2 -> Add Person",
                "3 -> Add Currency",
                "4 -> List Managers",
                "5 -> List Clients",
                "6 -> List Currencies",
                "7 -> Show credits per client",
                "8 -> Show payments per client",
                "9 -> Show balances per client",
                "10 -> Show total credits",
                "11 -> Show total payments",
                "12 -> Show total balances",
                "13 -> Show the bill per client for the last month",
                "14 -> Show clients with no payments for the last two months",
                "15 -> Show client with highest debt",
                "16 -> Show the manager with the highest revenue",
                "17 -> Exit"
        };

        int opt;
        WebTarget managerTarget, clientTarget, currencyTarget;
        while (true) {

            //Print menu header and options
            printMenu("Main Menu", mainMenuOptions, false, "Choose option: ");

            //Validate input
            try {
                opt = input.nextInt();
                input.nextLine();

                if (opt > mainMenuOptions.length || opt < 1) {
                    System.out.println("Please use a valid number from 1 to " + mainMenuOptions.length);
                    System.out.println("Press enter to continue...");
                    System.in.read();
                    continue;
                }

            } catch (Exception ex) {
                input.nextLine();
                System.out.println("Please use a valid number from 1 to " + mainMenuOptions.length);
                System.out.println("Press enter to continue...");
                System.in.read();
                continue;
            }

            switch (opt) {
                case 1:
                    //Set target trough rest link
                    managerTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/addManager");

                    //To add manager only the name is needed
                    String[] addManagerOptions = {
                            "Insert manager name: ",
                            "[1] -> Go back"
                    };

                    //Print menu header and options
                    printMenu("Add Manager Menu", addManagerOptions, false, "Choose option or insert name: ");

                    String managerName;

                    try {
                        //If its it then
                        opt = input.nextInt();
                        //Verify input
                        if (opt == 1) {
                            //Go back
                            break;
                        } else {
                            //Try again
                            System.out.println("Please use a valid number from 1 to " + 1);
                            System.out.println("Press enter to continue...");
                            System.in.read();
                            break;
                        }
                    } catch (Exception ex) {
                        //Get the name
                        managerName = input.nextLine();
                    }

                    //Create object
                    Entity<String> inputManager = Entity.entity(managerName, MediaType.APPLICATION_JSON);

                    //Try to add to the database
                    Response addManagerResponse = managerTarget.request().post(inputManager);

                    //Print feedback to admin
                    System.out.println(addManagerResponse.readEntity(String.class));

                    //Close response
                    addManagerResponse.close();

                    System.out.println("Press enter to continue...");
                    System.in.read();

                    break;
                case 2:
                    //Get clients
                    clientTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/addClients");
                    //Get mangers
                    managerTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/listManagers");

                    //Properties are client name and manager
                    HashMap<String, Object> clientsProp = new HashMap<>();

                    String[] addClientOptions = {
                            "Insert client name: ",
                            "[1] -> Go back"
                    };

                    printMenu("Add Client Menu", addClientOptions, false, "Choose option or insert name: ");

                    String clientName;
                    try {
                        //If its it then
                        opt = input.nextInt();
                        //Verify input
                        if (opt == 1) {
                            //Go back
                            break;
                        } else {
                            //Try again
                            System.out.println("Please use a valid number from 1 to " + 1);
                            System.out.println("Press enter to continue...");
                            System.in.read();
                            continue;
                        }
                    } catch (Exception ex) {
                        //Get the name
                        clientName = input.nextLine();
                    }

                    clientsProp.put("name", clientName);
                    Entity<HashMap<String, Object>> inputClient = Entity.entity(clientsProp, MediaType.APPLICATION_JSON);

                    String[] addClientOptionsChooseManager = {
                            "Please choose a manager for the client: "
                    };

                    Response addClientResponseManagers = managerTarget.request().get();
                    Map<Integer, String> managerNames = addClientResponseManagers.readEntity(new GenericType<>() {
                    });

                    printMenu("Add Client Menu", addClientOptionsChooseManager, true, "");

                    List<Integer> managerMapKeys = new ArrayList<>(managerNames.keySet());
                    for (int i = 1; i <= managerNames.keySet().size(); i++) {
                        System.out.println(i + " -> " + managerNames.get(managerMapKeys.get(i - 1)));
                    }

                    System.out.print("Please choose an option: ");

                    int index;
                    try {
                        index = input.nextInt();
                        input.nextLine();
                        //Verify input
                        if (index < 1 || index > managerNames.keySet().size() + 1) {
                            //Try again
                            System.out.println("Please use a valid number from 1 to " + managerNames.keySet().size() + 1);
                            System.out.println("Press enter to continue...");
                            System.in.read();
                            continue;
                        }
                    } catch (Exception ex) {
                        System.out.println("Please use a valid number from 1 to " + managerNames.keySet().size() + 1);
                        System.out.println("Press enter to continue...");
                        System.in.read();
                        continue;
                    }

                    clientsProp.put("managerID", managerMapKeys.get(index - 1));

                    Response addClientResponseClients = clientTarget.request().post(inputClient);

                    System.out.println(addClientResponseClients.readEntity(String.class));

                    System.out.println("Press enter continue...");
                    System.in.read();

                    addClientResponseClients.close();

                    break;

                case 3:
                    currencyTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/addCurrency");

                    HashMap<String, Object> currencyProp = new HashMap<>();

                    String[] addCurrencyOptions = {
                            "Insert currency name and exchange rate (should be float) separated by a single comma",
                            "[1] -> Go Back"
                    };

                    printMenu("Add Currency Menu", addCurrencyOptions, false, "Choose option or insert coin: ");

                    String currencyInfo;
                    try {
                        opt = input.nextInt();
                        input.nextLine();
                        //Verify input
                        if (opt != 1) {
                            //Try again
                            System.out.println("Please use a valid number from 1 to " + 1);
                            System.out.println("Press enter to continue...");
                            System.in.read();
                            continue;
                        } else {
                            break;
                        }
                    } catch (Exception ex) {
                        currencyInfo = input.nextLine();
                    }

                    if (!currencyInfo.equals("")) {
                        String currencyName = currencyInfo.split(",")[0];
                        currencyProp.put("name", currencyName);

                        double exchangeRate;

                        try {
                            exchangeRate = Double.parseDouble(currencyInfo.split(",")[1]);
                        } catch (Exception e) {
                            System.out.println("Currency exchange rate is not a float. Couldn't add currency.");
                            System.out.println("Press enter to continue...");
                            System.in.read();
                            break;
                        }

                        currencyProp.put("exchangeRate", exchangeRate);

                        Entity<HashMap<String, Object>> inputCurrency = Entity.entity(currencyProp, MediaType.APPLICATION_JSON);
                        Response addCurrencyResponse = currencyTarget.request().post(inputCurrency);

                        System.out.println(addCurrencyResponse.readEntity(String.class));

                        System.out.println("Press enter continue...");
                        System.in.read();

                        addCurrencyResponse.close();
                    }
                    break;
                case 4:
                    managerTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/listManagers");
                    Response managersResponse = managerTarget.request().get();

                    Map<Integer, String> allManagers = managersResponse.readEntity(new GenericType<>() {});

                    String[] listManagerOptions = {"List of Managers: "};

                    printMenu("List Managers Menu", listManagerOptions, true, "");

                    if (allManagers.isEmpty()) {
                        System.out.println("There are no managers.\nPlease add some...\nPress enter continue...");
                        System.in.read();
                    } else {
                        List<Integer> allManagersKeys = new ArrayList<>(allManagers.keySet());
                        for (int i = 1; i <= allManagers.keySet().size(); i++) {
                            System.out.println(i + " -> " + allManagers.get(allManagersKeys.get(i - 1)));
                        }
                        System.out.println("Press enter continue...");
                        System.in.read();
                    }
                    break;
                case 5:
                    clientTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/listClients");
                    Response clients = clientTarget.request().get();

                    Map<Integer, String> allClients = clients.readEntity(new GenericType<>() {});

                    String[] listClientOptions = {"List of clients: "};

                    printMenu("List Clients Menu", listClientOptions, true, "");

                    if (allClients.isEmpty()) {
                        System.out.println("Press enter continue...");
                        System.in.read();
                    } else {
                        List<Integer> allClientsKeys = new ArrayList<>(allClients.keySet());
                        for (int i = 1; i <= allClients.keySet().size(); i++) {
                            System.out.println(i + " -> " + allClients.get(allClientsKeys.get(i - 1)));
                        }
                        System.out.println("Press enter continue...");
                        System.in.read();
                    }
                    break;
                case 6:
                    currencyTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/listCurrencies");
                    Response currencies = currencyTarget.request().get();

                    Map<String, Double> allCurrencies = currencies.readEntity(new GenericType<>() {});

                    String[] listCurrenciesOptions = {"List of currencies: "};

                    printMenu("List Currencies Menu", listCurrenciesOptions, true, "");

                    if (allCurrencies.isEmpty()) {
                        System.out.println("Press enter continue...");
                        System.in.read();
                    } else {
                        for (Map.Entry<String, Double> mngrNm : allCurrencies.entrySet()) {
                            System.out.println(mngrNm.getKey() + " - " + mngrNm.getValue());
                        }
                        System.out.println("Press enter continue...");
                        System.in.read();
                    }
                    break;
                case 7:
                    clientTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/listCreditPerClient");
                    Response creditsPerClient = clientTarget.request().get();

                    Map<String, Float> allCreditsPerClient = creditsPerClient.readEntity(new GenericType<>() {});

                    String[] listCreditsPerClientOptions = {"List of credits per client: "};

                    printMenu("Credits Menu", listCreditsPerClientOptions, true, "");

                    if (allCreditsPerClient.isEmpty()) {
                        System.out.println("Something went wrong there are no credits\nPress enter continue...");
                        System.in.read();
                    } else {
                        for (Map.Entry<String, Float> mngrNm : allCreditsPerClient.entrySet()) {
                            System.out.println(mngrNm.getKey() + " -> " + mngrNm.getValue());
                        }
                        System.out.println("Press enter continue...");
                        System.in.read();

                    }
                    break;
                case 8:
                    clientTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/listPaymentsPerClient");
                    Response paymentsPerClient = clientTarget.request().get();

                    Map<String, Float> allPaymentsPerClient = paymentsPerClient.readEntity(new GenericType<>() {});

                    String[] listPaymentsPerClientOptions = {"List of payments per client: "};

                    printMenu("Payments Menu", listPaymentsPerClientOptions, true, "");
                    if (allPaymentsPerClient.isEmpty()) {
                        System.out.println("Something went wrong there are no payments\nPress enter continue...");
                        System.in.read();
                    } else {
                        for (Map.Entry<String, Float> mngrNm : allPaymentsPerClient.entrySet()) {
                            System.out.println(mngrNm.getKey() + " -> " + mngrNm.getValue());
                        }
                        System.out.println("Press enter continue...");
                        System.in.read();
                    }
                    break;
                case 9:
                    clientTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/listBalancesPerClient");
                    Response balancesPerClient = clientTarget.request().get();

                    Map<String, Float> allBalancesPerClient = balancesPerClient.readEntity(new GenericType<>() {});

                    String[] listBalancesPerClientOptions = {"List of balances per client: "};

                    printMenu("Balances Menu", listBalancesPerClientOptions, true, "");
                    if (allBalancesPerClient.isEmpty()) {
                        System.out.println("Something went wrong there are no payments\nPress enter continue...");
                        System.in.read();
                    } else {
                        for (Map.Entry<String, Float> mngrNm : allBalancesPerClient.entrySet()) {
                            System.out.println(mngrNm.getKey() + " -> " + mngrNm.getValue());
                        }
                        System.out.println("Press enter continue...");
                        System.in.read();
                    }
                    break;
                case 10:
                    clientTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/listTotalCredits");
                    Response allCredits = clientTarget.request().get();

                    Double totalCredits = allCredits.readEntity(Double.class);

                    String[] TotalCreditsInfo = {""};

                    printMenu("Total Credits Menu", TotalCreditsInfo, true, "");

                    if (totalCredits == 0) {
                        System.out.println("Something went wrong there are no total credits\nPress enter continue...");
                        System.in.read();
                    } else {
                        System.out.println("Total credits: " + totalCredits);
                        System.out.println("Press enter continue...");
                        System.in.read();
                    }
                    break;
                case 11:
                    clientTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/listTotalPayments");
                    Response allPayments = clientTarget.request().get();

                    Double totalPayments = allPayments.readEntity(Double.class);

                    String[] TotalPaymentsInfo = {""};

                    printMenu("Total Payments Menu", TotalPaymentsInfo, true, "");

                    if (totalPayments == 0) {
                        System.out.println("Something went wrong there are no total payments\nPress enter continue...");
                        System.in.read();
                    } else {
                        System.out.println("Total payments: " + totalPayments);
                        System.out.println("Press enter continue...");
                        System.in.read();
                    }
                    break;
                case 12:
                    clientTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/listTotalBalances");
                    Response allBalances = clientTarget.request().get();

                    Double totalBalances = allBalances.readEntity(Double.class);

                    String[] TotalBalancesInfo = {""};

                    printMenu("Total Balance Menu", TotalBalancesInfo, true, "");

                    if (totalBalances == 0) {
                        System.out.println("Something went wrong there are no total balances\nPress enter continue...");
                        System.in.read();
                    } else {
                        System.out.println("Total balances: " + totalBalances);
                        System.out.println("Press enter continue...");
                        System.in.read();
                    }
                    break;
                case 13:
                    clientTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/billPerClient");
                    Response billPerClient = clientTarget.request().get();

                    Map<Long, Double> allBillsPerClient = billPerClient.readEntity(new GenericType<>() {});
                    String[] listBillPerClientOptions = {"List of bills per client for the last month: "};

                    printMenu("Bills Menu", listBillPerClientOptions, true, "");
                    if (allBillsPerClient.isEmpty()) {
                        System.out.println("Something went wrong there are no payments\nPress enter continue...");
                        System.in.read();
                    } else {
                        for (Map.Entry<Long, Double> mngrNm : allBillsPerClient.entrySet()) {
                            System.out.println(mngrNm.getKey() + " -> " + mngrNm.getValue());
                        }
                        System.out.println("Press enter continue...");
                        System.in.read();
                    }
                    break;
                case 14:
                    System.out.println("To-Do option 14");
                    break;
                case 15:
                    clientTarget = restClient.target("http://host.docker.internal:8080/restws/rest/RestOperations/listClientHighestDebt");
                    Response clientId = clientTarget.request().get();

                    Map<String, Object> clientInfo = clientId.readEntity(new GenericType<>() {});

                    String[] highestDebInfo = {"Client with Highest Debt: "};

                    printMenu("header", highestDebInfo, true, "");

                    if (clientInfo.isEmpty()) {
                        System.out.println("Something went wrong there are no client found\nPress enter continue...");
                        System.in.read();
                    } else {
                        System.out.println("Client name: " + clientInfo.get("name").toString() + " with balance: " + Double.parseDouble(clientInfo.get("current_balance").toString()) + "€");

                        System.out.println("Press enter continue...");
                        System.in.read();
                    }
                    break;
                case 16:
                    System.out.println("To-Do option 16");
                    break;
                case 17:
                    System.out.println("Goodbye!");
                    return;
                default:
                    return;
            }
        }
    }
}