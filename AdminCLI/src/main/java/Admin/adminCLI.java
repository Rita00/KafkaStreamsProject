package Admin;

import java.util.Scanner;

public class adminCLI {

    public static void main(String[]args){

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
                        System.out.printf("\tInsert manager password:");
                        String managerPassword = sc.nextLine();
                        if(addManager(managerName, managerPassword)){
                            System.out.println("Manager added successfully");
                        }
                        break;
                    case 2:
                        System.out.print("\033[H\033[2J");
                        System.out.flush();
                        System.out.printf("\tInsert client name:");
                        String clientName = sc.nextLine();
                        System.out.printf("\tInsert client password:");
                        String clientPassword = sc.nextLine();
                        if(addClient(clientName, clientPassword)){
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
                        if(addCurrency(currencyName, exchangeRate)){
                            System.out.println("Currency added successfully");
                        }
                        break;
                    case 4:
                        listManagers();
                        break;
                    case 5:
                        listClients();
                        break;
                    case 6:
                        listCurrencies();
                        break;
                }
            }
            catch (NumberFormatException e) {
                System.out.println("Input String cannot be parsed to Integer.");
            }
        }
    }

    private static void listCurrencies() {
    }

    private static void listClients() {
    }

    private static void listManagers() {
    }

    private static boolean addCurrency(String currencyName, float exchangeRate) {
        return false;
    }

    private static boolean addClient(String clientName, String clientPassword) {
        return false;
    }

    private static boolean addManager(String managerName, String managerPassword) {
        return false;
    }
}
