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

        //Fetch all data from the DBInfoTopics
        System.out.println("Getting clients from " + dbClientsTopic);
        Duration dClients = Duration.ofSeconds(30);
        ConsumerRecords<Long, String> clientRecords = dbClients.poll(dClients);
        System.out.println("Got " + clientRecords.count() + " clients from topic " + dbClientsTopic);

        //If there are no clients program can't go on
        if(clientRecords.isEmpty()){
            System.out.println("Couldn't get clients from " + dbClientsTopic);
            return;
        }

        //Fetch currencies from currencies topic
        System.out.println("Getting currencies from " + dbCurrenciesTopic);
        Duration dCurrencies = Duration.ofSeconds(10);
        ConsumerRecords<Long, String> currencyRecords = dbCurrencies.poll(dCurrencies);
        System.out.println("Got " + currencyRecords.count() + " currencies from topic " + dbCurrenciesTopic);

        //Add euro to always have at least one coin
        Currency euro = new Currency("euro", 1f);
        currencies.add(euro);

        int sleepTime = 5000;
        boolean found = false;

        Person credClient, payClient;
        Currency credCurr, payCurr;
        Float cred, pay;
        while (true) {
            if(clientRecords.count() > 0){
                for (ConsumerRecord<Long, String> record : clientRecords) {
                    //Parse JSON string to JSON object
                    JSONObject json = new JSONObject(record.value());

                    //Instantiate client from payload part of json object
                    //Which contains all the relevant data
                    Person client = gson.fromJson(json.get("payload").toString(), Person.class);

                    found = false;
                    for (Person clnt:
                         clients) {
                        if(clnt.getId() == client.getId()){
                            found = true;
                        }
                    }
                    //If client is not already in the pool
                    if (!found) {
                        //Add client to the pool
                        clients.add(client);
                        System.out.println("Added client: " + client.getName());
                    }
                }
            }

            if(currencyRecords.count() > 0){
                for (ConsumerRecord<Long, String> record : currencyRecords) {

                    //Parse JSON string to JSON object
                    JSONObject json = new JSONObject(record.value());

                    //Instantiate client from payload part of json object
                    //Which contains all the relevant data
                    JSONObject jsonObject = new JSONObject(json.get("payload").toString());

                    Currency currency = gson.fromJson(json.get("payload").toString(), Currency.class);
                    currency.setExchangeRate(Float.parseFloat(jsonObject.get("exchangerate").toString()));

                    found = false;
                    for (Currency crr:
                            currencies) {
                        if(crr.getId() == currency.getId()){
                            found = true;
                        }
                    }
                    //If client is not already in the pool
                    if (!found) {
                        //Add client to the pool
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

            String credInfo = new JSONObject().put("value", cred)
                    .put("currencyName", credCurr.getName())
                    .put("currencyExchangeRate", credCurr.getExchangeRate())
                    .toString();

            //Produce to credit topic
            producer.send(new ProducerRecord<Long, String>(cTopic, (long) credClient.getId(), credInfo));
            System.out.println("Client " + credClient.getId() + " made a credit of " + cred + " " + credCurr.getName() + ".");

            //Produce random pay
            pay = rand.nextFloat() * (20f - 1f);

            //Choose random client to attach to the payment
            payClient = clients.get(rand.nextInt(clients.size()));

            //Choose
            payCurr = currencies.get(rand.nextInt(currencies.size()));

            String payInfo = new JSONObject().put("value", pay)
                    .put("currencyName", payCurr.getName())
                    .put("currencyExchangeRate", payCurr.getExchangeRate())
                    .toString();

            //Produce to payments topic
            producer.send(new ProducerRecord<Long, String>(pTopic, (long) payClient.getId(), payInfo));
            System.out.println("Client " + payClient.getId() + " made a payment of " + pay + " " + payCurr.getName() + ".");

            //Fetch all data from the DBInfoTopics
            System.out.println("Checking for new client records on topic " + dbClientsTopic);
            dClients = Duration.ofMillis(100);
            clientRecords = dbClients.poll(dClients);
            System.out.println(clientRecords.count() + " records found.");

            System.out.println("Checking for new currencies records on topic " + dbCurrenciesTopic);
            dCurrencies = Duration.ofMillis(100);
            currencyRecords = dbCurrencies.poll(dCurrencies);
            System.out.println(currencyRecords.count() + " records found.");

            //Sleep
            Thread.sleep(sleepTime);
        }
    }
}