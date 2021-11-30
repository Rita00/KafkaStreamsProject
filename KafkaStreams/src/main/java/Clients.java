import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;

public class Clients {

    public static void main(String[] args) throws Exception {

        Random rand = new Random();

        //Setup properties to produce to both topics
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
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.LongSerializer");

        //Create producer with previous properties
        Producer<String, Long> producer = new KafkaProducer<>(props);

        float cred, pay;
        int sleepTime;
        for(int i = 0; true; i++){
            sleepTime = rand.nextInt() * (10000 - 5000);

            //Produce random credit
            cred = rand.nextFloat() * (100f-1f);
            producer.send(new ProducerRecord<String, Long>(cTopic, Float.toString(cred), (long) i));

            //Produce random pay
            pay = rand.nextFloat() * (100f-1f);
            producer.send(new ProducerRecord<String, Long>(pTopic, Float.toString(pay), (long) i));

            //Sleep
            Thread.sleep(sleepTime);

        }

        //producer.close();
    }
}
