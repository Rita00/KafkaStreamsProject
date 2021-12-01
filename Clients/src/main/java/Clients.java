import com.google.gson.Gson;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import Entities.Client;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Clients {

    public static void main(String[] args) throws Exception {
        Random rand = new Random();
        Gson gson = new Gson();

        //Setup properties to produce to both topics
        //Assign Credits and Payments as name topics
        String cTopic = "Credits";
        String pTopic = "Payments";
        // create instance for properties to access producer configs
        Properties propsProducer = new Properties();
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
        propsProducer.put("value.serializer", "org.apache.kafka.common.serialization.FloatSerializer");
        //Create producer with previous properties
        Producer<Long, Float> producer = new KafkaProducer<>(propsProducer);

        //Setup properties to consume from DBInfoTopics
        //Assign topicName to string variable
        String dbTopic = "DBInfoTopics";
        // create instance for properties to access producer configs
        Properties props = new Properties();
        //Assign localhost id
        props.put("bootstrap.servers", "localhost:9092");
        //Set acknowledgements for producer requests.
        props.put("acks", "all");
        //If the request fails, the producer can automatically retry,
        props.put("retries", 0);
        //Specify buffer size in config
        props.put("batch.size", 16384);
        //Reduce the no of requests less than 0
        props.put("linger.ms", 1);
        //The buffer.memory controls the total amount of memory available to the producer for buffering.
        props.put("buffer.memory", 33554432);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        Consumer<Long, String> dbConsumer = new KafkaConsumer<>(props);
        dbConsumer.subscribe(Collections.singletonList(dbTopic));

        //List of client ids
        ArrayList<Long> clientIds = new ArrayList<>();

        //From each record we only want the client Id
        System.out.println("Getting clients from "+ dbTopic);
        ConsumerRecords<Long, String> clientRecords = dbConsumer.poll(Long.MAX_VALUE);
        System.out.println("Number of records fetched from DB: " + clientRecords.count());


        for ( ConsumerRecord<Long, String> record:
             clientRecords) {

            Client client = gson.fromJson(record.value(), Client.class);

            System.out.println(client.getClient_name());

            if(!clientIds.contains(client.getClient_id())){
                clientIds.add(client.getClient_id());
            }
        }

        System.out.println("Number of clients: "+ clientIds.size());

        float cred, pay;
        int sleepTime, index;
        long clientId;

        while(true){
            //Set random sleep time
            sleepTime = rand.nextInt() * (7000 - 5000);

            //Produce random credit
            cred = rand.nextFloat() * (1000f-1f);

            //Choose random client to attach to the credit
            index = rand.nextInt(clientIds.size());
            System.out.println(index);

            //Get client Id
            clientId = clientIds.get(index);
            //Produce to topic
            producer.send(new ProducerRecord<Long,  Float>(cTopic, (long) clientId, cred));

            //Produce random pay
            pay = rand.nextFloat() * (1000f-1f);

            //Choose random client to attach to the credit
            index = rand.nextInt(clientIds.size());
            System.out.println(index);

            //Choose random client to attach to the credit
            clientId = clientIds.get(index);
            //Produce to topic
            producer.send(new ProducerRecord<Long, Float>(pTopic,(long) clientId,  pay));

            //Sleep
            Thread.sleep(sleepTime);
        }
        //producer.close();
    }
}
