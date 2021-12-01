import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.*;

public class Clients {

    public static void main(String[] args) throws Exception {

        Random rand = new Random();

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
        propsProducer.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        propsProducer.put("value.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        //Create producer with previous properties
        Producer<String, Long> producer = new KafkaProducer<>(propsProducer);


        // create instance for properties to access producer configs
        Properties propsConsumer = new Properties();
        String dbTopic = "DBInfoTopics";
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
        propsConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
        propsConsumer.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        propsConsumer.put("value.deserializer",
                "org.apache.kafka.common.serialization.LongDeserializer");

        Consumer<String, Long> clients = new KafkaConsumer<>(propsConsumer);

        clients.subscribe(Collections.singletonList(dbTopic));
        ConsumerRecords<String, Long> clientRecords = clients.poll(Long.MAX_VALUE);

        List<Integer> clientIds = new ArrayList<>();

        for ( ConsumerRecord<String, Long> record:
             clientRecords) {
            clientIds.add(Integer.parseInt(record.key()));
        }
        
        float cred, pay;
        int sleepTime, clientId;
        for(int i = 0; true; i++){
            //Set random sleep time
            sleepTime = rand.nextInt() * (10000 - 5000);

            //Produce random credit
            cred = rand.nextFloat() * (100f-1f);
            //Choose random client to attach to the credit
            clientId = clientIds.get(rand.nextInt()*(clientIds.size() - 0));
            //Produce to topic
            producer.send(new ProducerRecord<String, Long>(cTopic, Float.toString(cred), (long) clientId));

            //Produce random pay
            pay = rand.nextFloat() * (100f-1f);
            //Choose random client to attach to the credit
            clientId = clientIds.get(rand.nextInt()*(clientIds.size() - 0));
            //Produce to topic
            producer.send(new ProducerRecord<String, Long>(pTopic, Float.toString(pay), (long) clientId));

            //Sleep
            Thread.sleep(sleepTime);
        }

        //producer.close();
    }
}
