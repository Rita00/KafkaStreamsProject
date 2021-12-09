import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


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
        String rTopic = "ResultsTopics";

        //Set properties
        java.util.Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "exercises-application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        System.out.println("Creating streams...");

        StreamsBuilder builder = new StreamsBuilder();

        KStream<Long, String> creditsStream = builder.stream(cTopic);
        KStream<Long, String> paymentsStream = builder.stream(pTopic);

        //---Credit per Client

        KTable<Long, Double> creditsPerClient = creditsStream
                .mapValues((v) -> convertCurrency(v))
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .reduce((v1, v2) -> v1 + v2, Materialized.as("creditsPerClient"));

        creditsPerClient.mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"int64\"," +
                                "\"optional\":false," +
                                "\"field\":\"client_id\"" +
                                "}," +
                                "{" +
                                "\"type\":\"double\"," +
                                "\"optional\":false," +
                                "\"field\":\"total_payments\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"client_id\":" + k + "," +
                                "\"total_payments\":" + v +
                                "}" +
                                "}"
                )
                .toStream()
                .to(rTopic, Produced.with(Serdes.Long(), Serdes.String()));

        //---Payments Per Client
        KTable<Long, Double> paymentsPerClient = paymentsStream
                .mapValues((v) -> convertCurrency(v))
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .reduce((v1, v2) -> v1 + v2, Materialized.as("paymentsPerClient"));

        paymentsPerClient.mapValues((k, v) ->
                            "{" +
                                "\"schema\":{" +
                                    "\"type\":\"struct\"," +
                                    "\"fields\":[" +
                                        "{" +
                                            "\"type\":\"int64\"," +
                                            "\"optional\":false," +
                                            "\"field\":\"client_id\"" +
                                        "}," +
                                        "{" +
                                            "\"type\":\"double\"," +
                                            "\"optional\":false," +
                                            "\"field\":\"total_payments\"" +
                                        "}" +
                                    "]," +
                                    "\"optional\":false," +
                                    "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                    "\"client_id\":" + k + "," +
                                    "\"total_payments\":" + v +
                                "}" +
                            "}"
                )
                .toStream()
                .to(rTopic, Produced.with(Serdes.Long(), Serdes.String()));

        //TO-DO
        //---Balance per client

        //TO-DO
        //---Sum every credit

        //TO-DO
        //---Sum every payment

        //TO-DO
        //---Sum every balance

        //---Total credit per client for the last month (tumbling window)

        Duration interval = Duration.ofDays(30);

        KTable<Windowed<Long>, Double> creditsPerClientMonthly = creditsStream
                .mapValues((v) -> convertCurrency(v))
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .windowedBy(TimeWindows.of(interval))
                .reduce((v1, v2) -> v1 + v2, Materialized.as("creditsPerClientMonthly"));

        /*creditsPerClientMonthly.mapValues((k, v) -> "Total credits for " + k + " are " + v + " euros for the last month.")
                .toStream((wk, v) -> wk.key()).map((k, v) -> new KeyValue<>(k, "" + k + "-->" + v))
                .to(rTopic, Produced.with(Serdes.Long(), Serdes.String()));*/

        //TO-DO
        //---Get the client with most negative balance

        //TO-DO
        //---Get the manager who has made the highest revenue (the highest sum of clients payments)

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        System.out.println("Reading stream from topic " + cTopic);

    }
}