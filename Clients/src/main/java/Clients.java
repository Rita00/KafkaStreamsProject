import com.google.gson.Gson;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import Entities.Client;
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
        String dbTopic = "DBInfoTopics";

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
        //Create the consumer with previous settings
        Consumer<Long, String> dbConsumer = new KafkaConsumer<>(propsConsumer);

        //Subscribe to the DBInfoTopics
        dbConsumer.subscribe(Collections.singletonList(dbTopic));

        //List of client ids
        ArrayList<Long> clientIds = new ArrayList<>();

        float cred, pay;
        int sleepTime = 2500;
        long clientId;

        while (true) {
            //Fetch all data from the DBInfoTopics
            System.out.println("Getting clients from " + dbTopic);
            Duration d = Duration.ofMillis(0);
            ConsumerRecords<Long, String> clientRecords = dbConsumer.poll(d);
            System.out.println("Number of records fetched from DB: " + clientRecords.count());

            for (int i = 0; i < 3; i++) {
                for (ConsumerRecord<Long, String> record : clientRecords) {
                    //Parse JSON string to JSON object
                    JSONObject json = new JSONObject(record.value());
                    //Instantiate client from payload part of json object
                    //Which contains all the relevant data
                    Client client = gson.fromJson(json.get("payload").toString(), Client.class);
                    //If client is not already in the pool
                    if (!clientIds.contains(client.getClient_id())) {
                        //Add client to the pool
                        clientIds.add(client.getClient_id());
                        System.out.println("Added client: " + client.getClient_name());
                    }
                }
                System.out.println("Number of clients currently in the pool: " + clientIds.size());

                //Produce random credit
                cred = rand.nextFloat() * (1000f - 1f);

                //Choose random client to attach to the credit
                clientId = clientIds.get(rand.nextInt(clientIds.size()));

                //Produce to credits topic
                producer.send(new ProducerRecord<Long, String>(cTopic, (long) clientId, String.valueOf(cred)));
                System.out.println("Client " + clientId + " made a credit of " + cred + " euros.");

                //Produce random pay
                pay = rand.nextFloat() * (1000f - 1f);

                //Choose random client to attach to the payment
                clientId = clientIds.get(rand.nextInt(clientIds.size()));

                //Produce to payments topic
                producer.send(new ProducerRecord<Long, String>(pTopic, (long) clientId, String.valueOf(pay)));
                System.out.println("Client " + clientId + " made a payment of " + pay + " euros.");

                //Sleep
                Thread.sleep(sleepTime);
            }
        }
        //producer.close();
    }
}