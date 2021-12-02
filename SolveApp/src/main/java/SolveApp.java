import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class SolveApp {
    public static void main(String args[]) throws Exception {

        //Set stream properties
        java.util.Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "SolveApp");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Float().getClass());

        //Set topic names
        String cTopic = "Clients";
        String pTopic = "Payments";
        String dbTopic = "DBInfoTopics";
        String rTopic = "ResultsTopics";

        //Instantiate new stream builder
        StreamsBuilder builder = new StreamsBuilder();

        KStream<Long, Float> creditsStream = builder.stream(cTopic);
        KStream<Long, Float> paymentsStream = builder.stream(pTopic);
        KStream<Long, String> clientsStream = builder.stream(dbTopic);
        System.out.println("Created KStreams...");


        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        System.out.println("Starting streams reading from topic " + cTopic);

        //Consume while there are resources to consume
        while (true) {

            //Read from credits topic and sum all credits for each user
            KTable<Long, Long> creditLines = creditsStream.groupByKey().count();
            creditLines.mapValues((k, v) -> k + " => " + v).toStream().to(rTopic);

            //Read from payments topic and sum all payments for each user
            KTable<Long, Float> paymentLines = paymentsStream.groupByKey().reduce((oldval, newval) -> oldval + newval, Materialized.as("tablename"));;
            paymentLines.toStream().peek((key, value) -> System.out.println("Client " + key +" has made " + value + " euros in payments.")).to(rTopic);
        }

        //cConsumer.close();
        //pConsumer.close();
        //dbConsumer.close();
    }
}