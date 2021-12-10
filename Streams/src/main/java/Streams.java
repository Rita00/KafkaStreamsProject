
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import org.apache.kafka.streams.state.*;
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
            double valEuros = value * exchangeRate;

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
        String dbCreditsPerClientTopic = "dbCreditsPerClientTopic";
        String rTopic = "ResultsTopics";
        String paymentsPerClientTopic = "paymentsPerClient";
        String creditsPerClientTopic = "creditsPerClient";
        String balancePerClientTopic = "balancePerClient";
        String totalResultsTopic = "totalResults";

        //Set properties
        java.util.Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "exercises-application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        System.out.println("Creating streams...");

        StreamsBuilder builder = new StreamsBuilder();

        KStream<Long, String> creditsStream = builder.stream(cTopic);

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
                                "\"field\":\"total_credits\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"client_id\":" + k + "," +
                                "\"total_credits\":" + v +
                                "}" +
                                "}"
                )
                .toStream()
                .to(creditsPerClientTopic, Produced.with(Serdes.Long(), Serdes.String()));


        KStream<Long, String> paymentsStream = builder.stream(pTopic);

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
                .to(paymentsPerClientTopic, Produced.with(Serdes.Long(), Serdes.String()));


        //---Balance per client
        ValueJoiner<Double, Double, Double> valueJoiner = ((payment, credit) -> payment - credit);

        KTable<Long, Double> joined = paymentsPerClient.join(creditsPerClient,
                valueJoiner
        );

        joined.mapValues((k, v) ->
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
                                "\"field\":\"current_balance\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"client_id\":" + k + "," +
                                "\"current_balance\":" + v +
                                "}" +
                                "}"
                )
                .toStream()
                .to(balancePerClientTopic, Produced.with(Serdes.Long(), Serdes.String()));


        //---Sum every credit
        KTable<String, Double> dbCreditsTable = creditsStream
                .mapValues((v) -> convertCurrency(v))
                .groupBy((k, v) -> "allCredits", Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce((v1, v2) -> v1 + v2, Materialized.as("aggregateCredits"));

        dbCreditsTable
                .mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"string\"," +
                                "\"optional\":false," +
                                "\"field\":\"aggregate\"" +
                                "}," +
                                "{" +
                                "\"type\":\"double\"," +
                                "\"optional\":false," +
                                "\"field\":\"value\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"aggregate\":\"" + k + "\"," +
                                "\"value\":" + v +
                                "}" +
                                "}"
                )
                .toStream()
                .to(totalResultsTopic, Produced.with(Serdes.String(), Serdes.String()));



        //---Sum every payment
        KTable<String, Double> dbPaymentsTable = creditsStream
                .mapValues((v) -> convertCurrency(v))
                .groupBy((k, v) -> "allPayments", Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce((v1, v2) -> v1 + v2, Materialized.as("aggregatePayments"));

        dbPaymentsTable
                .mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"string\"," +
                                "\"optional\":false," +
                                "\"field\":\"aggregate\"" +
                                "}," +
                                "{" +
                                "\"type\":\"double\"," +
                                "\"optional\":false," +
                                "\"field\":\"value\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"aggregate\":\"" + k + "\"," +
                                "\"value\":" + v +
                                "}" +
                                "}"
                )
                .toStream()
                .to(totalResultsTopic, Produced.with(Serdes.String(), Serdes.String()));


        //---Sum every balance
        KTable<String, Double> dbBalancesTable = creditsStream
                .mapValues((v) -> convertCurrency(v))
                .groupBy((k, v) -> "allBalances", Grouped.with(Serdes.String(), Serdes.Double()))
                .reduce((v1, v2) -> v1 + v2, Materialized.as("aggregateBalances"));

        dbBalancesTable
                .mapValues((k, v) ->
                        "{" +
                                "\"schema\":{" +
                                "\"type\":\"struct\"," +
                                "\"fields\":[" +
                                "{" +
                                "\"type\":\"string\"," +
                                "\"optional\":false," +
                                "\"field\":\"aggregate\"" +
                                "}," +
                                "{" +
                                "\"type\":\"double\"," +
                                "\"optional\":false," +
                                "\"field\":\"value\"" +
                                "}" +
                                "]," +
                                "\"optional\":false," +
                                "\"name\":\"total data\"" +
                                "}," +
                                "\"payload\":{" +
                                "\"aggregate\":\"" + k + "\"," +
                                "\"value\":" + v +
                                "}" +
                                "}"
                )
                .toStream()
                .to(totalResultsTopic, Produced.with(Serdes.String(), Serdes.String()));


        //---Total credit per client for the last month (tumbling window)

        Duration interval = Duration.ofDays(30);

        KTable<Windowed<Long>, Double> creditsPerClientMonthly = creditsStream
                .mapValues((v) -> convertCurrency(v))
                .groupByKey(Grouped.with(Serdes.Long(), Serdes.Double()))
                .windowedBy(TimeWindows.of(interval))
                .reduce((v1, v2) -> v1 + v2, Materialized.as("creditsPerClientMonthly"));

        creditsPerClientMonthly.mapValues((k, v) -> "Total credits for " + k + " are " + v + " euros for the last month.")
               .toStream((wk, v) -> wk.key()).map((k, v) -> new KeyValue<>(k, "" + k + "-->" + v))
                .to(rTopic, Produced.with(Serdes.Long(), Serdes.String()));

        //TO-DO
        //---Get the client with most negative balance

        //TO-DO
        //---Get the manager who has made the highest revenue (the highest sum of clients payments)

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        System.out.println("Reading stream from topic " + cTopic);

    }
}