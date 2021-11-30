import org.apache.kafka.clients.consumer.*;

import java.util.Collections;
import java.util.Properties;

public class KafkaStreams {
    public static void main() throws Exception {

        //Assign Credits and Payments as name topics
        String cTopic = "Credits";
        String pTopic = "Payments";

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
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.LongDeserializer");

        Consumer<String, Long> credits = new KafkaConsumer<>(props);
        Consumer<String, Long> payments = new KafkaConsumer<>(props);

        credits.subscribe(Collections.singletonList(cTopic));
        payments.subscribe(Collections.singletonList(pTopic));

        while (true) {
            ConsumerRecords<String, Long> creditRecords = credits.poll(Long.MAX_VALUE);
            for (ConsumerRecord<String, Long> record : creditRecords) {
                System.out.println(record.key() + " => " + record.value());
            }

            ConsumerRecords<String, Long> paymentRecords = credits.poll(Long.MAX_VALUE);
            for (ConsumerRecord<String, Long> record : paymentRecords) {
                System.out.println(record.key() + " => " + record.value());
            }
        }
        //consumer.close();
    }
}
