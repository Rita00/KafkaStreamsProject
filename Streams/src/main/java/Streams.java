
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Properties;


public class Streams {
    public static Double convertCurrency(String jsonString) {

        JSONObject json = null;
        try {
            json = new JSONObject(jsonString);

            double value = Double.parseDouble(json.get("value").toString());
            double exchangeRate = Double.parseDouble(json.get("currencyExchangeRate").toString());
            String coinName = json.get("currencyName").toString();
            double valEuros = value * exchangeRate;


            //System.out.println(value + " " + coinName + " is  " + valEuros + " euros");

            return valEuros;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String args[]) throws Exception {
        //Set topics names
        String cTopic = "Credits";
        String pTopic = "Payments";
        String dbTopic = "DBInfoTopics";
        String rTopicCred = "ResultsTopics";
        String rTopicPay = "ResultsTopics_1";


        //Set properties
        java.util.Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "exercises-application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        System.out.println("Creating streams...");

        StreamsBuilder builder = new StreamsBuilder();


        KStream<Long, String> creditsStream = builder.stream(cTopic);

        KTable<Long, Double> creditsPerClient = creditsStream
                .mapValues((v) -> convertCurrency(v))
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .reduce((v1, v2) -> v1 + v2);

        creditsPerClient.mapValues((k, v) -> k + " => " + v)
                .toStream()
                .to(rTopicCred, Produced.with(Serdes.Long(), Serdes.String()));

        KStream<Long, String> paymentsStream = builder.stream(pTopic);

        KTable<Long, Double> paymentsPerClient = paymentsStream
                .mapValues((v) -> convertCurrency(v))
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .reduce((v1, v2) -> v1 + v2);

        paymentsPerClient.mapValues((k, v) -> k + " => " + v)
                .toStream()
                .to(rTopicPay, Produced.with(Serdes.Long(), Serdes.String()));


        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        System.out.println("Reading stream from topic " + cTopic);
    }
}