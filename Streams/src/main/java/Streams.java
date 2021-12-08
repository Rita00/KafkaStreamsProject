import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class Streams {
    public static void main(String args[]) throws Exception {
        //Set topics names
        String cTopic = "Credits";
        String pTopic = "Payments";
        String dbTopic = "DBInfoTopics";
        String rTopic = "ResultsTopics";

        //Set properties
        java.util.Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "exercises-application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        System.out.println("Creating streams...");

        StreamsBuilder builder = new StreamsBuilder();

        KStream<Long,String> creditsStream = builder.stream(cTopic);

        System.out.println("Created KStreams...");

        KTable<Long, String> outlines = creditsStream.
                groupByKey().
                reduce((oldval, newval) -> String.valueOf(Float.parseFloat(oldval) + Float.parseFloat(newval)));
        outlines.mapValues((k, v) -> k + " => " + v).toStream().to(rTopic, Produced.with(Serdes.Long(), Serdes.String()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        System.out.println("Reading stream from topic " + cTopic);
    }
}