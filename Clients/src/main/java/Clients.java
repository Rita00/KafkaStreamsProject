import Entities.Currency;
import com.google.gson.Gson;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import Entities.*;
import org.json.JSONObject;

import java.time.Duration;
import java.util.*;

@SuppressWarnings({"BusyWait", "InfiniteLoopStatement"})
public class Clients {

    public static void main(String[] args) throws Exception {
        //Create instance for random
        Random rand = new Random();
        //Create instance for gson
        Gson gson = new Gson();

        //Set topics names
        String cTopic = "Credits";
        String pTopic = "Payments";
        String dbClientsTopic = "DBInfoTopics";
        String dbCurrenciesTopic = "DBInfoTopics_1";

        //Create instance for properties to access producer configs
        Properties propsProducer = new Properties();
        //Setup properties to produce to both topics
        //Assign localhost id
        propsProducer.put("bootstrap.servers", "localhost:9092");
        //Set acknowledgements for producer requests.
        propsProducer.put("acks", "all");
        //If the request fails, the producer can automatically retry,
        propsProducer.put("retries", 0);
        //Specify buffer size in config
        propsProducer.put("batch.size", 16384);
        //Reduce the no of requests less than 0
        propsProducer.put("linger.ms", 1);
        //The buffer.memory controls the total amount of memory available to the producer for buffering.
        propsProducer.put("buffer.memory", 33554432);
        propsProducer.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        propsProducer.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        //Create producer with previous properties
        Producer<Long, String> producer = new KafkaProducer<>(propsProducer);

        // create instance for properties to access producer configs
        Properties propsConsumer = new Properties();
        //Assign localhost id
        propsConsumer.put("bootstrap.servers", "localhost:9092");
        //Set acknowledgements for producer requests.
        propsConsumer.put("acks", "all");
        //If the request fails, the producer can automatically retry,
        propsConsumer.put("retries", 0);
        //Specify buffer size in config
        propsConsumer.put("batch.size", 16384);
        //Reduce the no of requests less than 0
        propsConsumer.put("linger.ms", 1);
        //The buffer.memory controls the total amount of memory available to the producer for buffering.
        propsConsumer.put("buffer.memory", 33554432);
        propsConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaDBConsumer");
        propsConsumer.put("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        propsConsumer.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        //Create the consumers with previous settings
        Consumer<Long, String> dbClients = new KafkaConsumer<>(propsConsumer);
        Consumer<Long, String> dbCurrencies = new KafkaConsumer<>(propsConsumer);

        //Subscribe to the DBInfoTopics
        dbClients.subscribe(Collections.singletonList(dbClientsTopic));
        dbCurrencies.subscribe(Collections.singletonList(dbCurrenciesTopic));

        //List of client ids
        ArrayList<Person> clients = new ArrayList<>();

        //List of currencies
        ArrayList<Currency> currencies = new ArrayList<>();

        //Initialize variables outside do while
        ConsumerRecords<Long, String> clientRecords;
        Duration dClients;

        //Fetch all data from the DBInfoTopics
        System.out.println("Getting clients from " + dbClientsTopic);
        do {
            dClients = Duration.ofSeconds(30);
            clientRecords = dbClients.poll(dClients);
            //If there are no clients program can't go on
        }
        while (clientRecords.isEmpty());
        System.out.println("Got " + clientRecords.count() + " clients from topic " + dbClientsTopic);

        //Fetch currencies from currencies topic
        System.out.println("Getting currencies from " + dbCurrenciesTopic);
        Duration dCurrencies = Duration.ofSeconds(10);
        ConsumerRecords<Long, String> currencyRecords = dbCurrencies.poll(dCurrencies);
        System.out.println("Got " + currencyRecords.count() + " currencies from topic " + dbCurrenciesTopic);

        //Add euro to always have at least one coin
        Currency euro = new Currency("euro", 1f);
        currencies.add(euro);

        //Initialize all necessary variables
        //sleepTime is the amount of time that separates each pair of credit-payment
        int sleepTime = 10000;
        boolean found;
        Person credClient, payClient;
        Currency credCurr, payCurr;
        Float cred, pay;

        //Results verification
        float totalCredits = 0F;
        float totalPayments = 0F;

        HashMap<Integer, Double> creditsPerClient = new HashMap<>();
        HashMap<Integer, Double> paymentsPerClient = new HashMap<>();

        HashMap<Long, Double> managersRevenue = new HashMap<>();

        while (true) {
            if (clientRecords.count() > 0) {
                for (ConsumerRecord<Long, String> record : clientRecords) {
                    //Parse JSON string to JSON object
                    JSONObject json = new JSONObject(record.value());

                    //Instantiate client from payload part of json object
                    //Which contains all the relevant data
                    Person client = gson.fromJson(json.get("payload").toString(), Person.class);

                    //Check if client is already in the pool
                    found = false;
                    for (Person clnt :
                            clients) {
                        if (clnt.getId() == client.getId()) {
                            found = true;
                            break;
                        }
                    }

                    //If client is not already in the pool
                    if (!found) {
                        //Add client to the pool
                        clients.add(client);
                        creditsPerClient.put(client.getId(), 0D);
                        paymentsPerClient.put(client.getId(), 0D);

                        managersRevenue.put(client.getManager_id(), 0D);

                        System.out.println("Added client: " + client.getName());
                    }
                }
            }

            if (currencyRecords.count() > 0) {
                for (ConsumerRecord<Long, String> record : currencyRecords) {

                    //Parse JSON string to JSON object
                    JSONObject json = new JSONObject(record.value());

                    //Instantiate client from payload part of json object
                    //Which contains all the relevant data
                    JSONObject jsonObject = new JSONObject(json.get("payload").toString());

                    //Set currency exchange rate manually because there was a bug
                    Currency currency = gson.fromJson(json.get("payload").toString(), Currency.class);
                    //where gson was rounding the exchange to 0 or 1
                    currency.setExchangeRate(Float.parseFloat(jsonObject.get("exchangerate").toString()));

                    //Check if currency is already in the pool
                    found = false;
                    for (Currency crr :
                            currencies) {
                        if (crr.getId() == currency.getId()) {
                            found = true;
                            break;
                        }
                    }
                    //If currency is not already in the pool
                    if (!found) {
                        //Add currency to the pool
                        currencies.add(currency);
                        System.out.println("Added currency: " + currency.getName() + " with exchange rate " + currency.getExchangeRate());
                    }
                }
            }

            System.out.println("Number of clients currently in the pool: " + clients.size());
            System.out.println("Number of currencies currently in the pool: " + currencies.size());

            //Choose random client
            credClient = clients.get(rand.nextInt(clients.size()));

            //Produce random credit
            cred = rand.nextFloat() * (20f - 1f);

            //Choose random currency
            credCurr = currencies.get(rand.nextInt(currencies.size()));

            //Create json with all the relevant information
            String credInfo = new JSONObject().put("value", cred)
                    .put("currencyName", credCurr.getName())
                    .put("currencyExchangeRate", credCurr.getExchangeRate())
                    .put("manager_id", credClient.getManager_id())
                    .toString();

            creditsPerClient.put(credClient.getId(), creditsPerClient.get(credClient.getId()) + cred * credCurr.getExchangeRate());
            totalCredits += cred * credCurr.getExchangeRate();

            //Produce to credit topic
            producer.send(new ProducerRecord<>(cTopic, (long) credClient.getId(), credInfo));
            System.out.println("Client " + credClient.getId() + " made a credit of " + cred + " " + credCurr.getName() + ".");

            //Produce random pay
            pay = rand.nextFloat() * (20f - 1f);

            //Choose random client to attach to the payment
            payClient = clients.get(rand.nextInt(clients.size()));

            while (payClient.getId() == 4) {
                payClient = clients.get(rand.nextInt(clients.size()));
            }
            //Choose
            payCurr = currencies.get(rand.nextInt(currencies.size()));

            //Create the json with all the relevant information
            String payInfo = new JSONObject().put("value", pay)
                    .put("currencyName", payCurr.getName())
                    .put("currencyExchangeRate", payCurr.getExchangeRate())
                    .put("manager_id", payClient.getManager_id())
                    .toString();

            paymentsPerClient.put(payClient.getId(), paymentsPerClient.get(payClient.getId()) + pay * payCurr.getExchangeRate());
            managersRevenue.put(payClient.getManager_id(), managersRevenue.get(payClient.getManager_id()) + pay * payCurr.getExchangeRate());
            totalPayments += pay * payCurr.getExchangeRate();

            //Produce to payments topic
            producer.send(new ProducerRecord<>(pTopic, (long) payClient.getId(), payInfo));
            System.out.println("Client " + payClient.getId() + " made a payment of " + pay + " " + payCurr.getName() + ".");

            //Fetch all data from the DBInfoTopics
            System.out.println("Checking for new client records on topic " + dbClientsTopic);
            dClients = Duration.ofMillis(250);
            clientRecords = dbClients.poll(dClients);
            System.out.println(clientRecords.count() + " records found.");

            //Check the topic for new records
            System.out.println("Checking for new currencies records on topic " + dbCurrenciesTopic);
            dCurrencies = Duration.ofMillis(250);
            currencyRecords = dbCurrencies.poll(dCurrencies);
            System.out.println(currencyRecords.count() + " records found.");

            //Results verification
            System.out.println("Results verification");
            System.out.println("Total credits generated: " + totalCredits);
            System.out.println("Total payments generated: " + totalPayments);
            System.out.println("Overall balance: " + (totalPayments - totalCredits));
            System.out.println("Credits per client");
            for (Map.Entry<Integer, Double> tmp :
                    creditsPerClient.entrySet()) {
                System.out.println("Id: " + tmp.getKey() + " sum of credits: " + tmp.getValue());
            }
            System.out.println("Payments per client");
            for (Map.Entry<Integer, Double> tmp :
                    paymentsPerClient.entrySet()) {
                System.out.println("Id: " + tmp.getKey() + " sum of payments: " + tmp.getValue());
            }
            System.out.println("Balance per client");
            for (Map.Entry<Integer, Double> tmpP :
                    paymentsPerClient.entrySet()) {
                for (Map.Entry<Integer, Double> tmpC :
                        creditsPerClient.entrySet()) {
                    if (tmpP.getKey().equals(tmpC.getKey())) {
                        System.out.println("Id: " + tmpC.getKey() + " balance: " + (tmpP.getValue() - tmpC.getValue()));
                    }
                }
            }
            System.out.println("Revenue per manager");
            for (Map.Entry<Long, Double> tmp :
                    managersRevenue.entrySet()) {
                System.out.println("Id: " + tmp.getKey() + " revenue: " + tmp.getValue());
            }

            //Sleep
            Thread.sleep(sleepTime);
        }
    }
}