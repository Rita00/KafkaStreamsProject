import Entities.Client;
import Entities.Currency;
import Entities.Manager;

import java.util.List;
import java.util.Scanner;

public class AdminCLI {


    public static void main(String[]args){

        RestOperations restOp = new RestOperations();
        Scanner sc = new Scanner(System.in);

        int opt;

        while(true){
            System.out.println("Welcome, Admin");
            System.out.println("\tOptions:");
            System.out.println("\t\t[1] - Add a Manager");
            System.out.println("\t\t[2] - Add a Client");
            System.out.println("\t\t[3] - Add a Currency");
            System.out.println("\t\t[4] - List Managers");
            System.out.println("\t\t[5] - List Clients");
            System.out.println("\t\t[6] - List Currencies");
            System.out.println("\tChoose an option: ");

            try {
                opt = sc.nextInt();

                if(opt > 6 || opt < 1){
                    System.out.println("Option invalid please use a number from 1 to 6");
                    continue;
                }

                switch (opt){
                    case 1:
                        System.out.print("\033[H\033[2J");
                        System.out.flush();
                        System.out.printf("\tInsert manager name:");
                        String managerName = sc.nextLine();
                        if(restOp.AddManager(managerName)){
                            System.out.println("Manager added successfully");
                        }
                        break;
                    case 2:
                        System.out.print("\033[H\033[2J");
                        System.out.flush();
                        System.out.printf("\tInsert client name:");
                        String clientName = sc.nextLine();
                        if(restOp.AddClient(clientName)){
                            System.out.println("Client added successfully");
                        }
                        break;
                    case 3:
                        System.out.print("\033[H\033[2J");
                        System.out.flush();
                        System.out.printf("\tInsert currency name:");
                        String currencyName = sc.nextLine();
                        System.out.printf("\tInsert currency exchange rate:");
                        float exchangeRate = sc.nextFloat();
                        if(restOp.AddCurrency(currencyName, exchangeRate)){
                            System.out.println("Currency added successfully");
                        }
                        break;
                    case 4:
                        System.out.print("\033[H\033[2J");
                        System.out.flush();
                        System.out.println("\tList of managers: ");
                        List<Manager> mngrs = restOp.ListManagers();
                        for (Manager mngr:
                             mngrs) {
                            System.out.println("Name: " + mngr.getName());
                        }
                        break;
                    case 5:
                        System.out.print("\033[H\033[2J");
                        System.out.flush();
                        System.out.println("\tList of clients: ");
                        List<Client> clnts = restOp.ListClients();
                        for (Client clnt:
                                clnts) {
                            System.out.println("Name: " + clnt.getName());
                        }
                        break;
                    case 6:
                        System.out.print("\033[H\033[2J");
                        System.out.flush();
                        System.out.println("\tList of currencies: ");
                        List<Currency> crrncs = restOp.ListCurrencies();
                        for (Currency crrnc:
                                crrncs) {
                            System.out.println("Name: " + crrnc.getName() + "| Exchange rate: " + crrnc.getExchangeRate());
                        }
                        break;
                }
            }
            catch (NumberFormatException e) {
                System.out.println("Input String cannot be parsed to Integer.");
            }
        }
    }
}