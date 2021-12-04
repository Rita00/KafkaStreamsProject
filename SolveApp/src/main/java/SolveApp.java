import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

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
        String cTopic = "Credits";
        String pTopic = "Payments";
        String dbTopic = "DBInfoClients";
        String rTopic = "ResultsTopic";

        //Instantiate new stream builder
        StreamsBuilder builder = new StreamsBuilder();

        KStream<Long, Float> creditsStream = builder.stream(cTopic, Consumed.with(Serdes.Long(), Serdes.Float()));
        KStream<Long, Float> paymentsStream = builder.stream(pTopic, Consumed.with(Serdes.Long(), Serdes.Float()));
        KStream<Long, String> clientsStream = builder.stream(dbTopic, Consumed.with(Serdes.Long(), Serdes.String()));
        System.out.println("Created KStreams...");


        //Consume while there are resources to consume
//        while (true) {

        //Read from credits topic and sum all credits for each user
//            KTable<Long, Float> creditLines = builder.table(cTopic);
//            KTable<Long, Long> creditLines = creditsStream.groupByKey().count();
//            creditLines.toStream().to("ResultsTopics", Produced.with(Serdes.Long(), Serdes.Long()));
//            System.out.println("\n\n\n\nAqui\n\n\n\n");
//            creditLines.toStream().print(Printed.toSysOut());
//            System.out.println(creditLines);

//        creditsStream.foreach((key, value) -> System.out.println(key + " =>---------------------- " + value));
//            creditLines.mapValues((k, v) -> k + " => " + v).toStream().to(rTopic);

        //Read from payments topic and sum all payments for each user
//            KTable<Long, Float> paymentLines = paymentsStream.groupByKey().reduce((oldval, newval) -> oldval + newval, Materialized.as("tablename"));

        creditsStream.peek((key, value) -> System.out.println("Client " + key + " has made " + value + " euros in credits."));

        paymentsStream.peek((key, value) -> System.out.println("Client " + key + " has made " + value + " euros in payments."))/*.to(rTopic)*/;

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        while(true);

//        System.out.println("Starting streams reading from topic " + cTopic);
//        Thread.sleep(30000);
//        streams.close();
//        }

        //cConsumer.close();
        //pConsumer.close();
        //dbConsumer.close();
    }
}