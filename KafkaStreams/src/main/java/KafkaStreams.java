import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class KafkaStreams {
    public static void main(String args[]) throws Exception {

        //Setup Consumer Settings for KafkaStreams application
        //Assign Credits and Payments as name topics
        String cTopic = "Credits";
        String pTopic = "Payments";
        String dbTopic = "DBInfoTopics";
        // create instance for properties to access producer configs
        Properties propsCPConsumer = new Properties();
        //Assign localhost id
        propsCPConsumer.put("bootstrap.servers", "localhost:9092");
        //Set acknowledgements for producer requests.
        propsCPConsumer.put("acks", "all");
        //If the request fails, the producer can automatically retry,
        propsCPConsumer.put("retries", 0);
        //Specify buffer size in config
        propsCPConsumer.put("batch.size", 16384);
        //Reduce the no of requests less than 0
        propsCPConsumer.put("linger.ms", 1);
        //The buffer.memory controls the total amount of memory available to the producer for buffering.
        propsCPConsumer.put("buffer.memory", 33554432);
        propsCPConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
        propsCPConsumer.put("key.deserializer",
                "org.apache.kafka.common.serialization.LongDeserializer");
        propsCPConsumer.put("value.deserializer",
                "org.apache.kafka.common.serialization.FloatDeserializer");
        //Database consumer has different properties because it needs to have different key and value deserializers
        // create instance for properties to access producer configs
        Properties propsDBConsumer = new Properties();
        //Assign localhost id
        propsDBConsumer.put("bootstrap.servers", "localhost:9092");
        //Set acknowledgements for producer requests.
        propsDBConsumer.put("acks", "all");
        //If the request fails, the producer can automatically retry,
        propsDBConsumer.put("retries", 0);
        //Specify buffer size in config
        propsDBConsumer.put("batch.size", 16384);
        //Reduce the no of requests less than 0
        propsDBConsumer.put("linger.ms", 1);
        //The buffer.memory controls the total amount of memory available to the producer for buffering.
        propsDBConsumer.put("buffer.memory", 33554432);
        propsDBConsumer.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
        propsDBConsumer.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        propsDBConsumer.put("value.deserializer",
                "org.apache.kafka.common.serialization.FloatDeserializer");
        Consumer<Long, Float> cConsumer= new KafkaConsumer<>(propsCPConsumer);
        Consumer<Long, Float> pConsumer = new KafkaConsumer<>(propsCPConsumer);
        Consumer<String, Float> dbConsumer = new KafkaConsumer<>(propsDBConsumer);
        //Set each consumer to their topic
        cConsumer.subscribe(Collections.singletonList(cTopic));
        cConsumer.subscribe(Collections.singletonList(pTopic));
        dbConsumer.subscribe(Collections.singletonList(dbTopic));

        //Setup properties to produce results to ResultsTopics
        String rTopic = "ResultsTopics";
        //Assign Credits and Payments as name topics
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
        //Instantiate new stream builder
        StreamsBuilder builder = new StreamsBuilder();

        //Get list of currencies from DBInfoTopics
//        System.out.println("Getting clients from "+ dbTopic);
//        ConsumerRecords<String, Float> clientRecords = dbConsumer.poll(Long.MAX_VALUE);
//        System.out.println("Number of records fetched from DB: " + clientRecords.count());
//        List<String> currencyList = new ArrayList<String>();
//        for ( ConsumerRecord<String, Float> record:
//                clientRecords) {
//            currencyList.add(record.key());
//        }

        //Consume while there are resources to consume
        while (true) {
            //Get records from Credits topic
            ConsumerRecords<Long, Float> creditRecords = cConsumer.poll(Long.MAX_VALUE);
            for (ConsumerRecord<Long, Float> record : creditRecords) {
                System.out.println("Client " +record.key() + " made a credit of " + record.value() + "euros.");

                //Outputs a stream to ResultsTopic with key:clientId and value:count of credits
//                KStream<Long, Long> lines = builder.stream(cTopic);
//                KTable<Long, Long> outlines = lines.
//                        groupByKey().count();
//                outlines.toStream().to(rTopic);
            }

            //Get records from Payments topic
//            ConsumerRecords<Long, Float> paymentRecords = cConsumer.poll(Long.MAX_VALUE);
//            for (ConsumerRecord<Long, Float> record : paymentRecords) {
//                System.out.println("Client " +record.key() + " made a payment of " + record.value() + "euros.");
//
//                //Outputs a stream to ResultsTopic with key:clientId and value:count of credits
////                KStream<Long, Long> lines = builder.stream(pTopic);
////                KTable<Long, Long> outlines = lines.
////                        groupByKey().count();
////                outlines.toStream().to(rTopic);
//
//            }
        }

        //cConsumer.close();
        //pConsumer.close();
        //dbConsumer.close();
    }
}