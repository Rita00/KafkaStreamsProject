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
        propsProducer.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        propsProducer.put("value.serializer", "org.apache.kafka.common.serialization.FloatSerializer");
        //Create producer with previous properties
        Producer<Long, Float> producer = new KafkaProducer<>(propsProducer);

        //Setup properties to consume from DBInfoTopics
        //Assign topicName to string variable
        String topicName = "DBInfoTopics";
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
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        Consumer<String, Long> consumer = new KafkaConsumer<>(props); consumer.subscribe(Collections.singletonList(topicName));

        //List of client ids
        List<Integer> clientIds = new ArrayList<>();

        //From each record we only want the client Id
        System.out.println("Getting clients from "+ topicName);
        ConsumerRecords<String, Long> clientRecords = consumer.poll(Long.MAX_VALUE);
        System.out.println("Number of records fetched from DB: " + clientRecords.count());
        for ( ConsumerRecord<String, Long> record:
             clientRecords) {
            clientIds.add(Math.toIntExact(Long.parseLong(record.key())));
        }
        
        float cred, pay;
        int sleepTime, clientId;
        while(true){
            //Set random sleep time
            sleepTime = rand.nextInt() * (7000 - 5000);

            //Produce random credit
            cred = rand.nextFloat() * (1000f-1f);
            //Choose random client to attach to the credit
            clientId = clientIds.get(rand.nextInt()*(clientIds.size() - 0));
            //Produce to topic
            producer.send(new ProducerRecord<Long,  Float>(cTopic, (long) clientId, cred));

            //Produce random pay
            pay = rand.nextFloat() * (1000f-1f);
            //Choose random client to attach to the credit
            clientId = clientIds.get(rand.nextInt()*(clientIds.size() - 0));
            //Produce to topic
            producer.send(new ProducerRecord<Long, Float>(pTopic,(long) clientId,  pay));

            //Sleep
            Thread.sleep(sleepTime);
        }
        //producer.close();
    }
}
