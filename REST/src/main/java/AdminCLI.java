import Entities.Client;
import Entities.Currency;
import Entities.Manager;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class AdminCLI {

    public static void printMenu(String menuHeader, String menuOptions[], boolean isMessage) {
        //Clear screen
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println("\t\t" + menuHeader);

        for (String opt : menuOptions) {
            System.out.println(opt);
        }

        if (!isMessage) {
            System.out.print("Choose an option: ");
        }
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        RestOperations restOp = new RestOperations();
        Scanner input = new Scanner(System.in);

        String mainOptions[] = {
                "1 - Add Manager",
                "2 - Add Client",
                "3 - Add Currency",
                "4 - List Managers",
                "5 - List Clients",
                "6 - List Currencies",
                "7 - Exit"
        };

        String header = "Administration Menu";

        int opt;
        while (true){
            printMenu(header, mainOptions, false);

            opt = input.nextInt();
            input.nextLine();

            switch(opt){
                case 1:
                    String addManagerOptions[] = {
                        "Insert manager name: "
                    };

                    printMenu(header, addManagerOptions , true);
                    String managerName = input.nextLine();

                    if(restOp.AddManager(managerName)){
                        String addManagerMessages[] = {
                                "Manager " + managerName + " added successfully!",
                                "Press enter to proceed..."
                        };

                        printMenu(header, addManagerMessages, true);
                        System.in.read();
                    }else{
                        String addManagerMessages[] = {
                                "Something went wrong while adding manager " + managerName,
                                "Press enter to proceed..."
                        };

                        printMenu(header, addManagerMessages, true);
                        System.in.read();
                    }
                    break;
                case 2:
                    String addClientOptions[] = {
                            "Insert client name: "
                    };

                    printMenu(header, addClientOptions , true);
                    String clientName = input.nextLine();

                    if(restOp.AddClient(clientName)){
                        String addClientMessages[] = {
                                "Client " + clientName + " added successfully!",
                                "Press enter to proceed..."
                        };

                        printMenu(header, addClientMessages, true);
                        System.in.read();
                    }else{
                        String addClientMessages[] = {
                                "Something went wrong while adding client " + clientName,
                                "Press enter to proceed..."
                        };

                        printMenu(header, addClientMessages, true);
                        System.in.read();
                    }
                    break;

                case 3:
                    String addCurrencyOptions[] = {
                            "Insert currency name",
                            "and exchange rate (should be float)",
                            "separated by a single space"
                    };

                    printMenu(header, addCurrencyOptions , true);
                    String info = input.nextLine();

                    String currencyName = info.split(" ")[0];
                    Float exchangeRate = Float.parseFloat(info.split(" ")[1]);

                    if(restOp.AddCurrency(currencyName, exchangeRate)){
                        String addCurrencyMessages[] = {
                                "Currency " + currencyName + " with exchange rate " + exchangeRate + " added successfully!",
                                "Press enter to proceed..."
                        };

                        printMenu(header, addCurrencyMessages, true);
                        System.in.read();
                    }else{
                        String addCurrencyMessages[] = {
                                "Something went wrong while adding currency " + currencyName + " with exchange rate " + exchangeRate,
                                "Press enter to proceed..."
                        };

                        printMenu(header, addCurrencyMessages, true);
                        System.in.read();
                    }
                    break;
            }
            Thread.sleep(250);
        }

    }
}